package com.digitalforensics.service;

import com.digitalforensics.dao.CustodyDAO;
import com.digitalforensics.dao.TimelineDAO;
import com.digitalforensics.exception.ForensicsException;
import com.digitalforensics.exception.InvalidCaseException;
import com.digitalforensics.model.CustodyRecord;
import com.digitalforensics.model.TimelineEvent;
import com.digitalforensics.model.TimelineSource;
import com.digitalforensics.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Reconstructs an investigation timeline.
 * Events come from two places - the timeline_events table and the chain of custody -
 * and are merged and sorted in Java.
 */
public class TimelineService {

    private final TimelineDAO timelineDAO = new TimelineDAO();
    private final CustodyDAO custodyDAO = new CustodyDAO();

    /** All events for a case in chronological order; pass a source to filter, or null for all. */
    public List<TimelineEvent> build(int caseId, TimelineSource onlySource) {
        List<TimelineEvent> events = new ArrayList<>(timelineDAO.findByCase(caseId));

        for (CustodyRecord r : custodyDAO.findByCase(caseId)) {
            events.add(fromCustody(r));
        }

        if (onlySource != null) {
            events.removeIf(e -> e.getSource() != onlySource); // lambda as a Predicate
        }
        Collections.sort(events); // uses TimelineEvent.compareTo -> oldest first
        return events;
    }

    /** Groups sorted events by day, keeping order, e.g. {"2026-09-19": [...], "2026-09-20": [...]}. */
    public Map<String, List<TimelineEvent>> groupByDay(List<TimelineEvent> sorted) {
        return sorted.stream().collect(
                Collectors.groupingBy(TimelineEvent::getEventDate, LinkedHashMap::new, Collectors.toList()));
    }

    public void addManual(int caseId, LocalDateTime when, String title, String description, User user)
            throws ForensicsException {
        if (when == null) {
            throw new InvalidCaseException("Please enter a valid date and time for the event.");
        }
        if (title == null || title.isBlank()) {
            throw new InvalidCaseException("Event title is required.");
        }
        if (title.length() > 150) {
            throw new InvalidCaseException("Event title must be 150 characters or fewer.");
        }
        TimelineEvent te = new TimelineEvent();
        te.setCaseId(caseId);
        te.setEventTime(when);
        te.setTitle(title.trim());
        te.setDescription(description);
        te.setSource(TimelineSource.MANUAL);
        addRaw(te, user);
    }

    void addRaw(TimelineEvent te, User user) {
        timelineDAO.insert(te, user.getId());
    }

    private TimelineEvent fromCustody(CustodyRecord r) {
        TimelineEvent te = new TimelineEvent();
        te.setId(-r.getId()); // negative id marks it as derived, not stored in timeline_events
        te.setCaseId(0);
        te.setEventTime(r.getActionTime());
        te.setTitle(r.getAction() + " - " + r.getEvidenceFileName());
        String details = "Chain of custody: " + r.getPerformedByName()
                + (r.getHandedToName() != null ? " → " + r.getHandedToName() : "");
        te.setDescription(r.getNotes() == null ? details : details + ". " + r.getNotes());
        te.setSource(TimelineSource.SYSTEM);
        te.setEvidenceId(r.getEvidenceId());
        te.setCreatedByName(r.getPerformedByName());
        return te;
    }
}
