package za.ac.cput.adp_assignment02.event;

import za.ac.cput.adp_assignment02.domain.Product;

/*
 * StockLevelChangedEvent.java
 * Published whenever a product's stock level has been checked after a change
 * (new product, edit, restock or sale), so open stock alerts can close themselves.
 */
public record StockLevelChangedEvent(Product product) {
}
