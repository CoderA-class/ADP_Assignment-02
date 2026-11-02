package za.ac.cput.adp_assignment02.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ProductTest {
    @Test
    void builderCreatesProductWithAllFields() {
        Product product = new Product.Builder().setId("P-1").setName("Milk")
                .setPrice(18.50).setQuantity(12).setCategory("Dairy").build();

        assertEquals("P-1", product.getId());
        assertEquals("Milk", product.getName());
        assertEquals(18.50, product.getPrice());
        assertEquals(12, product.getQuantity());
        assertEquals("Dairy", product.getCategory());
    }
}