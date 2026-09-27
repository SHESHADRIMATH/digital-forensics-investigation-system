package com.digitalforensics.model;

/** Kinds of digital evidence the system accepts. */
public enum EvidenceType {
    LOG("Log File"),
    DOCUMENT("Document"),
    IMAGE("Image"),
    SCREENSHOT("Screenshot"),
    TEXT("Text File"),
    CSV("CSV File"),
    SYSTEM_REPORT("System Report");

    private final String label;

    EvidenceType(String label) {
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
