package com.digitalforensics.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Output of AnalysisService: how often each keyword appeared and which lines matched.
 * Not stored in the database - it is recalculated from the evidence file each time.
 */
public class LogAnalysisResult {

    /** One matching line of the log. */
    public static class MatchedLine {
        private final int lineNumber;
        private final String text;
        private final String keyword;
        private final LocalDateTime timestamp;

        public MatchedLine(int lineNumber, String text, String keyword, LocalDateTime timestamp) {
            this.lineNumber = lineNumber;
            this.text = text;
            this.keyword = keyword;
            this.timestamp = timestamp;
        }

        public int getLineNumber() { return lineNumber; }
        public String getText() { return text; }
        public String getKeyword() { return keyword; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public boolean isHasTimestamp() { return timestamp != null; }
    }

    // LinkedHashMap keeps keywords in the order the user typed them
    private final Map<String, Integer> keywordCounts = new LinkedHashMap<>();
    private final List<MatchedLine> matches = new ArrayList<>();
    private int totalLines;
    private long elapsedMillis;
    private boolean truncated;

    public Map<String, Integer> getKeywordCounts() { return keywordCounts; }
    public List<MatchedLine> getMatches() { return matches; }
    public int getTotalLines() { return totalLines; }
    public void setTotalLines(int totalLines) { this.totalLines = totalLines; }
    public long getElapsedMillis() { return elapsedMillis; }
    public void setElapsedMillis(long elapsedMillis) { this.elapsedMillis = elapsedMillis; }
    public boolean isTruncated() { return truncated; }
    public void setTruncated(boolean truncated) { this.truncated = truncated; }

    public int getTotalMatches() {
        return keywordCounts.values().stream().mapToInt(Integer::intValue).sum();
    }

    /** Largest count, used to scale the bar chart in the JSP. */
    public int getMaxCount() {
        return keywordCounts.values().stream().mapToInt(Integer::intValue).max().orElse(0);
    }
}
