package com.digitalforensics.dao;

import com.digitalforensics.model.Finding;
import com.digitalforensics.model.FindingStatus;
import com.digitalforensics.model.Severity;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class FindingDAO extends BaseDAO {

    private static final String SELECT =
            "SELECT f.*, c.case_number, e.file_name, u.full_name "
          + "FROM findings f JOIN cases c ON c.id = f.case_id "
          + "LEFT JOIN evidence e ON e.id = f.evidence_id "
          + "JOIN users u ON u.id = f.created_by ";

    // CRITICAL first, then HIGH ...
    private static final String ORDER =
            "ORDER BY FIELD(f.severity,'CRITICAL','HIGH','MEDIUM','LOW'), f.created_at DESC";

    public void insert(Finding f, int createdBy) {
        insert("INSERT INTO findings (case_id, evidence_id, title, description, severity, created_by) VALUES (?,?,?,?,?,?)",
                f.getCaseId(), f.getEvidenceId(), f.getTitle(), f.getDescription(), f.getSeverity(), createdBy);
    }

    public Finding findById(int id) {
        return queryOne(SELECT + "WHERE f.id = ?", FindingDAO::map, id);
    }

    public List<Finding> findByCase(int caseId) {
        return queryList(SELECT + "WHERE f.case_id = ? " + ORDER, FindingDAO::map, caseId);
    }

    public List<Finding> findAll() {
        return queryList(SELECT + ORDER, FindingDAO::map);
    }

    public void updateStatus(int id, FindingStatus status) {
        update("UPDATE findings SET status = ? WHERE id = ?", status, id);
    }

    public int countOpen() {
        return queryInt("SELECT COUNT(*) FROM findings WHERE status = 'OPEN'");
    }

    private static Finding map(ResultSet rs) throws SQLException {
        Finding f = new Finding();
        f.setId(rs.getInt("id"));
        f.setCaseId(rs.getInt("case_id"));
        f.setCaseNumber(rs.getString("case_number"));
        f.setEvidenceId(nullableInt(rs, "evidence_id"));
        f.setEvidenceFileName(rs.getString("file_name"));
        f.setTitle(rs.getString("title"));
        f.setDescription(rs.getString("description"));
        f.setSeverity(Severity.valueOf(rs.getString("severity")));
        f.setStatus(FindingStatus.valueOf(rs.getString("status")));
        f.setCreatedByName(rs.getString("full_name"));
        f.setCreatedAt(time(rs, "created_at"));
        return f;
    }
}
