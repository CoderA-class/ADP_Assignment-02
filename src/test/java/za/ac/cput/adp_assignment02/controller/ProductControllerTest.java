package za.ac.cput.adp_assignment02.controller;

/*
  ProductControllerTest.java
  Test class for ProductController
  Author: Isaac Tinotenda Ziyengwa (231269307)
 */

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import za.ac.cput.adp_assignment02.domain.Product;
import za.ac.cput.adp_assignment02.service.ProductService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    private ProductService service;

    private ProductController controller;
    private Product laptop;

    @BeforeEach
    void setUp() {
        controller = new ProductController(service);
        laptop = new Product.Builder().setId("PRD-0001").setName("Laptop").setPrice(1500.00).setQuantity(10).setCategory("Electronics").build();
    }

    @Test
    void createDelegatesToTheService() {
        Product input = new Product.Builder().setName("Laptop").setPrice(1500.00).setQuantity(10).build();
        when(service.create(input)).thenReturn(laptop);
        assertEquals("PRD-0001", controller.create(input).getId());
    }

    @Test
    void readReturns200WhenFound() {
        when(service.read("PRD-0001")).thenReturn(laptop);
        ResponseEntity<Product> response = controller.read("PRD-0001");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Laptop", response.getBody().getName());
    }

    @Test
    void readReturns404WhenMissing() {
        when(service.read("NONEXISTENT")).thenReturn(null);
        assertEquals(HttpStatus.NOT_FOUND, controller.read("NONEXISTENT").getStatusCode());
    }

    @Test
    void getAllReturnsEveryProduct() {
        when(service.getAll()).thenReturn(List.of(laptop));
        assertEquals(1, controller.getAll().size());
    }

    @Test
    void deleteAndRestockDelegate() {
        when(service.delete("PRD-0001")).thenReturn(true);
        when(service.restock("PRD-0001", 5)).thenReturn(laptop);
        assertTrue(controller.delete("PRD-0001"));
        assertSame(laptop, controller.restock("PRD-0001", 5));
    }
}
