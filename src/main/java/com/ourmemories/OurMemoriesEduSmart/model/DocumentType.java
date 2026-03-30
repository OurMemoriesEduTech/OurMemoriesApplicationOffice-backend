package com.ourmemories.OurMemoriesEduSmart.model;

// DocumentType.java - Enum for document types
public enum DocumentType {
    PROOF_OF_PAYMENT(true, "Proof of Payment"),
    APPLICANT_ID_COPY(false, "Applicant ID Copy"),
    GRADE_11_12_RESULTS(false, "Grade 11/12 Results"),
    GRADE_9_10_11_12_RESULTS(false, "Grade 9/10/11/12 Results"),
    PROOF_OF_RESIDENCE(false, "Proof of Residence");

    private final boolean triggersPaymentUpdate;
    private final String displayName;

    DocumentType(boolean triggersPaymentUpdate, String displayName) {
        this.triggersPaymentUpdate = triggersPaymentUpdate;
        this.displayName = displayName;
    }

    public boolean triggersPaymentUpdate() {
        return triggersPaymentUpdate;
    }
}