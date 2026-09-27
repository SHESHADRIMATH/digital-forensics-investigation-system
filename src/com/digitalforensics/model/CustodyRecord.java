package com.digitalforensics.model;

import com.digitalforensics.util.DateUtil;

import java.time.LocalDateTime;

/** One entry in the chain of custody: who did what to which evidence, and when. */
public class CustodyRecord extends BaseEntity implements Reportable {

    private int evidenceId;
    private String evidenceFileName;
    private String caseNumber;
    private String action;
    private int performedById;
    private String performedByName;
    private Integer handedToId;
    private String handedToName;
    private String notes;
    private LocalDateTime actionTime;

    @Override
    public String getDisplayCode() {
        return "COC-" + id;
    }

    public String getEvidenceCode() {
        return String.format("EV-%04d", evidenceId);
    }

    @Override
    public String[] csvHeader() {
        return new String[]{"Time", "Evidence", "Action", "Performed By", "Handed To", "Notes"};
    }

    @Override
    public String[] toCsvRow() {
        return new String[]{getActionTimeText(), getEvidenceCode(), action, performedByName,
                handedToName == null ? "" : handedToName, notes == null ? "" : notes};
    }

    public int getEvidenceId() { return evidenceId; }
    public void setEvidenceId(int evidenceId) { this.evidenceId = evidenceId; }
    public String getEvidenceFileName() { return evidenceFileName; }
    public void setEvidenceFileName(String evidenceFileName) { this.evidenceFileName = evidenceFileName; }
    public String getCaseNumber() { return caseNumber; }
    public void setCaseNumber(String caseNumber) { this.caseNumber = caseNumber; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public int getPerformedById() { return performedById; }
    public void setPerformedById(int performedById) { this.performedById = performedById; }
    public String getPerformedByName() { return performedByName; }
    public void setPerformedByName(String performedByName) { this.performedByName = performedByName; }
    public Integer getHandedToId() { return handedToId; }
    public void setHandedToId(Integer handedToId) { this.handedToId = handedToId; }
    public String getHandedToName() { return handedToName; }
    public void setHandedToName(String handedToName) { this.handedToName = handedToName; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public LocalDateTime getActionTime() { return actionTime; }
    public void setActionTime(LocalDateTime actionTime) { this.actionTime = actionTime; }
    public String getActionTimeText() { return DateUtil.formatSeconds(actionTime); }
}
