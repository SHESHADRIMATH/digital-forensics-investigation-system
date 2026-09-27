package com.digitalforensics.controller;

import com.digitalforensics.exception.ForensicsException;
import com.digitalforensics.model.Case;
import com.digitalforensics.model.CaseStatus;
import com.digitalforensics.model.Priority;
import com.digitalforensics.model.User;
import com.digitalforensics.service.AuthenticationService;
import com.digitalforensics.service.CaseService;
import com.digitalforensics.service.EvidenceService;
import com.digitalforensics.service.FindingService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * /cases              -> list + search
 * /cases?action=new   -> create form
 * /cases?id=5         -> case details
 * POST action=create | update | close
 */
@WebServlet("/cases")
public class CaseServlet extends BaseServlet {

    private final CaseService caseService = new CaseService();
    private final EvidenceService evidenceService = new EvidenceService();
    private final FindingService findingService = new FindingService();
    private final AuthenticationService authService = new AuthenticationService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("priorities", Priority.values());
        req.setAttribute("statuses", CaseStatus.values());
        req.setAttribute("users", authService.activeUsers());

        if ("new".equals(req.getParameter("action"))) {
            req.setAttribute("incidentTypes", CaseService.INCIDENT_TYPES);
            render(req, resp, "cases/form", "cases");
            return;
        }

        Integer id = intParam(req, "id");
        if (id != null) {
            try {
                req.setAttribute("theCase", caseService.get(id));
                req.setAttribute("evidenceList", evidenceService.byCase(id));
                req.setAttribute("findings", findingService.forCase(id));
                render(req, resp, "cases/view", "cases");
            } catch (ForensicsException e) {
                flash(req, "error", e.getMessage());
                redirect(req, resp, "/cases");
            }
            return;
        }

        String q = param(req, "q");
        CaseStatus status = enumParam(req, "status", CaseStatus.class);
        req.setAttribute("q", q);
        req.setAttribute("selectedStatus", status);
        req.setAttribute("cases", caseService.search(q, status));
        render(req, resp, "cases/list", "cases");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User user = currentUser(req);
        String action = param(req, "action");
        Integer id = intParam(req, "id");
        try {
            switch (action == null ? "" : action) {
                case "create" -> {
                    Case c = new Case();
                    c.setTitle(param(req, "title"));
                    c.setDescription(param(req, "description"));
                    c.setIncidentType(param(req, "incidentType"));
                    c.setPriority(enumParam(req, "priority", Priority.class));
                    c.setAssignedToId(intParam(req, "assignedTo"));
                    int newId = caseService.create(c, user);
                    flash(req, "success", "Case created successfully.");
                    redirect(req, resp, "/cases?id=" + newId);
                }
                case "update" -> {
                    Priority p = enumParam(req, "priority", Priority.class);
                    CaseStatus s = enumParam(req, "status", CaseStatus.class);
                    if (id == null || p == null || s == null) throw new ForensicsException("Invalid update request.");
                    caseService.update(id, p, s, intParam(req, "assignedTo"), user);
                    flash(req, "success", "Case updated.");
                    redirect(req, resp, "/cases?id=" + id);
                }
                case "close" -> {
                    if (id == null) throw new ForensicsException("Invalid case.");
                    caseService.close(id, user);
                    flash(req, "success", "Case closed.");
                    redirect(req, resp, "/cases?id=" + id);
                }
                default -> redirect(req, resp, "/cases");
            }
        } catch (ForensicsException e) {
            flash(req, "error", e.getMessage());
            redirect(req, resp, id != null ? "/cases?id=" + id : "/cases?action=new");
        }
    }
}
