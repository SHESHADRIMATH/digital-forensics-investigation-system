package com.digitalforensics.controller;

import com.digitalforensics.exception.ForensicsException;
import com.digitalforensics.model.Case;
import com.digitalforensics.model.TimelineEvent;
import com.digitalforensics.model.TimelineSource;
import com.digitalforensics.service.CaseService;
import com.digitalforensics.service.TimelineService;
import com.digitalforensics.util.DateUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet("/timeline")
public class TimelineServlet extends BaseServlet {

    private final TimelineService timelineService = new TimelineService();
    private final CaseService caseService = new CaseService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<Case> cases = caseService.allCases();
        req.setAttribute("cases", cases);
        req.setAttribute("sources", TimelineSource.values());
        req.setAttribute("now", DateUtil.nowForInput());

        Integer caseId = intParam(req, "caseId");
        if (caseId == null && !cases.isEmpty()) {
            caseId = cases.get(0).getId(); // default to the newest case
        }
        if (caseId != null) {
            try {
                TimelineSource source = enumParam(req, "source", TimelineSource.class);
                List<TimelineEvent> events = timelineService.build(caseId, source);
                req.setAttribute("theCase", caseService.get(caseId));
                req.setAttribute("selectedSource", source);
                req.setAttribute("eventCount", events.size());
                req.setAttribute("days", timelineService.groupByDay(events));
            } catch (ForensicsException e) {
                req.setAttribute("flash", e.getMessage());
                req.setAttribute("flashType", "error");
            }
        }
        render(req, resp, "timeline", "timeline");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Integer caseId = intParam(req, "caseId");
        try {
            if (caseId == null) throw new ForensicsException("Invalid case.");
            caseService.get(caseId);
            timelineService.addManual(caseId, DateUtil.parseInput(param(req, "eventTime")),
                    param(req, "title"), param(req, "description"), currentUser(req));
            flash(req, "success", "Event added to the timeline.");
        } catch (ForensicsException e) {
            flash(req, "error", e.getMessage());
        }
        redirect(req, resp, "/timeline" + (caseId != null ? "?caseId=" + caseId : ""));
    }
}
