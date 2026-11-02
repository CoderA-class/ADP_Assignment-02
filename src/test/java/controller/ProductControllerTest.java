package za.ac.cput.adp_assignment02.controller;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import za.ac.cput.adp_assignment02.domain.Product;
import za.ac.cput.adp_assignment02.factory.ProductFactory;
import za.ac.cput.adp_assignment02.repository.ProductRepository;
import za.ac.cput.adp_assignment02.service.ProductService;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class ProductControllerTest {
    @Autowired
    private ProductRepository repository;

    private ProductController controller;

    @BeforeEach
    void setUp() {
        controller = new ProductController(new ProductService(repository));
    }

    private Product product() {
        return ProductFactory.createProduct("P-1", "Milk", 18.50, 12, "Dairy");
    }

    @Test
    void createDelegatesToService() {
        Product product = product();

        assertEquals(product.getId(), controller.create(product).getId());
        assertTrue(repository.existsById(product.getId()));
    }

    @Test
    void getAllDelegatesToService() {
        List<Product> products = List.of(product());
        repository.saveAll(products);

        assertEquals(1, controller.getAll().size());
        assertEquals(products.get(0).getId(), controller.getAll().get(0).getId());
    }

    @Test
    void readDelegatesToService() {
        Product product = product();
        repository.save(product);

        assertEquals(product.getName(), controller.read("P-1").getName());
    }

    @Test
    void deleteDelegatesToService() {
        repository.save(product());

        assertTrue(controller.delete("P-1"));
        assertTrue(repository.findById("P-1").isEmpty());
    }
}