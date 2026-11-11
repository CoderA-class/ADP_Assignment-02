package za.ac.cput.adp_assignment02.factory;

/*
NotificationFactory.java
Notification Factory class
Author: David Daniel Sepkitt (240046935)
Date: 11 November 2026
*/


import za.ac.cput.adp_assignment02.domain.Notification;

public class NotificationFactory {
    public static Notification createNotification(Notification.Type type, String title, String message, String refId, String audience) {
        if (type == null || title == null || title.isBlank()) {
            return null;
        }
        return new Notification(type, title, message, refId, audience == null || audience.isBlank() ? Notification.AUDIENCE_ADMIN : audience);
    }

    /* Sent to every admin */
    public static Notification forAdmins(Notification.Type type, String title, String message, String refId) {
        return createNotification(type, title, message, refId, Notification.AUDIENCE_ADMIN);
    }

    /* Sent to one employee, e.g. "your leave was approved" */
    public static Notification forEmployee(String employeeId, Notification.Type type, String title, String message, String refId) {
        return createNotification(type, title, message, refId, employeeId);
    }
}
