package za.ac.cput.adp_assignment02.dto;

import java.util.List;

/*
 * CheckoutRequest.java
 * What the Point of Sale screen sends when a sale is completed.
 * Only IDs and quantities are sent; prices and totals are worked out on the server.
 */
/* paymentMethod: CASH, CARD, EFT or ACCOUNT (pay later). Empty means cash. */
public record CheckoutRequest(String customerId, String employeeId, List<Line> items, String paymentMethod) {

    public CheckoutRequest(String customerId, String employeeId, List<Line> items) {
        this(customerId, employeeId, items, null);
    }

    public record Line(String productId, int quantity) {
    }
}
