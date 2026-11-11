package za.ac.cput.adp_assignment02.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.ac.cput.adp_assignment02.domain.Customer;
import za.ac.cput.adp_assignment02.domain.Employee;
import za.ac.cput.adp_assignment02.domain.Notification;
import za.ac.cput.adp_assignment02.domain.Product;
import za.ac.cput.adp_assignment02.event.StockLevelChangedEvent;
import za.ac.cput.adp_assignment02.repository.CustomerRepository;
import za.ac.cput.adp_assignment02.repository.NotificationRepository;
import za.ac.cput.adp_assignment02.repository.ProductRepository;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/*
 * NotificationService.java
 * Creates and reads in-app notifications. Low stock and inactive customers are
 * also checked automatically every hour (and shortly after the app starts).
 */
@Service
public class NotificationService {

    private final NotificationRepository repository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final int inactiveCustomerDays;
    private final ApplicationEventPublisher events;

    public NotificationService(NotificationRepository repository,
                               ProductRepository productRepository,
                               CustomerRepository customerRepository,
                               @Value("${app.notifications.inactive-customer-days:30}") int inactiveCustomerDays,
                               ApplicationEventPublisher events) {
        this.repository = repository;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.inactiveCustomerDays = inactiveCustomerDays;
        this.events = events;
    }

    public Notification notify(Notification.Type type, String title, String message, String refId, String audience) {
        return repository.save(new Notification(type, title, message, refId, audience));
    }

    /* Only creates the notification if there isn't already an unread one for the same thing */
    public void notifyOnce(Notification.Type type, String title, String message, String refId, String audience) {
        if (!repository.existsByTypeAndRefIdAndSeenFalse(type, refId)) {
            notify(type, title, message, refId, audience);
        }
    }

    public void checkStock(Product product) {
        if (product == null) {
            return;
        }
        // lets open stock alerts close themselves once the product is restocked
        events.publishEvent(new StockLevelChangedEvent(product));
        if (product.getQuantity() <= 0) {
            notifyOnce(Notification.Type.OUT_OF_STOCK, "Out of stock: " + product.getName(),
                    product.getName() + " is out of stock. Re-order from the supplier as soon as possible.",
                    product.getId(), Notification.AUDIENCE_ALL);
        } else if (product.isLowStock()) {
            notifyOnce(Notification.Type.LOW_STOCK, "Low stock: " + product.getName(),
                    "Only " + product.getQuantity() + " left (re-order level is " + product.getReorderLevel() + ").",
                    product.getId(), Notification.AUDIENCE_ALL);
        }
    }

    public List<Notification> getFor(String employeeId, Employee.AccessLevel accessLevel) {
        List<String> audiences = new ArrayList<>();
        audiences.add(Notification.AUDIENCE_ALL);
        if (employeeId != null && !employeeId.isBlank()) {
            audiences.add(employeeId);
        }
        if (accessLevel == Employee.AccessLevel.ADMIN) {
            audiences.add(Notification.AUDIENCE_ADMIN);
        }
        List<Notification> all = repository.findForAudiences(audiences);
        return all.size() > 100 ? all.subList(0, 100) : all;
    }

    @Transactional
    public Notification markRead(Long id) {
        Notification notification = repository.findById(id).orElse(null);
        if (notification != null) {
            notification.setSeen(true);
            repository.save(notification);
        }
        return notification;
    }

    @Transactional
    public int markAllRead(String employeeId, Employee.AccessLevel accessLevel) {
        int count = 0;
        for (Notification n : getFor(employeeId, accessLevel)) {
            if (!n.isSeen()) {
                n.setSeen(true);
                repository.save(n);
                count++;
            }
        }
        return count;
    }

    /* Marks every unread notification of this type about this record as read (e.g. once a stock alert is dealt with) */
    @Transactional
    public void markHandled(Notification.Type type, String refId) {
        for (Notification n : repository.findByTypeAndRefIdAndSeenFalse(type, refId)) {
            n.setSeen(true);
            repository.save(n);
        }
    }

    public boolean delete(Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }

    /* Runs 15 seconds after start-up and then every hour */
    @Scheduled(initialDelay = 15_000, fixedDelay = 3_600_000)
    public void runAutomaticChecks() {
        for (Product product : productRepository.findAll()) {
            checkStock(product);
        }
        LocalDate cutOff = LocalDate.now().minusDays(inactiveCustomerDays);
        for (Customer customer : customerRepository.findAll()) {
            LocalDate lastSeen = customer.getLastPurchaseDate() != null ? customer.getLastPurchaseDate() : customer.getCreatedDate();
            if (lastSeen != null && lastSeen.isBefore(cutOff)) {
                long days = ChronoUnit.DAYS.between(lastSeen, LocalDate.now());
                String what = customer.getLastPurchaseDate() != null ? "hasn't bought anything in " : "hasn't made a first purchase in ";
                notifyOnce(Notification.Type.INACTIVE_CUSTOMER, "Inactive customer: " + customer.getFullName(),
                        customer.getFullName() + " " + what + days + " days. Consider reaching out.",
                        customer.getCustID(), Notification.AUDIENCE_ADMIN);
            }
        }
    }
}
