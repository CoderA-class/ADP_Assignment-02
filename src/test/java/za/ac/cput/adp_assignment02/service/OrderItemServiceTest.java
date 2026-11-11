package za.ac.cput.adp_assignment02.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import za.ac.cput.adp_assignment02.domain.OrderItem;
import za.ac.cput.adp_assignment02.domain.Product;
import za.ac.cput.adp_assignment02.repository.OrderItemRepository;
import za.ac.cput.adp_assignment02.repository.ProductRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/*
 * OrderItemServiceTest.java
 * Order lines pick up the product's name and price and work out their own line total.
 * Author: David Daniel Sepkitt (240046935)
 * Date: 11 November 2026
 */
@ExtendWith(MockitoExtension.class)
class OrderItemServiceTest {

    @Mock private OrderItemRepository repository;
    @Mock private ProductRepository productRepository;
    @Mock private IdGeneratorService idGenerator;

    private OrderItemService service;

    @BeforeEach
    void setUp() {
        service = new OrderItemService(repository, productRepository, idGenerator);
    }

    @Test
    void createFillsInTheProductAndLineTotal() {
        Product rice = new Product.Builder().setId("PRD-0008").setName("Long Grain Rice 10kg").setPrice(189.99).setQuantity(30).build();
        when(idGenerator.nextId(eq(IdGeneratorService.ORDER_ITEM), any())).thenReturn("ITM-0001");
        when(productRepository.findById("PRD-0008")).thenReturn(Optional.of(rice));
        when(repository.save(any(OrderItem.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderItem line = new OrderItem.OrderItemBuilder().setOrderId("ORD-0001").setProductId("PRD-0008").setQuantity(2).build();
        OrderItem saved = service.create(line);

        assertEquals("ITM-0001", saved.getOrderItemId());
        assertEquals("Long Grain Rice 10kg", saved.getProductName());
        assertEquals(379.98, saved.getLineTotal(), 0.001);
    }

    @Test
    void aLineNeedsAnOrderAProductAndAQuantity() {
        assertThrows(IllegalArgumentException.class, () -> service.create(new OrderItem.OrderItemBuilder().setOrderId("ORD-0001").setQuantity(1).build()));
        assertThrows(IllegalArgumentException.class, () -> service.create(new OrderItem.OrderItemBuilder().setOrderId("ORD-0001").setProductId("PRD-0001").setQuantity(0).build()));
    }
}
