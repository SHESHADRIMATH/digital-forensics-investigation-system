package com.digitalforensics.model;

/** Where a timeline event came from. */
public enum TimelineSource {
    MANUAL("Manual"),
    LOG("Log Analysis"),
    SYSTEM("System");

    private final String label;

    TimelineSource(String label) {
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
