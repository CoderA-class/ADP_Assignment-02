package za.ac.cput.adp_assignment02.factory;

/*
PaymentFactory.java
Payment Factory class
Author: David Daniel Sepkitt (240046935)
Date: 11 November 2026
*/


import za.ac.cput.adp_assignment02.domain.Order;
import za.ac.cput.adp_assignment02.domain.Payment;

public class PaymentFactory {
    /* Money received against an account (pay-later) order: CASH, CARD or EFT */
    public static Payment createPayment(Order order, double amount, String method, String receivedBy, String note) {
        if (order == null || order.getOrderId() == null || amount <= 0) {
            return null;
        }
        String how = method == null || method.isBlank() ? "CASH" : method.trim().toUpperCase();
        double rounded = Math.round(amount * 100.0) / 100.0;
        return new Payment(order, rounded, how, receivedBy, note == null || note.isBlank() ? null : note.trim());
    }
}
