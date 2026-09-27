package com.digitalforensics.controller;

import com.digitalforensics.exception.ForensicsException;
import com.digitalforensics.service.CaseService;
import com.digitalforensics.service.CustodyService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/** Chain-of-custody log for a whole case, and the form handler for new custody entries. */
@WebServlet("/custody")
public class CustodyServlet extends BaseServlet {

    private final CustodyService custodyService = new CustodyService();
    private final CaseService caseService = new CaseService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer caseId = intParam(req, "caseId");
        req.setAttribute("cases", caseService.allCases());
        req.setAttribute("selectedCaseId", caseId);
        req.setAttribute("records", caseId == null ? custodyService.recent(100) : custodyService.forCase(caseId));
        render(req, resp, "custody", "custody");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Integer evidenceId = intParam(req, "evidenceId");
        try {
            if (evidenceId == null) throw new ForensicsException("Invalid evidence.");
            custodyService.record(evidenceId, param(req, "custodyAction"), intParam(req, "handedTo"),
                    param(req, "notes"), currentUser(req));
            flash(req, "success", "Custody entry recorded.");
        } catch (ForensicsException e) {
            flash(req, "error", e.getMessage());
        }
        redirect(req, resp, evidenceId == null ? "/custody" : "/evidence?id=" + evidenceId + "#custody");
    }
}
