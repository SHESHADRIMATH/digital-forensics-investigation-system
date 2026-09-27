package com.digitalforensics.model;

/** Review state of a finding. */
public enum FindingStatus {
    OPEN("Open"),
    CONFIRMED("Confirmed"),
    DISMISSED("Dismissed");

    private final String label;

    FindingStatus(String label) {
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
