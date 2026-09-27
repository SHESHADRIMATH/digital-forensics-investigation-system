package com.digitalforensics.model;

/**
 * User roles and what each is allowed to do (role-based access control).
 * Keeping the rules here means servlets and JSPs ask one place, e.g. role.canCloseCase().
 */
public enum Role {
    ADMIN("Administrator"),
    INVESTIGATOR("Forensic Investigator"),
    ANALYST("Analyst");

    private final String label;

    Role(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public boolean canCreateCase()      { return this == ADMIN || this == INVESTIGATOR; }
    public boolean canUpdateCase()      { return this == ADMIN || this == INVESTIGATOR; }
    public boolean canAssignCase()      { return this == ADMIN; }
    public boolean canCloseCase()       { return this == ADMIN; }
    public boolean canUploadEvidence()  { return this == ADMIN || this == INVESTIGATOR; }
    public boolean canRecordCustody()   { return this == ADMIN || this == INVESTIGATOR; }
    public boolean canReviewFindings()  { return this == ADMIN || this == INVESTIGATOR; }
}
