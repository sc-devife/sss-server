package com.sss.app.service.notification;

public interface NotificationCleanupService {

    /** Permanently deletes notifications past the retention window. Returns the number of rows deleted. */
    int deleteExpiredNotifications();
}
