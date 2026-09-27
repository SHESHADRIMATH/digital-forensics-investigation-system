package com.digitalforensics.model;

import java.util.EnumMap;
import java.util.Map;

/** Numbers shown on the dashboard cards. */
public class DashboardStats {

    private int totalCases;
    private int activeCases;
    private int totalEvidence;
    private int verifiedEvidence;
    private int modifiedEvidence;
    private int openFindings;
    private final Map<CaseStatus, Integer> casesByStatus = new EnumMap<>(CaseStatus.class);

    public int getTotalCases() { return totalCases; }
    public void setTotalCases(int totalCases) { this.totalCases = totalCases; }
    public int getActiveCases() { return activeCases; }
    public void setActiveCases(int activeCases) { this.activeCases = activeCases; }
    public int getTotalEvidence() { return totalEvidence; }
    public void setTotalEvidence(int totalEvidence) { this.totalEvidence = totalEvidence; }
    public int getVerifiedEvidence() { return verifiedEvidence; }
    public void setVerifiedEvidence(int verifiedEvidence) { this.verifiedEvidence = verifiedEvidence; }
    public int getModifiedEvidence() { return modifiedEvidence; }
    public void setModifiedEvidence(int modifiedEvidence) { this.modifiedEvidence = modifiedEvidence; }
    public int getOpenFindings() { return openFindings; }
    public void setOpenFindings(int openFindings) { this.openFindings = openFindings; }
    public Map<CaseStatus, Integer> getCasesByStatus() { return casesByStatus; }

    /** Percentage helper for the status bars (avoids divide-by-zero in the JSP). */
    public int percentOfCases(int count) {
        return totalCases == 0 ? 0 : Math.round(count * 100f / totalCases);
    }
}
