package com.digitalforensics.service;

import com.digitalforensics.dao.CaseDAO;
import com.digitalforensics.dao.EvidenceDAO;
import com.digitalforensics.dao.FindingDAO;
import com.digitalforensics.model.CaseStatus;
import com.digitalforensics.model.DashboardStats;
import com.digitalforensics.model.HashCheck;
import com.digitalforensics.model.IntegrityStatus;
import com.digitalforensics.model.Reportable;

import java.util.List;

/** Report and dashboard data, plus CSV export. */
public class ReportService {

    /** The report types offered on the Reports page. */
    public enum ReportType {
        SUMMARY("Investigation Summary", "Case details, evidence, findings and key timeline events"),
        EVIDENCE("Evidence Report", "Every evidence item with its SHA-256 hash and status"),
        HASH("Hash Verification Report", "History of every integrity check"),
        CUSTODY("Chain-of-Custody Report", "Who handled each evidence item, and when"),
        TIMELINE("Timeline Report", "All events in chronological order"),
        FINDINGS("Findings Report", "Findings grouped by severity");

        private final String label;
        private final String description;

        ReportType(String label, String description) {
            this.label = label;
            this.description = description;
        }

        public String getLabel() { return label; }
        public String getDescription() { return description; }
        public String getName() { return name(); }
    }

    private final CaseDAO caseDAO = new CaseDAO();
    private final EvidenceDAO evidenceDAO = new EvidenceDAO();
    private final FindingDAO findingDAO = new FindingDAO();

    public DashboardStats dashboardStats() {
        DashboardStats s = new DashboardStats();
        caseDAO.countByStatus(s.getCasesByStatus());
        s.setTotalCases(caseDAO.countAll());
        s.setActiveCases(s.getTotalCases() - s.getCasesByStatus().getOrDefault(CaseStatus.CLOSED, 0));
        s.setTotalEvidence(evidenceDAO.countAll());
        s.setVerifiedEvidence(evidenceDAO.countByIntegrity(IntegrityStatus.VERIFIED));
        s.setModifiedEvidence(evidenceDAO.countByIntegrity(IntegrityStatus.MODIFIED));
        s.setOpenFindings(findingDAO.countOpen());
        return s;
    }

    /**
     * Builds CSV text from any list of Reportable objects (polymorphism:
     * the same method works for Evidence, CustodyRecord, TimelineEvent, Finding, HashCheck).
     */
    public String toCsv(List<? extends Reportable> rows, String[] headerIfEmpty) {
        StringBuilder sb = new StringBuilder();
        String[] header = rows.isEmpty() ? headerIfEmpty : rows.get(0).csvHeader();
        appendRow(sb, header);
        for (Reportable r : rows) {
            appendRow(sb, r.toCsvRow());
        }
        return sb.toString();
    }

    private void appendRow(StringBuilder sb, String[] cells) {
        for (int i = 0; i < cells.length; i++) {
            if (i > 0) sb.append(',');
            sb.append(escape(cells[i]));
        }
        sb.append("\r\n");
    }

    /**
     * Quotes a CSV cell. Cells starting with = + - @ are prefixed with ' so Excel
     * does not run them as formulas (CSV injection).
     */
    private String escape(String value) {
        if (value == null) return "";
        String v = value;
        if (!v.isEmpty() && "=+-@".indexOf(v.charAt(0)) >= 0) {
            v = "'" + v;
        }
        if (v.contains(",") || v.contains("\"") || v.contains("\n") || v.contains("\r")) {
            v = "\"" + v.replace("\"", "\"\"") + "\"";
        }
        return v;
    }

    public List<HashCheck> hashChecksForCase(int caseId) {
        return evidenceDAO.findHashChecksByCase(caseId);
    }
}
