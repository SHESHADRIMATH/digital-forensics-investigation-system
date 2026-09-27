package com.digitalforensics.model;

import com.digitalforensics.util.DateUtil;

import java.time.LocalDateTime;

/** Result of one integrity verification (re-hash and compare). */
public class HashCheck extends BaseEntity implements Reportable {

    private int evidenceId;
    private String computedHash;
    private IntegrityStatus result;
    private String checkedByName;
    private LocalDateTime checkedAt;

    @Override
    public String getDisplayCode() {
        return "HC-" + id;
    }

    public String getEvidenceCode() {
        return String.format("EV-%04d", evidenceId);
    }

    @Override
    public String[] csvHeader() {
        return new String[]{"Checked At", "Evidence", "Computed SHA-256", "Result", "Checked By"};
    }

    @Override
    public String[] toCsvRow() {
        return new String[]{getCheckedAtText(), getEvidenceCode(), computedHash,
                result.getLabel(), checkedByName};
    }

    public int getEvidenceId() { return evidenceId; }
    public void setEvidenceId(int evidenceId) { this.evidenceId = evidenceId; }
    public String getComputedHash() { return computedHash; }
    public void setComputedHash(String computedHash) { this.computedHash = computedHash; }
    public IntegrityStatus getResult() { return result; }
    public void setResult(IntegrityStatus result) { this.result = result; }
    public String getCheckedByName() { return checkedByName; }
    public void setCheckedByName(String checkedByName) { this.checkedByName = checkedByName; }
    public LocalDateTime getCheckedAt() { return checkedAt; }
    public void setCheckedAt(LocalDateTime checkedAt) { this.checkedAt = checkedAt; }
    public String getCheckedAtText() { return DateUtil.formatSeconds(checkedAt); }
}
