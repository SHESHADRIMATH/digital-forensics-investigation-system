package com.digitalforensics.controller;

import com.digitalforensics.exception.ForensicsException;
import com.digitalforensics.model.Case;
import com.digitalforensics.model.CustodyRecord;
import com.digitalforensics.model.Evidence;
import com.digitalforensics.model.Finding;
import com.digitalforensics.model.HashCheck;
import com.digitalforensics.model.Reportable;
import com.digitalforensics.model.TimelineEvent;
import com.digitalforensics.service.CaseService;
import com.digitalforensics.service.CustodyService;
import com.digitalforensics.service.EvidenceService;
import com.digitalforensics.service.FindingService;
import com.digitalforensics.service.ReportService;
import com.digitalforensics.service.ReportService.ReportType;
import com.digitalforensics.service.TimelineService;
import com.digitalforensics.util.DateUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

/**
 * /reports                                  -> choose case + report type
 * /reports?caseId=1&type=CUSTODY            -> printable HTML report
 * /reports?caseId=1&type=CUSTODY&format=csv -> CSV download
 */
@WebServlet("/reports")
public class ReportServlet extends BaseServlet {

    private final ReportService reportService = new ReportService();
    private final CaseService caseService = new CaseService();
    private final EvidenceService evidenceService = new EvidenceService();
    private final CustodyService custodyService = new CustodyService();
    private final TimelineService timelineService = new TimelineService();
    private final FindingService findingService = new FindingService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer caseId = intParam(req, "caseId");
        ReportType type = enumParam(req, "type", ReportType.class);

        if (caseId == null || type == null) {
            req.setAttribute("cases", caseService.allCases());
            req.setAttribute("types", ReportType.values());
            req.setAttribute("selectedCaseId", caseId);
            render(req, resp, "reports/index", "reports");
            return;
        }

        try {
            Case c = caseService.get(caseId);
            List<Evidence> evidence = evidenceService.byCase(caseId);
            List<CustodyRecord> custody = custodyService.forCase(caseId);
            List<TimelineEvent> timeline = timelineService.build(caseId, null);
            List<Finding> findings = findingService.forCase(caseId);
            List<HashCheck> hashChecks = reportService.hashChecksForCase(caseId);

            if ("csv".equals(req.getParameter("format")) && type != ReportType.SUMMARY) {
                List<? extends Reportable> rows = switch (type) {
                    case EVIDENCE -> evidence;
                    case HASH -> hashChecks;
                    case CUSTODY -> custody;
                    case TIMELINE -> timeline;
                    default -> findings;
                };
                String[] emptyHeader = switch (type) {
                    case EVIDENCE -> new Evidence().csvHeader();
                    case HASH -> new HashCheck().csvHeader();
                    case CUSTODY -> new CustodyRecord().csvHeader();
                    case TIMELINE -> new TimelineEvent().csvHeader();
                    default -> new Finding().csvHeader();
                };
                String fileName = c.getCaseNumber() + "_" + type.name().toLowerCase() + "_report.csv";
                resp.setContentType("text/csv; charset=UTF-8");
                resp.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
                resp.getOutputStream().write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF}); // UTF-8 BOM for Excel
                resp.getOutputStream().write(reportService.toCsv(rows, emptyHeader).getBytes(StandardCharsets.UTF_8));
                return;
            }

            req.setAttribute("theCase", c);
            req.setAttribute("type", type);
            req.setAttribute("evidence", evidence);
            req.setAttribute("custody", custody);
            req.setAttribute("timeline", timeline);
            req.setAttribute("findings", findings);
            req.setAttribute("hashChecks", hashChecks);
            req.setAttribute("generatedAt", DateUtil.formatSeconds(LocalDateTime.now()));
            req.setAttribute("generatedBy", currentUser(req).getFullName());
            render(req, resp, "reports/print", "reports");
        } catch (ForensicsException e) {
            flash(req, "error", e.getMessage());
            redirect(req, resp, "/reports");
        }
    }
}
