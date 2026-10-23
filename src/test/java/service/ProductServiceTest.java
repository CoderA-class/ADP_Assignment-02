package za.ac.cput.adp_assignment02.service;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import za.ac.cput.adp_assignment02.domain.Product;
import za.ac.cput.adp_assignment02.factory.ProductFactory;
import za.ac.cput.adp_assignment02.repository.ProductRepository;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class ProductServiceTest {
    @Autowired
    private ProductRepository repository;

    private ProductService service;

    @BeforeEach
    void setUp() {
        service = new ProductService(repository);
    }

    private Product product() {
        return ProductFactory.createProduct("P-1", "Milk", 18.50, 12, "Dairy");
    }

    @Test
    void createsProductThroughRepository() {
        Product product = product();

        assertEquals(product.getId(), service.create(product).getId());
        assertTrue(repository.existsById(product.getId()));
    }

    @Test
    void readsProductById() {
        Product product = product();
        repository.save(product);

        assertEquals(product.getName(), service.read("P-1").getName());
    }

    @Test
    void returnsNullForUnknownProduct() {
        assertFalse(repository.existsById("missing"));
        assertNull(service.read("missing"));
    }

    @Test
    void getsAllProducts() {
        List<Product> products = List.of(product());
        repository.saveAll(products);

        assertEquals(1, service.getAll().size());
        assertEquals(products.get(0).getId(), service.getAll().get(0).getId());
    }

    @Test
    void deletesExistingProduct() {
        repository.save(product());

        assertTrue(service.delete("P-1"));
        assertFalse(repository.existsById("P-1"));
    }
}