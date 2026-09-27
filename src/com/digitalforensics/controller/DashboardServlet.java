package com.digitalforensics.controller;

import com.digitalforensics.service.CaseService;
import com.digitalforensics.service.CustodyService;
import com.digitalforensics.service.FindingService;
import com.digitalforensics.service.ReportService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/** "" maps the site root (http://localhost:8080/forensics/) to the dashboard too. */
@WebServlet(urlPatterns = {"", "/dashboard"})
public class DashboardServlet extends BaseServlet {

    private final ReportService reportService = new ReportService();
    private final CaseService caseService = new CaseService();
    private final CustodyService custodyService = new CustodyService();
    private final FindingService findingService = new FindingService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("stats", reportService.dashboardStats());
        req.setAttribute("recentCases", caseService.recent(5));
        req.setAttribute("recentCustody", custodyService.recent(6));
        req.setAttribute("topFindings", findingService.all().stream().limit(5).toList());
        render(req, resp, "dashboard", "dashboard");
    }
}
