package za.ac.cput.adp_assignment02.service;

/*
  ProductServiceTest.java
  Test class for ProductService
  Author: Isaac Tinotenda Ziyengwa (231269307)
 */

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import za.ac.cput.adp_assignment02.domain.Product;
import za.ac.cput.adp_assignment02.repository.ProductRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository repository;
    @Mock
    private IdGeneratorService idGenerator;
    @Mock
    private NotificationService notificationService;

    private ProductService service;

    @BeforeEach
    void setUp() {
        service = new ProductService(repository, idGenerator, notificationService);
    }

    @Test
    void createGeneratesTheIdAndChecksStock() {
        when(idGenerator.nextId(eq(IdGeneratorService.PRODUCT), any())).thenReturn("PRD-0001");
        when(repository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        Product saved = service.create(new Product.Builder().setName("  Maize Meal 10kg ").setPrice(109).setQuantity(50).build());

        assertEquals("PRD-0001", saved.getId());
        assertEquals("Maize Meal 10kg", saved.getName());
        assertEquals("General", saved.getCategory());
        verify(notificationService).checkStock(saved);
    }

    @Test
    void createRejectsMissingNameOrNegativeValues() {
        assertThrows(IllegalArgumentException.class, () -> service.create(new Product.Builder().setName(" ").setPrice(10).build()));
        assertThrows(IllegalArgumentException.class, () -> service.create(new Product.Builder().setName("Rice").setPrice(-1).build()));
        assertThrows(IllegalArgumentException.class, () -> service.create(new Product.Builder().setName("Rice").setPrice(1).setQuantity(-3).build()));
        verify(repository, never()).save(any());
    }

    @Test
    void restockAddsToTheQuantity() {
        Product rice = new Product.Builder().setId("PRD-0002").setName("Rice").setPrice(90).setQuantity(5).build();
        when(repository.findById("PRD-0002")).thenReturn(Optional.of(rice));
        when(repository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        Product saved = service.restock("PRD-0002", 20);

        assertEquals(25, saved.getQuantity());
    }

    @Test
    void restockRejectsZeroAndUnknownProducts() {
        assertThrows(IllegalArgumentException.class, () -> service.restock("PRD-0002", 0));
        when(repository.findById("NOPE")).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> service.restock("NOPE", 5));
    }

    @Test
    void updateOfUnknownProductFails() {
        when(repository.existsById("PRD-0404")).thenReturn(false);
        Product missing = new Product.Builder().setId("PRD-0404").setName("Ghost").build();
        assertThrows(NoSuchElementException.class, () -> service.update(missing));
    }

    @Test
    void lowStockOnlyReturnsProductsAtOrBelowTheirReorderLevel() {
        Product low = new Product.Builder().setId("A").setName("Low").setQuantity(3).setReorderLevel(10).build();
        Product fine = new Product.Builder().setId("B").setName("Fine").setQuantity(80).setReorderLevel(10).build();
        when(repository.findAll()).thenReturn(List.of(low, fine));

        List<Product> result = service.getLowStock();

        assertEquals(1, result.size());
        assertEquals("A", result.get(0).getId());
    }

    @Test
    void deleteReturnsFalseWhenTheProductDoesNotExist() {
        when(repository.existsById("PRD-0099")).thenReturn(false);
        assertFalse(service.delete("PRD-0099"));
    }
}
