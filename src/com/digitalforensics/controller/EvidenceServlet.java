package com.digitalforensics.controller;

import com.digitalforensics.exception.ForensicsException;
import com.digitalforensics.model.Evidence;
import com.digitalforensics.model.EvidenceStatus;
import com.digitalforensics.model.EvidenceType;
import com.digitalforensics.model.HashCheck;
import com.digitalforensics.model.IntegrityStatus;
import com.digitalforensics.model.User;
import com.digitalforensics.service.AuthenticationService;
import com.digitalforensics.service.CaseService;
import com.digitalforensics.service.CustodyService;
import com.digitalforensics.service.EvidenceService;
import com.digitalforensics.util.DateUtil;
import com.digitalforensics.util.FileUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.io.InputStream;

/**
 * /evidence                 -> evidence register
 * /evidence?action=upload   -> upload form
 * /evidence?id=3            -> evidence viewer (metadata, hash, custody)
 * POST action=upload | verify | status
 *
 * @MultipartConfig lets this servlet receive file uploads (multipart/form-data).
 */
@WebServlet("/evidence")
@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 25L * 1024 * 1024, maxRequestSize = 26L * 1024 * 1024)
public class EvidenceServlet extends BaseServlet {

    private final EvidenceService evidenceService = new EvidenceService();
    private final CaseService caseService = new CaseService();
    private final CustodyService custodyService = new CustodyService();
    private final AuthenticationService authService = new AuthenticationService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if ("upload".equals(req.getParameter("action"))) {
            req.setAttribute("openCases", caseService.openCases());
            req.setAttribute("types", EvidenceType.values());
            req.setAttribute("selectedCaseId", intParam(req, "caseId"));
            req.setAttribute("now", DateUtil.nowForInput());
            req.setAttribute("allowed", String.join(", ", FileUtil.ALLOWED_EXTENSIONS));
            render(req, resp, "evidence/upload", "evidence");
            return;
        }

        Integer id = intParam(req, "id");
        if (id != null) {
            try {
                Evidence ev = evidenceService.get(id);
                req.setAttribute("ev", ev);
                req.setAttribute("custody", custodyService.forEvidence(id));
                req.setAttribute("hashChecks", evidenceService.hashHistory(id));
                req.setAttribute("users", authService.activeUsers());
                req.setAttribute("custodyActions", CustodyService.MANUAL_ACTIONS);
                req.setAttribute("evidenceStatuses", EvidenceStatus.values());
                render(req, resp, "evidence/view", "evidence");
            } catch (ForensicsException e) {
                flash(req, "error", e.getMessage());
                redirect(req, resp, "/evidence");
            }
            return;
        }

        req.setAttribute("evidenceList", evidenceService.all());
        render(req, resp, "evidence/list", "evidence");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = currentUser(req);
        String action = param(req, "action");
        Integer id = intParam(req, "id");
        try {
            if ("upload".equals(action)) {
                Integer caseId = intParam(req, "caseId");
                if (caseId == null) throw new ForensicsException("Please choose a case.");
                Part file = req.getPart("file");
                if (file == null || file.getSize() == 0) throw new ForensicsException("Please choose a file to upload.");
                int newId;
                try (InputStream in = file.getInputStream()) {
                    newId = evidenceService.upload(caseId, file.getSubmittedFileName(), in, file.getSize(),
                            enumParam(req, "evidenceType", EvidenceType.class), param(req, "description"),
                            DateUtil.parseInput(param(req, "collectedAt")), user);
                }
                flash(req, "success", "Evidence uploaded and SHA-256 hash recorded.");
                redirect(req, resp, "/evidence?id=" + newId);

            } else if ("verify".equals(action) && id != null) {
                HashCheck check = evidenceService.verify(id, user);
                if (check.getResult() == IntegrityStatus.VERIFIED) {
                    flash(req, "success", "Integrity verified - the file is unchanged since upload.");
                } else {
                    flash(req, "error", "WARNING: hash mismatch - this evidence may have been modified!");
                }
                redirect(req, resp, "/evidence?id=" + id);

            } else if ("status".equals(action) && id != null) {
                EvidenceStatus status = enumParam(req, "status", EvidenceStatus.class);
                if (status == null) throw new ForensicsException("Please choose a status.");
                evidenceService.changeStatus(id, status, user);
                flash(req, "success", "Evidence status updated.");
                redirect(req, resp, "/evidence?id=" + id);

            } else {
                redirect(req, resp, "/evidence");
            }
        } catch (ForensicsException e) {
            flash(req, "error", e.getMessage());
            redirect(req, resp, id != null ? "/evidence?id=" + id : "/evidence?action=upload");
        } catch (IllegalStateException e) {
            // thrown by getPart() when the file is bigger than maxFileSize
            flash(req, "error", "File is too large. The limit is 20 MB.");
            redirect(req, resp, "/evidence?action=upload");
        }
    }
}
