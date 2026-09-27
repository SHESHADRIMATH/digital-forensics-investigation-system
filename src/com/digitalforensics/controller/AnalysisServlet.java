package com.digitalforensics.controller;

import com.digitalforensics.exception.ForensicsException;
import com.digitalforensics.model.Evidence;
import com.digitalforensics.service.AnalysisService;
import com.digitalforensics.service.EvidenceService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * GET  /analysis                     -> pick a log file
 * POST action=analyze                -> run keyword scan, show results
 * POST action=timeline               -> copy selected lines to the case timeline
 */
@WebServlet("/analysis")
public class AnalysisServlet extends BaseServlet {

    private final AnalysisService analysisService = new AnalysisService();
    private final EvidenceService evidenceService = new EvidenceService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        prepareForm(req, intParam(req, "evidenceId"), String.join(", ", AnalysisService.DEFAULT_KEYWORDS));
        render(req, resp, "analysis", "analysis");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer evidenceId = intParam(req, "evidenceId");
        String action = param(req, "action");
        try {
            if (evidenceId == null) throw new ForensicsException("Please choose an evidence file.");
            Evidence ev = evidenceService.get(evidenceId);

            if ("timeline".equals(action)) {
                String[] raw = req.getParameterValues("line");
                Set<Integer> lines = raw == null ? Set.of() : Arrays.stream(raw)
                        .map(s -> { try { return Integer.valueOf(s); } catch (NumberFormatException e) { return null; } })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());
                int added = analysisService.addLinesToTimeline(ev, lines, currentUser(req));
                flash(req, "success", added + " event(s) added to the timeline of " + ev.getCaseNumber() + ".");
                redirect(req, resp, "/timeline?caseId=" + ev.getCaseId());
                return;
            }

            String keywordText = param(req, "keywords");
            Set<String> keywords = AnalysisService.parseKeywords(keywordText);
            req.setAttribute("result", analysisService.analyze(ev, keywords, currentUser(req)));
            req.setAttribute("analyzed", ev);
            prepareForm(req, evidenceId, String.join(", ", keywords));
            render(req, resp, "analysis", "analysis");

        } catch (ForensicsException e) {
            flash(req, "error", e.getMessage());
            redirect(req, resp, "/analysis" + (evidenceId != null ? "?evidenceId=" + evidenceId : ""));
        }
    }

    private void prepareForm(HttpServletRequest req, Integer selectedId, String keywords) {
        List<Evidence> textFiles = evidenceService.all().stream()
                .filter(Evidence::isTextReadable)
                .toList();
        req.setAttribute("textFiles", textFiles);
        req.setAttribute("selectedId", selectedId);
        req.setAttribute("keywords", keywords);
        req.setAttribute("defaultKeywords", AnalysisService.DEFAULT_KEYWORDS);
    }
}
