package com.digitalforensics.service;

import com.digitalforensics.dao.EvidenceDAO;
import com.digitalforensics.dao.FindingDAO;
import com.digitalforensics.exception.ForensicsException;
import com.digitalforensics.exception.InvalidCaseException;
import com.digitalforensics.exception.UnauthorizedAccessException;
import com.digitalforensics.model.Evidence;
import com.digitalforensics.model.Finding;
import com.digitalforensics.model.FindingStatus;
import com.digitalforensics.model.User;

import java.util.List;

public class FindingService {

    private final FindingDAO findingDAO = new FindingDAO();
    private final EvidenceDAO evidenceDAO = new EvidenceDAO();

    public List<Finding> forCase(int caseId) {
        return findingDAO.findByCase(caseId);
    }

    public List<Finding> all() {
        return findingDAO.findAll();
    }

    /** Any logged-in role may record a finding. */
    public void create(Finding f, User user) throws ForensicsException {
        if (f.getTitle() == null || f.getTitle().isBlank()) {
            throw new InvalidCaseException("Finding title is required.");
        }
        if (f.getTitle().length() > 150) {
            throw new InvalidCaseException("Finding title must be 150 characters or fewer.");
        }
        if (f.getSeverity() == null) {
            throw new InvalidCaseException("Please choose a severity.");
        }
        if (f.getEvidenceId() != null) {
            Evidence ev = evidenceDAO.findById(f.getEvidenceId());
            if (ev == null || ev.getCaseId() != f.getCaseId()) {
                throw new InvalidCaseException("The linked evidence does not belong to this case.");
            }
        }
        findingDAO.insert(f, user.getId());
    }

    public void changeStatus(int findingId, FindingStatus status, User user) throws ForensicsException {
        if (!user.getRole().canReviewFindings()) {
            throw new UnauthorizedAccessException("Only investigators and administrators can review findings.");
        }
        if (findingDAO.findById(findingId) == null) {
            throw new InvalidCaseException("Finding does not exist.");
        }
        findingDAO.updateStatus(findingId, status);
    }
}
