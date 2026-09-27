package com.digitalforensics.model;

/** Case life-cycle: OPEN -> IN_PROGRESS -> (ON_HOLD) -> CLOSED. */
public enum CaseStatus {
    OPEN("Open"),
    IN_PROGRESS("In Progress"),
    ON_HOLD("On Hold"),
    CLOSED("Closed");

    private final String label;

    CaseStatus(String label) {
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
