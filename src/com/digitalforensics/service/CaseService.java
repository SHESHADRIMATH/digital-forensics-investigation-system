package com.digitalforensics.service;

import com.digitalforensics.dao.CaseDAO;
import com.digitalforensics.dao.TimelineDAO;
import com.digitalforensics.exception.ForensicsException;
import com.digitalforensics.exception.InvalidCaseException;
import com.digitalforensics.exception.UnauthorizedAccessException;
import com.digitalforensics.model.Case;
import com.digitalforensics.model.CaseStatus;
import com.digitalforensics.model.Priority;
import com.digitalforensics.model.TimelineEvent;
import com.digitalforensics.model.TimelineSource;
import com.digitalforensics.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Business rules for creating, updating and closing cases. */
public class CaseService {

    public static final List<String> INCIDENT_TYPES = List.of(
            "Unauthorized Access", "Malware / Phishing", "Data Leakage", "Insider Threat",
            "Fraud", "Ransomware", "Policy Violation", "Other");

    private final CaseDAO caseDAO = new CaseDAO();
    private final TimelineDAO timelineDAO = new TimelineDAO();

    public Case get(int id) throws InvalidCaseException {
        Case c = caseDAO.findById(id);
        if (c == null) {
            throw new InvalidCaseException("Case #" + id + " does not exist.");
        }
        return c;
    }

    public List<Case> search(String text, CaseStatus status) {
        return caseDAO.search(text, status);
    }

    public List<Case> openCases() {
        return caseDAO.findOpen();
    }

    public List<Case> allCases() {
        return caseDAO.findAll();
    }

    public List<Case> recent(int limit) {
        return caseDAO.findRecent(limit);
    }

    public int create(Case c, User user) throws ForensicsException {
        if (!user.getRole().canCreateCase()) {
            throw new UnauthorizedAccessException("Your role cannot create cases.");
        }
        validate(c);
        if (c.getAssignedToId() != null && !user.getRole().canAssignCase()) {
            c.setAssignedToId(user.getId()); // investigators can only assign a new case to themselves
        }
        c.setCaseNumber(caseDAO.nextCaseNumber());
        c.setStatus(CaseStatus.OPEN);
        c.setCreatedById(user.getId());
        int id = caseDAO.insert(c);
        logSystemEvent(id, "Case created", "Case " + c.getCaseNumber() + " opened by " + user.getFullName(), user);
        return id;
    }

    public void update(int caseId, Priority priority, CaseStatus status, Integer assignedTo, User user)
            throws ForensicsException {
        if (!user.getRole().canUpdateCase()) {
            throw new UnauthorizedAccessException("Your role cannot update cases.");
        }
        Case c = get(caseId);
        if (c.isClosed()) {
            throw new InvalidCaseException("Closed cases cannot be changed.");
        }
        if (status == CaseStatus.CLOSED) {
            throw new InvalidCaseException("Use the 'Close case' button to close a case.");
        }
        if (!user.getRole().canAssignCase()) {
            assignedTo = c.getAssignedToId(); // silently keep the existing assignee
        }

        List<String> changes = new ArrayList<>();
        if (c.getPriority() != priority) changes.add("Priority " + c.getPriority().getLabel() + " → " + priority.getLabel());
        if (c.getStatus() != status) changes.add("Status " + c.getStatus().getLabel() + " → " + status.getLabel());
        if (!Objects.equals(c.getAssignedToId(), assignedTo)) changes.add("Investigator reassigned");
        if (changes.isEmpty()) {
            return;
        }
        caseDAO.updateDetails(caseId, priority, status, assignedTo);
        logSystemEvent(caseId, "Case updated", String.join("; ", changes) + " (by " + user.getFullName() + ")", user);
    }

    public void close(int caseId, User user) throws ForensicsException {
        if (!user.getRole().canCloseCase()) {
            throw new UnauthorizedAccessException("Only an administrator can close a case.");
        }
        Case c = get(caseId);
        if (c.isClosed()) {
            throw new InvalidCaseException("Case is already closed.");
        }
        caseDAO.close(caseId);
        logSystemEvent(caseId, "Case closed", "Closed by " + user.getFullName(), user);
    }

    private void validate(Case c) throws InvalidCaseException {
        if (c.getTitle() == null || c.getTitle().isBlank()) {
            throw new InvalidCaseException("Case title is required.");
        }
        if (c.getTitle().length() > 150) {
            throw new InvalidCaseException("Case title must be 150 characters or fewer.");
        }
        if (c.getIncidentType() == null || !INCIDENT_TYPES.contains(c.getIncidentType())) {
            throw new InvalidCaseException("Please choose a valid incident type.");
        }
        if (c.getPriority() == null) {
            throw new InvalidCaseException("Please choose a priority.");
        }
    }

    private void logSystemEvent(int caseId, String title, String description, User user) {
        TimelineEvent ev = new TimelineEvent();
        ev.setCaseId(caseId);
        ev.setEventTime(LocalDateTime.now());
        ev.setTitle(title);
        ev.setDescription(description);
        ev.setSource(TimelineSource.SYSTEM);
        timelineDAO.insert(ev, user.getId());
    }
}
