package com.digitalforensics.model;

/** Where an evidence item is in the investigation. */
public enum EvidenceStatus {
    COLLECTED("Collected"),
    IN_ANALYSIS("In Analysis"),
    ANALYZED("Analyzed"),
    ARCHIVED("Archived");

    private final String label;

    EvidenceStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /** Lower-case name used as a CSS class, e.g. "in_progress" -> badge colour. */
    public String getCss() {
        return name().toLowerCase();
    }
}
