package za.ac.cput.adp_assignment02.factory;

/*
StockAlertFactory.java
StockAlert Factory class
Author: Adriaan van der Westhuizen (240436415)
Date: 11 November 2026
*/


import za.ac.cput.adp_assignment02.domain.Employee;
import za.ac.cput.adp_assignment02.domain.Product;
import za.ac.cput.adp_assignment02.domain.StockAlert;

public class StockAlertFactory {
    /* An employee tells the admins that a product needs re-ordering; it starts as OPEN */
    public static StockAlert createStockAlert(Product product, Employee raisedBy, String note) {
        if (product == null || product.getId() == null || raisedBy == null || raisedBy.getEmpID() == null) {
            return null;
        }
        String cleanNote = note == null || note.isBlank() ? null : note.trim();
        return new StockAlert(product, raisedBy, cleanNote);
    }
}
