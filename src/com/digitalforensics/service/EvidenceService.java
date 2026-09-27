package com.digitalforensics.service;

import com.digitalforensics.dao.CaseDAO;
import com.digitalforensics.dao.CustodyDAO;
import com.digitalforensics.dao.EvidenceDAO;
import com.digitalforensics.exception.EvidenceNotFoundException;
import com.digitalforensics.exception.ForensicsException;
import com.digitalforensics.exception.InvalidCaseException;
import com.digitalforensics.exception.InvalidEvidenceException;
import com.digitalforensics.exception.UnauthorizedAccessException;
import com.digitalforensics.model.Case;
import com.digitalforensics.model.Evidence;
import com.digitalforensics.model.EvidenceStatus;
import com.digitalforensics.model.EvidenceType;
import com.digitalforensics.model.HashCheck;
import com.digitalforensics.model.IntegrityStatus;
import com.digitalforensics.model.User;
import com.digitalforensics.util.FileUtil;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/** Evidence registration, upload, integrity verification and custody logging. */
public class EvidenceService {

    public static final long MAX_FILE_SIZE = 20L * 1024 * 1024; // 20 MB

    private final EvidenceDAO evidenceDAO = new EvidenceDAO();
    private final CaseDAO caseDAO = new CaseDAO();
    private final CustodyDAO custodyDAO = new CustodyDAO();
    private final HashService hashService = new HashService();

    public Evidence get(int id) throws EvidenceNotFoundException {
        Evidence ev = evidenceDAO.findById(id);
        if (ev == null) {
            throw new EvidenceNotFoundException("Evidence #" + id + " does not exist.");
        }
        return ev;
    }

    public List<Evidence> all() {
        return evidenceDAO.findAll();
    }

    public List<Evidence> byCase(int caseId) {
        return evidenceDAO.findByCase(caseId);
    }

    public List<HashCheck> hashHistory(int evidenceId) {
        return evidenceDAO.findHashChecks(evidenceId);
    }

    public Path fileOf(Evidence ev) {
        return FileUtil.storageDir().resolve(ev.getStoredName());
    }

    /**
     * Upload workflow:
     *  1. check role and case   2. validate the file   3. save it to evidence-storage/
     *  4. SHA-256 on a background thread   5. insert metadata   6. write two custody records
     */
    public int upload(int caseId, String originalName, InputStream content, long size, EvidenceType type,
                      String description, LocalDateTime collectedAt, User user) throws ForensicsException {
        if (!user.getRole().canUploadEvidence()) {
            throw new UnauthorizedAccessException("Your role cannot upload evidence.");
        }
        Case c = caseDAO.findById(caseId);
        if (c == null) {
            throw new InvalidCaseException("Please choose a valid case.");
        }
        if (c.isClosed()) {
            throw new InvalidCaseException("Evidence cannot be added to a closed case.");
        }
        if (originalName == null || originalName.isBlank()) {
            throw new InvalidEvidenceException("Please choose a file to upload.");
        }
        String safeName = FileUtil.sanitize(originalName);
        String ext = FileUtil.extension(safeName);
        if (!FileUtil.ALLOWED_EXTENSIONS.contains(ext)) {
            throw new InvalidEvidenceException("File type ." + ext + " is not allowed. Allowed: "
                    + String.join(", ", FileUtil.ALLOWED_EXTENSIONS));
        }
        if (size <= 0) {
            throw new InvalidEvidenceException("The file is empty.");
        }
        if (size > MAX_FILE_SIZE) {
            throw new InvalidEvidenceException("File is larger than the 20 MB limit.");
        }
        if (collectedAt == null) {
            collectedAt = LocalDateTime.now();
        }
        if (collectedAt.isAfter(LocalDateTime.now().plusMinutes(5))) {
            throw new InvalidEvidenceException("Collection time cannot be in the future.");
        }

        // unique prefix so two files with the same name never overwrite each other
        String storedName = UUID.randomUUID().toString().substring(0, 8) + "_" + safeName;
        Path target = FileUtil.storageDir().resolve(storedName);
        try {
            Files.copy(content, target);
        } catch (IOException e) {
            throw new InvalidEvidenceException("Could not save the file: " + e.getMessage());
        }

        String sha256 = hashService.calculate(target);

        Evidence ev = new Evidence();
        ev.setCaseId(caseId);
        ev.setFileName(safeName);
        ev.setStoredName(storedName);
        ev.setEvidenceType(type != null ? type : FileUtil.guessType(safeName));
        ev.setFileSize(size);
        ev.setSha256Original(sha256);
        ev.setDescription(description);
        ev.setCollectedById(user.getId());
        ev.setCollectedAt(collectedAt);
        int id = evidenceDAO.insert(ev);

        custodyDAO.insert(id, "Evidence Collected", user.getId(), null,
                "Registered under case " + c.getCaseNumber());
        custodyDAO.insert(id, "Hash Generated", user.getId(), null, "SHA-256 " + sha256);
        return id;
    }

    /** Re-hashes the stored file and compares it with the hash taken at upload. */
    public HashCheck verify(int evidenceId, User user) throws ForensicsException {
        Evidence ev = get(evidenceId);
        Path file = fileOf(ev);
        if (!Files.exists(file)) {
            throw new EvidenceNotFoundException("The evidence file is missing from storage: " + ev.getStoredName());
        }
        String current = hashService.calculate(file);
        IntegrityStatus result = hashService.isUnchanged(ev.getSha256Original(), current)
                ? IntegrityStatus.VERIFIED : IntegrityStatus.MODIFIED;

        evidenceDAO.updateIntegrity(evidenceId, result);
        evidenceDAO.insertHashCheck(evidenceId, current, result, user.getId());
        custodyDAO.insert(evidenceId, result == IntegrityStatus.VERIFIED ? "Hash Verified" : "Integrity Check FAILED",
                user.getId(), null, "Computed SHA-256 " + current);

        HashCheck check = new HashCheck();
        check.setEvidenceId(evidenceId);
        check.setComputedHash(current);
        check.setResult(result);
        return check;
    }

    public void changeStatus(int evidenceId, EvidenceStatus status, User user) throws ForensicsException {
        if (!user.getRole().canRecordCustody()) {
            throw new UnauthorizedAccessException("Your role cannot change evidence status.");
        }
        Evidence ev = get(evidenceId);
        if (ev.getStatus() == status) {
            return;
        }
        evidenceDAO.updateStatus(evidenceId, status);
        custodyDAO.insert(evidenceId, "Status: " + status.getLabel(), user.getId(), null,
                "Changed from " + ev.getStatus().getLabel());
    }

    /** Called by AnalysisService the first time a file is analysed. */
    void markInAnalysis(Evidence ev, User user) {
        if (ev.getStatus() == EvidenceStatus.COLLECTED) {
            evidenceDAO.updateStatus(ev.getId(), EvidenceStatus.IN_ANALYSIS);
            custodyDAO.insert(ev.getId(), "Evidence Analyzed", user.getId(), null, "Log analysis started");
        }
    }
}
