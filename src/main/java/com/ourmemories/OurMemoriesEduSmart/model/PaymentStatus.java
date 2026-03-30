package com.ourmemories.OurMemoriesEduSmart.model;

public enum PaymentStatus {
    PENDING("Pending Payment", "warning", "⏳"),
    AWAITING_VERIFICATION("Awaiting Verification", "info", "📎"),
    VERIFIED("Payment Verified", "success", "✓"),
    REJECTED("Payment Rejected", "danger", "✗"),
    EXPIRED("Payment Expired", "secondary", "⏰");

    private final String displayName;
    private final String color;
    private final String icon;

    PaymentStatus(String displayName, String color, String icon) {
        this.displayName = displayName;
        this.color = color;
        this.icon = icon;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColor() {
        return color;
    }

    public String getIcon() {
        return icon;
    }

    public boolean requiresVerification() {
        return this == AWAITING_VERIFICATION;
    }

    public boolean isFinal() {
        return this == VERIFIED || this == REJECTED || this == EXPIRED;
    }
}