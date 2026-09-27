package com.digitalforensics.model;

import com.digitalforensics.util.DateUtil;

import java.time.LocalDateTime;

/** A conclusion drawn by an investigator, optionally linked to a piece of evidence. */
public class Finding extends BaseEntity implements Reportable {

    private int caseId;
    private String caseNumber;
    private Integer evidenceId;
    private String evidenceFileName;
    private String title;
    private String description;
    private Severity severity;
    private FindingStatus status = FindingStatus.OPEN;
    private String createdByName;
    private LocalDateTime createdAt;

    @Override
    public String getDisplayCode() {
        return "FND-" + id;
    }

    public String getEvidenceCode() {
        return evidenceId == null ? "" : String.format("EV-%04d", evidenceId);
    }

    @Override
    public String[] csvHeader() {
        return new String[]{"Finding", "Title", "Severity", "Status", "Evidence", "Description", "Recorded By", "Recorded At"};
    }

    @Override
    public String[] toCsvRow() {
        return new String[]{getDisplayCode(), title, severity.getLabel(), status.getLabel(), getEvidenceCode(),
                description == null ? "" : description, createdByName, getCreatedAtText()};
    }

    public int getCaseId() { return caseId; }
    public void setCaseId(int caseId) { this.caseId = caseId; }
    public String getCaseNumber() { return caseNumber; }
    public void setCaseNumber(String caseNumber) { this.caseNumber = caseNumber; }
    public Integer getEvidenceId() { return evidenceId; }
    public void setEvidenceId(Integer evidenceId) { this.evidenceId = evidenceId; }
    public String getEvidenceFileName() { return evidenceFileName; }
    public void setEvidenceFileName(String evidenceFileName) { this.evidenceFileName = evidenceFileName; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Severity getSeverity() { return severity; }
    public void setSeverity(Severity severity) { this.severity = severity; }
    public FindingStatus getStatus() { return status; }
    public void setStatus(FindingStatus status) { this.status = status; }
    public String getCreatedByName() { return createdByName; }
    public void setCreatedByName(String createdByName) { this.createdByName = createdByName; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getCreatedAtText() { return DateUtil.format(createdAt); }
}
