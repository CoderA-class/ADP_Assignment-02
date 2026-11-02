package za.ac.cput.adp_assignment02.service;

/*
OrderService.java
Order Service Implementation
Author: Rocco Given Visagie (220343527)
Date: 10 August 2026
Updated: checkout() handles a complete sale from the Point of Sale screen: it generates
         the order and item IDs, looks up prices, checks and reduces stock, works out the
         totals and raises notifications.
*/

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.ac.cput.adp_assignment02.domain.Customer;
import za.ac.cput.adp_assignment02.domain.Employee;
import za.ac.cput.adp_assignment02.domain.Notification;
import za.ac.cput.adp_assignment02.domain.Order;
import za.ac.cput.adp_assignment02.domain.OrderItem;
import za.ac.cput.adp_assignment02.domain.Product;
import za.ac.cput.adp_assignment02.dto.CheckoutRequest;
import za.ac.cput.adp_assignment02.repository.CustomerRepository;
import za.ac.cput.adp_assignment02.repository.EmployeeRepository;
import za.ac.cput.adp_assignment02.repository.OrderItemRepository;
import za.ac.cput.adp_assignment02.repository.OrderRepository;
import za.ac.cput.adp_assignment02.repository.ProductRepository;
import za.ac.cput.adp_assignment02.util.Helper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
public class OrderService implements IOrderService {
    private final OrderRepository repository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final EmployeeRepository employeeRepository;
    private final IdGeneratorService idGenerator;
    private final NotificationService notificationService;

    public OrderService(OrderRepository repository, OrderItemRepository orderItemRepository, ProductRepository productRepository,
                        CustomerRepository customerRepository, EmployeeRepository employeeRepository,
                        IdGeneratorService idGenerator, NotificationService notificationService) {
        this.repository = repository;
        this.orderItemRepository = orderItemRepository;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.employeeRepository = employeeRepository;
        this.idGenerator = idGenerator;
        this.notificationService = notificationService;
    }

    /* Complete a sale. Everything happens in one transaction: if any line fails, nothing is saved. */
    @Transactional
    public Order checkout(CheckoutRequest request) {
        if (request == null || Helper.isBlank(request.customerId())) {
            throw new IllegalArgumentException("Please choose a customer for this sale.");
        }
        if (request.items() == null || request.items().isEmpty()) {
            throw new IllegalArgumentException("Add at least one product to the sale.");
        }
        Customer customer = customerRepository.findById(request.customerId())
                .orElseThrow(() -> new NoSuchElementException("The selected customer no longer exists."));
        Employee employee = Helper.isBlank(request.employeeId()) ? null : employeeRepository.findById(request.employeeId()).orElse(null);

        // Combine duplicate lines for the same product
        Map<String, Integer> quantities = new LinkedHashMap<>();
        for (CheckoutRequest.Line line : request.items()) {
            if (line == null || Helper.isBlank(line.productId()) || line.quantity() <= 0) {
                throw new IllegalArgumentException("Every product in the sale needs a quantity of at least 1.");
            }
            quantities.merge(line.productId(), line.quantity(), Integer::sum);
        }

        String orderId = idGenerator.nextId(IdGeneratorService.ORDER, repository::existsById);
        List<OrderItem> items = new ArrayList<>();
        List<Product> touchedProducts = new ArrayList<>();
        double total = 0;

        for (Map.Entry<String, Integer> entry : quantities.entrySet()) {
            Product product = productRepository.findById(entry.getKey())
                    .orElseThrow(() -> new NoSuchElementException("A product in the sale no longer exists."));
            int qty = entry.getValue();
            if (product.getQuantity() < qty) {
                throw new IllegalStateException("Not enough stock for " + product.getName() + ": only " + product.getQuantity() + " available.");
            }
            product.setQuantity(product.getQuantity() - qty);
            touchedProducts.add(productRepository.save(product));

            double lineTotal = round(product.getPrice() * qty);
            total += lineTotal;
            items.add(new OrderItem.OrderItemBuilder()
                    .setOrderItemId(idGenerator.nextId(IdGeneratorService.ORDER_ITEM, orderItemRepository::existsById))
                    .setOrderId(orderId)
                    .setProductId(product.getId())
                    .setProductName(product.getName())
                    .setUnitPrice(product.getPrice())
                    .setQuantity(qty)
                    .setLineTotal(lineTotal)
                    .build());
        }

        Order order = new Order.Builder()
                .setOrderId(orderId)
                .setCustomerId(customer.getCustID())
                .setCustomerName(customer.getFullName())
                .setEmployeeId(employee == null ? null : employee.getEmpID())
                .setEmployeeName(employee == null ? null : employee.getFullName())
                .setOrderDate(LocalDate.now())
                .setOrderTime(LocalDateTime.now())
                .setTotalAmount(round(total))
                .setOrderItems(items)
                .build();
        Order saved = repository.save(order);

        customer.setLastPurchaseDate(LocalDate.now());
        customerRepository.save(customer);

        for (Product product : touchedProducts) {
            notificationService.checkStock(product);
        }
        notificationService.notify(Notification.Type.NEW_SALE, "New sale " + orderId,
                String.format("R %.2f sale to %s%s.", saved.getTotalAmount(), customer.getFullName(),
                        employee == null ? "" : " by " + employee.getFullName()),
                orderId, Notification.AUDIENCE_ADMIN);
        return saved;
    }

