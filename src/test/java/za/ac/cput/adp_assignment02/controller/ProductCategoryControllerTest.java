package za.ac.cput.adp_assignment02.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import za.ac.cput.adp_assignment02.domain.ProductCategory;
import za.ac.cput.adp_assignment02.service.ProductCategoryService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/*
 * ProductCategoryControllerTest.java
 * Test class for ProductCategoryController
 * Author: David Daniel Sepkitt (240046935)
 * Date: 11 November 2026
 */
@ExtendWith(MockitoExtension.class)
class ProductCategoryControllerTest {

    @Mock private ProductCategoryService service;
    private ProductCategoryController controller;

    @BeforeEach
    void setUp() {
        controller = new ProductCategoryController(service);
    }

    @Test
    void createPassesNameIconAndColour() {
        ProductCategory electronics = new ProductCategory("Electronics", "mobile", "#2563b8");
        when(service.create("Electronics", "mobile", "#2563b8")).thenReturn(electronics);
        assertEquals("Electronics", controller.create(electronics).getName());
    }

    @Test
    void listAndDeleteDelegateToTheService() {
        when(service.getAll()).thenReturn(List.of(new ProductCategory("Bakery", null, null)));
        when(service.delete(1L)).thenReturn(true);
        assertEquals(1, controller.list().size());
        assertTrue(controller.delete(1L));
    }
}
