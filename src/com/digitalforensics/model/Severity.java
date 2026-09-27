package com.digitalforensics.model;

/** Severity of a finding. */
public enum Severity {
    LOW("Low"),
    MEDIUM("Medium"),
    HIGH("High"),
    CRITICAL("Critical");

    private final String label;

    Severity(String label) {
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
