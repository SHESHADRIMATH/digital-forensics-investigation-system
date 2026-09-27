package com.digitalforensics.service;

import com.digitalforensics.dao.CustodyDAO;
import com.digitalforensics.exception.ForensicsException;
import com.digitalforensics.exception.InvalidEvidenceException;
import com.digitalforensics.exception.UnauthorizedAccessException;
import com.digitalforensics.model.CustodyRecord;
import com.digitalforensics.model.User;

import java.util.List;

/** Manual chain-of-custody entries (transfers, storage, etc.). */
public class CustodyService {

    /** Actions a user can record by hand. Upload/verify actions are written automatically. */
    public static final List<String> MANUAL_ACTIONS = List.of(
            "Transferred", "Received", "Evidence Analyzed", "Moved to Secure Storage",
            "Copied for Analysis", "Returned to Owner");

    private final CustodyDAO custodyDAO = new CustodyDAO();
    private final EvidenceService evidenceService = new EvidenceService();

    public void record(int evidenceId, String action, Integer handedTo, String notes, User user)
            throws ForensicsException {
        if (!user.getRole().canRecordCustody()) {
            throw new UnauthorizedAccessException("Your role cannot record custody actions.");
        }
        evidenceService.get(evidenceId); // throws if the evidence does not exist
        if (!MANUAL_ACTIONS.contains(action)) {
            throw new InvalidEvidenceException("Please choose a valid custody action.");
        }
        if ("Transferred".equals(action) && handedTo == null) {
            throw new InvalidEvidenceException("Choose who the evidence is being transferred to.");
        }
        if (handedTo != null && handedTo == user.getId()) {
            throw new InvalidEvidenceException("You cannot transfer evidence to yourself.");
        }
        if (notes != null && notes.length() > 500) {
            throw new InvalidEvidenceException("Notes must be 500 characters or fewer.");
        }
        custodyDAO.insert(evidenceId, action, user.getId(), "Transferred".equals(action) ? handedTo : null, notes);
    }

    public List<CustodyRecord> forEvidence(int evidenceId) {
        return custodyDAO.findByEvidence(evidenceId);
    }

    public List<CustodyRecord> forCase(int caseId) {
        return custodyDAO.findByCase(caseId);
    }

    public List<CustodyRecord> recent(int limit) {
        return custodyDAO.findRecent(limit);
    }
}
