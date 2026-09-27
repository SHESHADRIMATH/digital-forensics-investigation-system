package com.digitalforensics.model;

import com.digitalforensics.util.DateUtil;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * One event on an investigation timeline.
 * Implements Comparable so a List of events can be sorted chronologically with Collections.sort().
 */
public class TimelineEvent extends BaseEntity implements Reportable, Comparable<TimelineEvent> {

    private int caseId;
    private LocalDateTime eventTime;
    private String title;
    private String description;
    private TimelineSource source = TimelineSource.MANUAL;
    private Integer evidenceId;
    private String createdByName;

    @Override
    public String getDisplayCode() {
        return "EVT-" + id;
    }

    @Override
    public int compareTo(TimelineEvent other) {
        return eventTime.compareTo(other.eventTime);
    }

    public String getEvidenceCode() {
        return evidenceId == null ? "" : String.format("EV-%04d", evidenceId);
    }

    @Override
    public String[] csvHeader() {
        return new String[]{"Time", "Event", "Details", "Source", "Evidence", "Added By"};
    }

    @Override
    public String[] toCsvRow() {
        return new String[]{getEventTimeText(), title, description == null ? "" : description,
                source.getLabel(), getEvidenceCode(), createdByName};
    }

    public int getCaseId() { return caseId; }
    public void setCaseId(int caseId) { this.caseId = caseId; }
    public LocalDateTime getEventTime() { return eventTime; }
    public void setEventTime(LocalDateTime eventTime) { this.eventTime = eventTime; }
    public String getEventTimeText() { return DateUtil.formatSeconds(eventTime); }
    public String getEventDate() { return eventTime == null ? "" : eventTime.toLocalDate().toString(); }
    public String getEventClock() { return eventTime == null ? "" : eventTime.format(DateTimeFormatter.ofPattern("HH:mm:ss")); }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public TimelineSource getSource() { return source; }
    public void setSource(TimelineSource source) { this.source = source; }
    public Integer getEvidenceId() { return evidenceId; }
    public void setEvidenceId(Integer evidenceId) { this.evidenceId = evidenceId; }
    public String getCreatedByName() { return createdByName; }
    public void setCreatedByName(String createdByName) { this.createdByName = createdByName; }
}
