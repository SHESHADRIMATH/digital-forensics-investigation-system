package com.digitalforensics.dao;

import com.digitalforensics.model.Case;
import com.digitalforensics.model.CaseStatus;
import com.digitalforensics.model.Priority;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CaseDAO extends BaseDAO {

    // JOIN users twice to get the names of the assignee and the creator
    private static final String SELECT =
            "SELECT c.*, a.full_name AS assigned_name, u.full_name AS creator_name, "
          + "  (SELECT COUNT(*) FROM evidence e WHERE e.case_id = c.id) AS evidence_count "
          + "FROM cases c "
          + "LEFT JOIN users a ON a.id = c.assigned_to "
          + "JOIN users u ON u.id = c.created_by ";

    public Case findById(int id) {
        return queryOne(SELECT + "WHERE c.id = ?", CaseDAO::map, id);
    }

    public List<Case> findAll() {
        return queryList(SELECT + "ORDER BY c.created_at DESC", CaseDAO::map);
    }

    /** Search by case number / title / incident type, optionally filtered by status. */
    public List<Case> search(String text, CaseStatus status) {
        StringBuilder sql = new StringBuilder(SELECT).append("WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        if (text != null && !text.isBlank()) {
            sql.append("AND (c.case_number LIKE ? OR c.title LIKE ? OR c.incident_type LIKE ?) ");
            String like = "%" + text.trim() + "%";
            params.add(like);
            params.add(like);
            params.add(like);
        }
        if (status != null) {
            sql.append("AND c.status = ? ");
            params.add(status);
        }
        sql.append("ORDER BY FIELD(c.status,'OPEN','IN_PROGRESS','ON_HOLD','CLOSED'), c.created_at DESC");
        return queryList(sql.toString(), CaseDAO::map, params.toArray());
    }

    public List<Case> findRecent(int limit) {
        return queryList(SELECT + "ORDER BY c.updated_at DESC LIMIT ?", CaseDAO::map, limit);
    }

    public List<Case> findOpen() {
        return queryList(SELECT + "WHERE c.status <> 'CLOSED' ORDER BY c.case_number", CaseDAO::map);
    }

    public int insert(Case c) {
        return insert("INSERT INTO cases (case_number, title, description, incident_type, priority, status, "
                        + "assigned_to, created_by) VALUES (?,?,?,?,?,?,?,?)",
                c.getCaseNumber(), c.getTitle(), c.getDescription(), c.getIncidentType(),
                c.getPriority(), c.getStatus(), c.getAssignedToId(), c.getCreatedById());
    }

    public void updateDetails(int caseId, Priority priority, CaseStatus status, Integer assignedTo) {
        update("UPDATE cases SET priority = ?, status = ?, assigned_to = ? WHERE id = ?",
                priority, status, assignedTo, caseId);
    }

    public void close(int caseId) {
        update("UPDATE cases SET status = 'CLOSED', closed_at = ? WHERE id = ?", LocalDateTime.now(), caseId);
    }

    /** Next number in the DF-YYYY-NNN sequence for the current year. */
    public String nextCaseNumber() {
        String prefix = "DF-" + Year.now() + "-";
        int count = queryInt("SELECT COUNT(*) FROM cases WHERE case_number LIKE ?", prefix + "%");
        return String.format("%s%03d", prefix, count + 1);
    }

    public int countAll() {
        return queryInt("SELECT COUNT(*) FROM cases");
    }

    public void countByStatus(Map<CaseStatus, Integer> into) {
        for (CaseStatus s : CaseStatus.values()) {
            into.put(s, 0);
        }
        queryList("SELECT status, COUNT(*) AS n FROM cases GROUP BY status", rs -> {
            into.put(CaseStatus.valueOf(rs.getString("status")), rs.getInt("n"));
            return null;
        });
    }

    private static Case map(ResultSet rs) throws SQLException {
        Case c = new Case();
        c.setId(rs.getInt("id"));
        c.setCaseNumber(rs.getString("case_number"));
        c.setTitle(rs.getString("title"));
        c.setDescription(rs.getString("description"));
        c.setIncidentType(rs.getString("incident_type"));
        c.setPriority(Priority.valueOf(rs.getString("priority")));
        c.setStatus(CaseStatus.valueOf(rs.getString("status")));
        c.setAssignedToId(nullableInt(rs, "assigned_to"));
        c.setAssignedToName(rs.getString("assigned_name"));
        c.setCreatedById(rs.getInt("created_by"));
        c.setCreatedByName(rs.getString("creator_name"));
        c.setCreatedAt(time(rs, "created_at"));
        c.setUpdatedAt(time(rs, "updated_at"));
        c.setClosedAt(time(rs, "closed_at"));
        c.setEvidenceCount(rs.getInt("evidence_count"));
        return c;
    }
}
