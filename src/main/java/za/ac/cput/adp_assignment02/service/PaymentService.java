package za.ac.cput.adp_assignment02.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.ac.cput.adp_assignment02.domain.Employee;
import za.ac.cput.adp_assignment02.domain.Notification;
import za.ac.cput.adp_assignment02.domain.Order;
import za.ac.cput.adp_assignment02.domain.Payment;
import za.ac.cput.adp_assignment02.repository.OrderRepository;
import za.ac.cput.adp_assignment02.repository.PaymentRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/*
 * PaymentService.java
 * Customer accounts: sales made "on account" are paid off later, in full or in part.
 */
@Service
public class PaymentService {

    private final PaymentRepository repository;
    private final OrderRepository orderRepository;
    private final NotificationService notificationService;

    public PaymentService(PaymentRepository repository, OrderRepository orderRepository, NotificationService notificationService) {
        this.repository = repository;
        this.orderRepository = orderRepository;
        this.notificationService = notificationService;
    }

    /* amount null = pay off the whole balance */
    @Transactional
    public Order recordPayment(String orderId, Double amount, String method, String note, Employee receivedBy) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new NoSuchElementException("Order was not found."));
        double due = order.getBalanceDue();
        if (due <= 0) {
            throw new IllegalStateException("Order " + orderId + " is already paid in full.");
        }
        double pay = amount == null ? due : Math.round(amount * 100.0) / 100.0;
        if (pay <= 0) {
            throw new IllegalArgumentException("Enter an amount of more than R 0.");
        }
        if (pay > due + 0.001) {
            throw new IllegalArgumentException(String.format("That's more than the R %.2f still owed on this order.", due));
        }
        String how = OrderService.cleanMethod(method);
        if ("ACCOUNT".equals(how)) {
            throw new IllegalArgumentException("Choose how the customer paid: cash, card or EFT.");
        }
        repository.save(new Payment(order, pay, how, receivedBy == null ? null : receivedBy.getFullName(),
                note == null || note.isBlank() ? null : note.trim()));
        order.setAmountPaid(Math.round((order.getAmountPaid() + pay) * 100.0) / 100.0);
        Order saved = orderRepository.save(order);

        String text = saved.getBalanceDue() <= 0
                ? String.format("%s paid off order %s (R %.2f, %s).", order.getCustomerName(), orderId, pay, how.toLowerCase())
                : String.format("%s paid R %.2f on order %s (%s). R %.2f still owed.", order.getCustomerName(), pay, orderId, how.toLowerCase(), saved.getBalanceDue());
        notificationService.notify(Notification.Type.SYSTEM, saved.getBalanceDue() <= 0 ? "Account settled" : "Account payment", text,
                orderId, Notification.AUDIENCE_ADMIN);
        return saved;
    }

    public List<Payment> forOrder(String orderId) {
        return repository.findByOrderIdOrderByPaidAtDesc(orderId);
    }

    public List<Payment> forCustomer(String customerId) {
        return repository.findByCustomerIdOrderByPaidAtDesc(customerId);
    }

    public List<Payment> all() {
        List<Payment> list = repository.findAllByOrderByPaidAtDesc();
        return list.size() > 500 ? new ArrayList<>(list.subList(0, 500)) : list;
    }

    /* what each customer still owes: customerId -> balance */
    public Map<String, Double> balances() {
        Map<String, Double> map = new LinkedHashMap<>();
        for (Order o : orderRepository.findAll()) {
            double due = o.getBalanceDue();
            if (due > 0) {
                map.merge(o.getCustomerId(), due, Double::sum);
            }
        }
        map.replaceAll((k, v) -> Math.round(v * 100.0) / 100.0);
        return map;
    }
}
