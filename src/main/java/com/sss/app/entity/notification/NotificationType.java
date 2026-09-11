package com.sss.app.entity.notification;

public final class NotificationType {
    private NotificationType() {}

    public static final String LEAD_CREATED = "LEAD_CREATED";
    public static final String LEAD_STATUS_CHANGED = "LEAD_STATUS_CHANGED";
    public static final String LEAD_CONVERTED = "LEAD_CONVERTED";

    public static final String ESCAPE_CREATED = "ESCAPE_CREATED";
    public static final String ESCAPE_ASSIGNED = "ESCAPE_ASSIGNED";
    public static final String ESCAPE_STATUS_CHANGED = "ESCAPE_STATUS_CHANGED";
    public static final String ESCAPE_CANCELLED = "ESCAPE_CANCELLED";
    public static final String ESCAPE_TRAVEL_DATE_APPROACHING = "ESCAPE_TRAVEL_DATE_APPROACHING";

    public static final String QUOTATION_CREATED = "QUOTATION_CREATED";
    public static final String QUOTATION_SENT = "QUOTATION_SENT";
    public static final String QUOTATION_ACCEPTED = "QUOTATION_ACCEPTED";
    public static final String QUOTATION_REJECTED = "QUOTATION_REJECTED";

    public static final String PAYMENT_RECORDED = "PAYMENT_RECORDED";
    public static final String PAYMENT_VERIFIED = "PAYMENT_VERIFIED";
    public static final String PAYMENT_PARTIAL = "PAYMENT_PARTIAL";
    public static final String PAYMENT_COMPLETED = "PAYMENT_COMPLETED";

    public static final String FOLLOWUP_ASSIGNED = "FOLLOWUP_ASSIGNED";
    public static final String FOLLOWUP_DUE_SOON = "FOLLOWUP_DUE_SOON";
    public static final String FOLLOWUP_OVERDUE = "FOLLOWUP_OVERDUE";
    public static final String FOLLOWUP_COMPLETED = "FOLLOWUP_COMPLETED";

    public static final String USER_INVITED = "USER_INVITED";
    public static final String USER_ACCEPTED_INVITATION = "USER_ACCEPTED_INVITATION";
    public static final String USER_ROLE_CHANGED = "USER_ROLE_CHANGED";

    public static final class RelatedEntityType {
        private RelatedEntityType() {}
        public static final String LEAD = "LEAD";
        public static final String ESCAPE = "ESCAPE";
        public static final String QUOTE = "QUOTE";
        public static final String PAYMENT_MILESTONE = "PAYMENT_MILESTONE";
        public static final String FOLLOWUP = "FOLLOWUP";
        public static final String USER = "USER";
    }
}
