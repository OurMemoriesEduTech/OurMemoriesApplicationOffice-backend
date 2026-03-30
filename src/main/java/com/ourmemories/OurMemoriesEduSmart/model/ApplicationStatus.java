package com.ourmemories.OurMemoriesEduSmart.model;

public enum ApplicationStatus {
    PENDING_PAYMENT("Awaiting Payment", "warning", "⏳"),
    PAYMENT_VERIFIED("Payment Verified", "info", "✓"),
    UNDER_REVIEW("Under Review", "primary", "📋"),
    APPROVED("Approved", "success", "🎉"),
    REJECTED("Rejected", "danger", "✗"),
    CANCELLED("Cancelled", "secondary", "✗");

    private final String displayName;
    private final String color;
    private final String icon;

    ApplicationStatus(String displayName, String color, String icon) {
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

    public boolean isEditable() {
        return this == PENDING_PAYMENT || this == PAYMENT_VERIFIED;
    }

    public boolean isCancellable() {
        return this == PENDING_PAYMENT || this == PAYMENT_VERIFIED;
    }
}