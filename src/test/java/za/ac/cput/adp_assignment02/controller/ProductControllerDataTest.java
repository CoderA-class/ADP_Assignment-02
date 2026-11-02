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
import za.ac.cput.adp_assignment02.repository.CustomerRepository;
import za.ac.cput.adp_assignment02.repository.IdSequenceRepository;
import za.ac.cput.adp_assignment02.repository.NotificationRepository;
import za.ac.cput.adp_assignment02.service.IdGeneratorService;
import za.ac.cput.adp_assignment02.service.NotificationService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/*
 * Team test (moved from src/test/java/controller): ProductController with a real H2 repository.
 */
@DataJpaTest
class ProductControllerDataTest {
    @Autowired
    private ProductRepository repository;
    @Autowired
    private IdSequenceRepository idSequenceRepository;
    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private CustomerRepository customerRepository;

    private ProductController controller;

    @BeforeEach
    void setUp() {
        controller = new ProductController(new ProductService(repository, new IdGeneratorService(idSequenceRepository),
                new NotificationService(notificationRepository, repository, customerRepository, 30)));
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

        assertEquals(product.getName(), controller.read("P-1").getBody().getName());
    }

    @Test
    void deleteDelegatesToService() {
        repository.save(product());

        assertTrue(controller.delete("P-1"));
        assertTrue(repository.findById("P-1").isEmpty());
    }
}