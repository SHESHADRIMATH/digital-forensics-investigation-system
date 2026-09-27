package com.digitalforensics.model;

import com.digitalforensics.util.DateUtil;
import com.digitalforensics.util.FileUtil;

import java.time.LocalDateTime;

/** One piece of digital evidence. Metadata lives in the "evidence" table; the file lives on disk. */
public class Evidence extends BaseEntity implements Reportable {

    private int caseId;
    private String caseNumber;
    private String fileName;
    private String storedName;
    private EvidenceType evidenceType;
    private long fileSize;
    private String sha256Original;
    private IntegrityStatus integrity = IntegrityStatus.NOT_VERIFIED;
    private LocalDateTime lastVerifiedAt;
    private EvidenceStatus status = EvidenceStatus.COLLECTED;
    private String description;
    private int collectedById;
    private String collectedByName;
    private LocalDateTime collectedAt;
    private LocalDateTime uploadedAt;

    @Override
    public String getDisplayCode() {
        return String.format("EV-%04d", id);
    }

    /** Only plain-text files can be opened in Log Analysis. */
    public boolean isTextReadable() {
        return FileUtil.TEXT_EXTENSIONS.contains(FileUtil.extension(fileName));
    }

    @Override
    public String[] csvHeader() {
        return new String[]{"Evidence ID", "Case", "File Name", "Type", "Size (bytes)", "SHA-256",
                "Integrity", "Status", "Collected By", "Collected At"};
    }

    @Override
    public String[] toCsvRow() {
        return new String[]{getDisplayCode(), caseNumber, fileName, evidenceType.getLabel(),
                String.valueOf(fileSize), sha256Original, integrity.getLabel(), status.getLabel(),
                collectedByName, getCollectedAtText()};
    }

    public int getCaseId() { return caseId; }
    public void setCaseId(int caseId) { this.caseId = caseId; }
    public String getCaseNumber() { return caseNumber; }
    public void setCaseNumber(String caseNumber) { this.caseNumber = caseNumber; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getStoredName() { return storedName; }
    public void setStoredName(String storedName) { this.storedName = storedName; }
    public EvidenceType getEvidenceType() { return evidenceType; }
    public void setEvidenceType(EvidenceType evidenceType) { this.evidenceType = evidenceType; }
    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }
    public String getFileSizeText() { return FileUtil.humanSize(fileSize); }
    public String getSha256Original() { return sha256Original; }
    public void setSha256Original(String sha256Original) { this.sha256Original = sha256Original; }
    public IntegrityStatus getIntegrity() { return integrity; }
    public void setIntegrity(IntegrityStatus integrity) { this.integrity = integrity; }
    public LocalDateTime getLastVerifiedAt() { return lastVerifiedAt; }
    public void setLastVerifiedAt(LocalDateTime lastVerifiedAt) { this.lastVerifiedAt = lastVerifiedAt; }
    public String getLastVerifiedAtText() { return DateUtil.format(lastVerifiedAt); }
    public EvidenceStatus getStatus() { return status; }
    public void setStatus(EvidenceStatus status) { this.status = status; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getCollectedById() { return collectedById; }
    public void setCollectedById(int collectedById) { this.collectedById = collectedById; }
    public String getCollectedByName() { return collectedByName; }
    public void setCollectedByName(String collectedByName) { this.collectedByName = collectedByName; }
    public LocalDateTime getCollectedAt() { return collectedAt; }
    public void setCollectedAt(LocalDateTime collectedAt) { this.collectedAt = collectedAt; }
    public String getCollectedAtText() { return DateUtil.format(collectedAt); }
    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
    public String getUploadedAtText() { return DateUtil.format(uploadedAt); }
}