    /* Generic create (kept for the original API): generates IDs and recalculates the total */
    @Override
    @Transactional
    public Order create(Order order) {
        if (order == null || Helper.isBlank(order.getCustomerId())) {
            throw new IllegalArgumentException("A customer is required for an order.");
        }
        if (Helper.isBlank(order.getOrderId())) {
            order.setOrderId(idGenerator.nextId(IdGeneratorService.ORDER, repository::existsById));
        }
        if (order.getOrderDate() == null) {
            order.setOrderDate(LocalDate.now());
        }
        if (order.getOrderTime() == null) {
            order.setOrderTime(LocalDateTime.now());
        }
        if (Helper.isBlank(order.getCustomerName())) {
            customerRepository.findById(order.getCustomerId()).ifPresent(c -> order.setCustomerName(c.getFullName()));
        }
        if (order.getOrderItems() != null) {
            for (OrderItem item : order.getOrderItems()) {
                if (Helper.isBlank(item.getOrderItemId())) {
                    item.setOrderItemId(idGenerator.nextId(IdGeneratorService.ORDER_ITEM, orderItemRepository::existsById));
                }
                item.setOrderId(order.getOrderId());
            }
            order.setTotalAmount(round(order.calculateTotal()));
        }
        return repository.save(order);
    }

    @Override
    public Order read(String orderId) {
        return repository.findById(orderId).orElse(null);
    }

    @Override
    public Order update(Order order) {
        if (order == null || Helper.isBlank(order.getOrderId()) || !repository.existsById(order.getOrderId())) {
            throw new NoSuchElementException("Order was not found.");
        }
        return repository.save(order);
    }

    /* Deleting (voiding) an order puts the stock back */
    @Override
    @Transactional
    public boolean delete(String orderId) {
        Order order = repository.findById(orderId).orElse(null);
        if (order == null) {
            return false;
        }
        if (order.getOrderItems() != null) {
            for (OrderItem item : order.getOrderItems()) {
                productRepository.findById(item.getProductId()).ifPresent(p -> {
                    p.setQuantity(p.getQuantity() + item.getQuantity());
                    productRepository.save(p);
                });
            }
        }
        repository.delete(order);
        return true;
    }

    @Override
    public List<Order> getAll() {
        return repository.findAll();
    }

    public List<Order> getForCustomer(String customerId) {
        return repository.findByCustomerIdOrderByOrderDateDesc(customerId);
    }

    public List<Order> searchByCustomerName(String name) {
        return Helper.isBlank(name) ? getAll() : repository.searchByCustomerName(name.trim());
    }

    private static double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
