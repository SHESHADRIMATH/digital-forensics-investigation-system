package com.digitalforensics.dao;

import com.digitalforensics.model.Evidence;
import com.digitalforensics.model.EvidenceStatus;
import com.digitalforensics.model.EvidenceType;
import com.digitalforensics.model.HashCheck;
import com.digitalforensics.model.IntegrityStatus;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/** Evidence metadata plus its hash-check history. */
public class EvidenceDAO extends BaseDAO {

    private static final String SELECT =
            "SELECT e.*, c.case_number, u.full_name AS collector_name "
          + "FROM evidence e JOIN cases c ON c.id = e.case_id JOIN users u ON u.id = e.collected_by ";

    public Evidence findById(int id) {
        return queryOne(SELECT + "WHERE e.id = ?", EvidenceDAO::map, id);
    }

    public List<Evidence> findAll() {
        return queryList(SELECT + "ORDER BY e.uploaded_at DESC", EvidenceDAO::map);
    }

    public List<Evidence> findByCase(int caseId) {
        return queryList(SELECT + "WHERE e.case_id = ? ORDER BY e.collected_at", EvidenceDAO::map, caseId);
    }

    public int insert(Evidence ev) {
        return insert("INSERT INTO evidence (case_id, file_name, stored_name, evidence_type, file_size, "
                        + "sha256_original, description, collected_by, collected_at) VALUES (?,?,?,?,?,?,?,?,?)",
                ev.getCaseId(), ev.getFileName(), ev.getStoredName(), ev.getEvidenceType(), ev.getFileSize(),
                ev.getSha256Original(), ev.getDescription(), ev.getCollectedById(), ev.getCollectedAt());
    }

    public void updateStatus(int id, EvidenceStatus status) {
        update("UPDATE evidence SET status = ? WHERE id = ?", status, id);
    }

    public void updateIntegrity(int id, IntegrityStatus integrity) {
        update("UPDATE evidence SET integrity = ?, last_verified_at = ? WHERE id = ?",
                integrity, LocalDateTime.now(), id);
    }

    public int countAll() {
        return queryInt("SELECT COUNT(*) FROM evidence");
    }

    public int countByIntegrity(IntegrityStatus integrity) {
        return queryInt("SELECT COUNT(*) FROM evidence WHERE integrity = ?", integrity);
    }

    // ---- hash_checks table ----

    public void insertHashCheck(int evidenceId, String computedHash, IntegrityStatus result, int userId) {
        insert("INSERT INTO hash_checks (evidence_id, computed_hash, result, checked_by) VALUES (?,?,?,?)",
                evidenceId, computedHash, result, userId);
    }

    public List<HashCheck> findHashChecks(int evidenceId) {
        return queryList("SELECT h.*, u.full_name FROM hash_checks h JOIN users u ON u.id = h.checked_by "
                + "WHERE h.evidence_id = ? ORDER BY h.checked_at DESC", EvidenceDAO::mapCheck, evidenceId);
    }

    public List<HashCheck> findHashChecksByCase(int caseId) {
        return queryList("SELECT h.*, u.full_name FROM hash_checks h JOIN users u ON u.id = h.checked_by "
                + "JOIN evidence e ON e.id = h.evidence_id WHERE e.case_id = ? ORDER BY h.checked_at",
                EvidenceDAO::mapCheck, caseId);
    }

    private static Evidence map(ResultSet rs) throws SQLException {
        Evidence ev = new Evidence();
        ev.setId(rs.getInt("id"));
        ev.setCaseId(rs.getInt("case_id"));
        ev.setCaseNumber(rs.getString("case_number"));
        ev.setFileName(rs.getString("file_name"));
        ev.setStoredName(rs.getString("stored_name"));
        ev.setEvidenceType(EvidenceType.valueOf(rs.getString("evidence_type")));
        ev.setFileSize(rs.getLong("file_size"));
        ev.setSha256Original(rs.getString("sha256_original"));
        ev.setIntegrity(IntegrityStatus.valueOf(rs.getString("integrity")));
        ev.setLastVerifiedAt(time(rs, "last_verified_at"));
        ev.setStatus(EvidenceStatus.valueOf(rs.getString("status")));
        ev.setDescription(rs.getString("description"));
        ev.setCollectedById(rs.getInt("collected_by"));
        ev.setCollectedByName(rs.getString("collector_name"));
        ev.setCollectedAt(time(rs, "collected_at"));
        ev.setUploadedAt(time(rs, "uploaded_at"));
        return ev;
    }

    private static HashCheck mapCheck(ResultSet rs) throws SQLException {
        HashCheck h = new HashCheck();
        h.setId(rs.getInt("id"));
        h.setEvidenceId(rs.getInt("evidence_id"));
        h.setComputedHash(rs.getString("computed_hash"));
        h.setResult(IntegrityStatus.valueOf(rs.getString("result")));
        h.setCheckedByName(rs.getString("full_name"));
        h.setCheckedAt(time(rs, "checked_at"));
        return h;
    }
}
