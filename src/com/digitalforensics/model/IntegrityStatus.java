package com.digitalforensics.model;

/** Result of the latest SHA-256 check. */
public enum IntegrityStatus {
    NOT_VERIFIED("Not Verified"),
    VERIFIED("Verified"),
    MODIFIED("Modified");

    private final String label;

    IntegrityStatus(String label) {
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
