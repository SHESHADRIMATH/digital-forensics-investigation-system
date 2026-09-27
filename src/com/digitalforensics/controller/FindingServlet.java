package com.digitalforensics.controller;

import com.digitalforensics.exception.ForensicsException;
import com.digitalforensics.model.Finding;
import com.digitalforensics.model.FindingStatus;
import com.digitalforensics.model.Severity;
import com.digitalforensics.service.CaseService;
import com.digitalforensics.service.EvidenceService;
import com.digitalforensics.service.FindingService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/findings")
public class FindingServlet extends BaseServlet {

    private final FindingService findingService = new FindingService();
    private final CaseService caseService = new CaseService();
    private final EvidenceService evidenceService = new EvidenceService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer caseId = intParam(req, "caseId");
        req.setAttribute("cases", caseService.allCases());
        req.setAttribute("severities", Severity.values());
        req.setAttribute("findingStatuses", FindingStatus.values());
        req.setAttribute("selectedCaseId", caseId);
        if (caseId != null) {
            req.setAttribute("findings", findingService.forCase(caseId));
            req.setAttribute("caseEvidence", evidenceService.byCase(caseId));
        } else {
            req.setAttribute("findings", findingService.all());
        }
        render(req, resp, "findings", "findings");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Integer caseId = intParam(req, "caseId");
        try {
            if ("status".equals(param(req, "action"))) {
                Integer id = intParam(req, "id");
                FindingStatus status = enumParam(req, "status", FindingStatus.class);
                if (id == null || status == null) throw new ForensicsException("Invalid request.");
                findingService.changeStatus(id, status, currentUser(req));
                flash(req, "success", "Finding marked as " + status.getLabel() + ".");
            } else {
                if (caseId == null) throw new ForensicsException("Please choose a case.");
                caseService.get(caseId);
                Finding f = new Finding();
                f.setCaseId(caseId);
                f.setEvidenceId(intParam(req, "evidenceId"));
                f.setTitle(param(req, "title"));
                f.setDescription(param(req, "description"));
                f.setSeverity(enumParam(req, "severity", Severity.class));
                findingService.create(f, currentUser(req));
                flash(req, "success", "Finding recorded.");
            }
        } catch (ForensicsException e) {
            flash(req, "error", e.getMessage());
        }
        redirect(req, resp, "/findings" + (caseId != null ? "?caseId=" + caseId : ""));
    }
}
