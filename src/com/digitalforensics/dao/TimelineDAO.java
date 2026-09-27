package com.digitalforensics.dao;

import com.digitalforensics.model.TimelineEvent;
import com.digitalforensics.model.TimelineSource;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class TimelineDAO extends BaseDAO {

    public void insert(TimelineEvent ev, int createdBy) {
        insert("INSERT INTO timeline_events (case_id, event_time, title, description, source, evidence_id, created_by) "
                        + "VALUES (?,?,?,?,?,?,?)",
                ev.getCaseId(), ev.getEventTime(), ev.getTitle(), ev.getDescription(), ev.getSource(),
                ev.getEvidenceId(), createdBy);
    }

    /** Returned unsorted on purpose - TimelineService sorts in Java to demonstrate Collections. */
    public List<TimelineEvent> findByCase(int caseId) {
        return queryList("SELECT t.*, u.full_name FROM timeline_events t JOIN users u ON u.id = t.created_by "
                + "WHERE t.case_id = ?", TimelineDAO::map, caseId);
    }

    private static TimelineEvent map(ResultSet rs) throws SQLException {
        TimelineEvent t = new TimelineEvent();
        t.setId(rs.getInt("id"));
        t.setCaseId(rs.getInt("case_id"));
        t.setEventTime(time(rs, "event_time"));
        t.setTitle(rs.getString("title"));
        t.setDescription(rs.getString("description"));
        t.setSource(TimelineSource.valueOf(rs.getString("source")));
        t.setEvidenceId(nullableInt(rs, "evidence_id"));
        t.setCreatedByName(rs.getString("full_name"));
        return t;
    }
}
