package za.ac.cput.adp_assignment02.dto;

import java.util.List;

/*
 * CheckoutRequest.java
 * What the Point of Sale screen sends when a sale is completed.
 * Only IDs and quantities are sent; prices and totals are worked out on the server.
 */
public record CheckoutRequest(String customerId, String employeeId, List<Line> items) {
    public record Line(String productId, int quantity) {
    }
}
