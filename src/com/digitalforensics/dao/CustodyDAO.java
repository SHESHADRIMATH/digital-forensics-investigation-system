package com.digitalforensics.dao;

import com.digitalforensics.model.CustodyRecord;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Chain-of-custody records. There is deliberately no update() or delete():
 * custody history must never be changed once written.
 */
public class CustodyDAO extends BaseDAO {

    private static final String SELECT =
            "SELECT k.*, e.file_name, c.case_number, p.full_name AS performer, h.full_name AS receiver "
          + "FROM chain_of_custody k "
          + "JOIN evidence e ON e.id = k.evidence_id "
          + "JOIN cases c ON c.id = e.case_id "
          + "JOIN users p ON p.id = k.performed_by "
          + "LEFT JOIN users h ON h.id = k.handed_to ";

    public void insert(int evidenceId, String action, int performedBy, Integer handedTo, String notes) {
        insert("INSERT INTO chain_of_custody (evidence_id, action, performed_by, handed_to, notes) VALUES (?,?,?,?,?)",
                evidenceId, action, performedBy, handedTo, notes);
    }

    public List<CustodyRecord> findByEvidence(int evidenceId) {
        return queryList(SELECT + "WHERE k.evidence_id = ? ORDER BY k.action_time, k.id", CustodyDAO::map, evidenceId);
    }

    public List<CustodyRecord> findByCase(int caseId) {
        return queryList(SELECT + "WHERE e.case_id = ? ORDER BY k.action_time, k.id", CustodyDAO::map, caseId);
    }

    public List<CustodyRecord> findRecent(int limit) {
        return queryList(SELECT + "ORDER BY k.action_time DESC, k.id DESC LIMIT ?", CustodyDAO::map, limit);
    }

    private static CustodyRecord map(ResultSet rs) throws SQLException {
        CustodyRecord r = new CustodyRecord();
        r.setId(rs.getInt("id"));
        r.setEvidenceId(rs.getInt("evidence_id"));
        r.setEvidenceFileName(rs.getString("file_name"));
        r.setCaseNumber(rs.getString("case_number"));
        r.setAction(rs.getString("action"));
        r.setPerformedById(rs.getInt("performed_by"));
        r.setPerformedByName(rs.getString("performer"));
        r.setHandedToId(nullableInt(rs, "handed_to"));
        r.setHandedToName(rs.getString("receiver"));
        r.setNotes(rs.getString("notes"));
        r.setActionTime(time(rs, "action_time"));
        return r;
    }
}
