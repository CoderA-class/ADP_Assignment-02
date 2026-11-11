package za.ac.cput.adp_assignment02.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import za.ac.cput.adp_assignment02.domain.Product;
import za.ac.cput.adp_assignment02.domain.ProductCategory;
import za.ac.cput.adp_assignment02.repository.ProductCategoryRepository;
import za.ac.cput.adp_assignment02.repository.ProductRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/*
 * ProductCategoryServiceTest.java
 * Admin-managed stock categories.
 */
@ExtendWith(MockitoExtension.class)
class ProductCategoryServiceTest {

    @Mock private ProductCategoryRepository repository;
    @Mock private ProductRepository productRepository;

    private ProductCategoryService service;

    @BeforeEach
    void setUp() {
        service = new ProductCategoryService(repository, productRepository);
    }

    @Test
    void creatingACategoryTidiesTheNameAndChecksForDuplicates() {
        when(repository.findByNameIgnoreCase("Electronics")).thenReturn(Optional.empty());
        when(repository.save(any(ProductCategory.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductCategory saved = service.create("  Electronics ", "mobile", "#2563B8");

        assertEquals("Electronics", saved.getName());
        assertEquals("mobile", saved.getIcon());
        assertEquals("#2563b8", saved.getColor());

        when(repository.findByNameIgnoreCase("Electronics")).thenReturn(Optional.of(saved));
        assertThrows(IllegalStateException.class, () -> service.create("Electronics", null, null));
        assertThrows(IllegalArgumentException.class, () -> service.create("   ", null, null));
    }

    @Test
    void aCategoryInUseCannotBeDeleted() {
        ProductCategory dairy = new ProductCategory("Dairy", null, null);
        when(repository.findById(1L)).thenReturn(Optional.of(dairy));
        when(productRepository.findByCategoryIgnoreCase("Dairy")).thenReturn(List.of(new Product.Builder().setId("PRD-0001").setName("Milk").setCategory("Dairy").build()));

        assertThrows(IllegalStateException.class, () -> service.delete(1L));
        verify(repository, never()).delete(any());
    }

    @Test
    void renamingMovesTheProductsToTheNewName() {
        ProductCategory drinks = new ProductCategory("Drinks", null, null);
        Product cola = new Product.Builder().setId("PRD-0005").setName("Cola").setCategory("Drinks").build();
        when(repository.findById(2L)).thenReturn(Optional.of(drinks));
        when(repository.findByNameIgnoreCase("Beverages")).thenReturn(Optional.empty());
        when(productRepository.findByCategoryIgnoreCase("Drinks")).thenReturn(List.of(cola));
        when(repository.save(any(ProductCategory.class))).thenAnswer(inv -> inv.getArgument(0));

        service.update(2L, "Beverages", null, null);

        assertEquals("Beverages", cola.getCategory());
        verify(productRepository).save(cola);
    }
}
