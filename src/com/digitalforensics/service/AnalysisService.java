package com.digitalforensics.service;

import com.digitalforensics.exception.ForensicsException;
import com.digitalforensics.exception.InvalidEvidenceException;
import com.digitalforensics.model.Evidence;
import com.digitalforensics.model.LogAnalysisResult;
import com.digitalforensics.model.LogAnalysisResult.MatchedLine;
import com.digitalforensics.model.TimelineEvent;
import com.digitalforensics.model.TimelineSource;
import com.digitalforensics.model.User;
import com.digitalforensics.util.BackgroundExecutor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Keyword-based log analysis.
 * Reads a text evidence file line by line and counts suspicious keywords.
 */
public class AnalysisService {

    public static final List<String> DEFAULT_KEYWORDS = List.of(
            "LOGIN_FAILED", "UNAUTHORIZED", "ACCESS_DENIED", "ERROR", "MALWARE", "SUSPICIOUS", "USB");

    private static final int MAX_MATCHES_SHOWN = 500;

    /** Matches timestamps like 2026-09-19 22:41:10 or 2026-09-19T22:41:10 at any position in a line. */
    private static final Pattern TIMESTAMP = Pattern.compile("(\\d{4}-\\d{2}-\\d{2})[ T](\\d{2}:\\d{2}:\\d{2})");

    private final EvidenceService evidenceService = new EvidenceService();
    private final TimelineService timelineService = new TimelineService();

    /** Splits "error, malware  usb" into a clean, de-duplicated, upper-case keyword set. */
    public static Set<String> parseKeywords(String input) {
        if (input == null || input.isBlank()) {
            return new LinkedHashSet<>(DEFAULT_KEYWORDS);
        }
        Set<String> keywords = new LinkedHashSet<>();
        Arrays.stream(input.split("[,\\s]+"))
                .map(String::trim)
                .filter(k -> !k.isEmpty())
                .map(k -> k.toUpperCase(Locale.ROOT))
                .limit(15)
                .forEach(keywords::add);
        return keywords;
    }

    public LogAnalysisResult analyze(Evidence ev, Set<String> keywords, User user) throws ForensicsException {
        if (!ev.isTextReadable()) {
            throw new InvalidEvidenceException("Only .log, .txt and .csv files can be analysed.");
        }
        Path file = evidenceService.fileOf(ev);
        if (!Files.exists(file)) {
            throw new InvalidEvidenceException("The evidence file is missing from storage.");
        }

        // run the scan on a worker thread, wait up to 20 seconds
        Future<LogAnalysisResult> future = BackgroundExecutor.submit(() -> scan(file, keywords));
        LogAnalysisResult result;
        try {
            result = future.get(20, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            throw new ForensicsException("Log analysis timed out.");
        } catch (ExecutionException e) {
            throw new ForensicsException("Could not read the log file: " + e.getCause().getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ForensicsException("Log analysis was interrupted.", e);
        }
        evidenceService.markInAnalysis(ev, user);
        return result;
    }

    private LogAnalysisResult scan(Path file, Set<String> keywords) throws IOException {
        long start = System.currentTimeMillis();
        LogAnalysisResult result = new LogAnalysisResult();
        keywords.forEach(k -> result.getKeywordCounts().put(k, 0));

        // InputStreamReader replaces invalid bytes instead of failing, so binary junk won't crash the scan
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(Files.newInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            int lineNo = 0;
            while ((line = reader.readLine()) != null) {
                lineNo++;
                String upper = line.toUpperCase(Locale.ROOT);
                String firstHit = null;
                for (String k : keywords) {
                    if (upper.contains(k)) {
                        result.getKeywordCounts().merge(k, 1, Integer::sum);
                        if (firstHit == null) firstHit = k;
                    }
                }
                if (firstHit != null) {
                    if (result.getMatches().size() < MAX_MATCHES_SHOWN) {
                        result.getMatches().add(new MatchedLine(lineNo, line, firstHit, parseTimestamp(line)));
                    } else {
                        result.setTruncated(true);
                    }
                }
            }
            result.setTotalLines(lineNo);
        }
        result.setElapsedMillis(System.currentTimeMillis() - start);
        return result;
    }

    /**
     * Copies the chosen log lines onto the case timeline.
     * Line numbers come from the browser, but the text is re-read from the file on
     * the server so a user cannot inject fake log content.
     */
    public int addLinesToTimeline(Evidence ev, Set<Integer> lineNumbers, User user) throws ForensicsException {
        if (lineNumbers.isEmpty()) {
            throw new InvalidEvidenceException("Select at least one line to add to the timeline.");
        }
        Path file = evidenceService.fileOf(ev);
        int added = 0;
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(Files.newInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            int lineNo = 0;
            while ((line = reader.readLine()) != null) {
                lineNo++;
                if (!lineNumbers.contains(lineNo)) continue;
                LocalDateTime when = parseTimestamp(line);
                if (when == null) continue; // lines without a timestamp can't be placed on a timeline
                TimelineEvent te = new TimelineEvent();
                te.setCaseId(ev.getCaseId());
                te.setEventTime(when);
                // title = the log line without its timestamp
                te.setTitle(shorten(TIMESTAMP.matcher(line).replaceFirst("").trim(), 150));
                te.setDescription(ev.getFileName() + " line " + lineNo + ": " + shorten(line, 900));
                te.setSource(TimelineSource.LOG);
                te.setEvidenceId(ev.getId());
                timelineService.addRaw(te, user);
                added++;
            }
        } catch (IOException e) {
            throw new ForensicsException("Could not read the log file: " + e.getMessage(), e);
        }
        return added;
    }

    static LocalDateTime parseTimestamp(String line) {
        Matcher m = TIMESTAMP.matcher(line);
        if (!m.find()) return null;
        try {
            return LocalDateTime.parse(m.group(1) + "T" + m.group(2));
        } catch (java.time.format.DateTimeParseException e) {
            return null;
        }
    }

    private static String shorten(String s, int max) {
        if (s.isEmpty()) return "Log event";
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }
}
