package za.ac.cput.adp_assignment02.service;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.ac.cput.adp_assignment02.domain.Employee;
import za.ac.cput.adp_assignment02.domain.Notification;
import za.ac.cput.adp_assignment02.domain.Product;
import za.ac.cput.adp_assignment02.domain.StockAlert;
import za.ac.cput.adp_assignment02.event.StockLevelChangedEvent;
import za.ac.cput.adp_assignment02.repository.ProductRepository;
import za.ac.cput.adp_assignment02.repository.StockAlertRepository;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.NoSuchElementException;

/*
 * StockAlertService.java
 * Lets employees alert the admins about low stock, and lets admins respond.
 * Every step sends a notification: admins hear about new alerts, and the employee
 * who raised one hears when an admin is on it and when it has been restocked.
 */
@Service
public class StockAlertService {

    private static final EnumSet<StockAlert.Status> ACTIVE = EnumSet.of(StockAlert.Status.OPEN, StockAlert.Status.ACKNOWLEDGED);

    private final StockAlertRepository repository;
    private final ProductRepository productRepository;
    private final NotificationService notificationService;

    public StockAlertService(StockAlertRepository repository, ProductRepository productRepository, NotificationService notificationService) {
        this.repository = repository;
        this.productRepository = productRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public StockAlert raise(String productId, String note, Employee employee) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NoSuchElementException("Product was not found."));
        if (!product.isLowStock() && product.getQuantity() > 0) {
            throw new IllegalArgumentException(product.getName() + " still has " + product.getQuantity()
                    + " in stock, which is above its re-order level of " + product.getReorderLevel() + ".");
        }
        List<StockAlert> active = repository.findByProductIdAndStatusIn(productId, ACTIVE);
        if (!active.isEmpty()) {
            StockAlert existing = active.get(0);
            throw new IllegalStateException("The admins already know about " + product.getName()
                    + " – " + existing.getRaisedByName() + " sent an alert. You'll be notified when it's restocked.");
        }
        String cleanNote = note == null || note.isBlank() ? null : note.trim();
        if (cleanNote != null && cleanNote.length() > 500) {
            cleanNote = cleanNote.substring(0, 500);
        }
        StockAlert saved = repository.save(new StockAlert(product, employee, cleanNote));

        String level = product.getQuantity() <= 0 ? "is out of stock" : "is down to " + product.getQuantity() + " (re-order level " + product.getReorderLevel() + ")";
        notificationService.notify(Notification.Type.STOCK_ALERT, "Restock request: " + product.getName(),
                employee.getFullName() + " says " + product.getName() + " " + level + "."
                        + (cleanNote != null ? " Note: \"" + cleanNote + "\"" : ""),
                product.getId(), Notification.AUDIENCE_ADMIN);
        return saved;
    }

    public List<StockAlert> getAll() {
        List<StockAlert> all = repository.findAllByOrderByCreatedAtDesc();
        return all.size() > 200 ? all.subList(0, 200) : all;
    }

    @Transactional
    public StockAlert acknowledge(Long id, Employee admin) {
        StockAlert alert = find(id);
        if (alert.getStatus() != StockAlert.Status.OPEN) {
            return alert;
        }
        alert.setStatus(StockAlert.Status.ACKNOWLEDGED);
        alert.setHandledByName(admin.getFullName());
        alert.setHandledAt(LocalDateTime.now());
        StockAlert saved = repository.save(alert);
        notificationService.notify(Notification.Type.STOCK_ALERT_UPDATE, "On it: " + alert.getProductName(),
                admin.getFullName() + " has seen your stock alert for " + alert.getProductName() + " and is sorting out a re-order.",
                alert.getProductId(), alert.getRaisedById());
        return saved;
    }

    @Transactional
    public StockAlert resolve(Long id, String resolution, Employee admin) {
        StockAlert alert = find(id);
        if (alert.getStatus() == StockAlert.Status.RESOLVED) {
            return alert;
        }
        String text = resolution == null || resolution.isBlank() ? "Closed by " + admin.getFullName() + "." : resolution.trim();
        return close(alert, admin.getFullName(), text);
    }

    /* A product changed – close any open alert for it once it's back above the re-order level */
    @EventListener
    @Transactional
    public void onStockLevelChanged(StockLevelChangedEvent event) {
        Product product = event.product();
        if (product == null || product.getQuantity() <= 0 || product.isLowStock()) {
            return;
        }
        for (StockAlert alert : repository.findByProductIdAndStatusIn(product.getId(), ACTIVE)) {
            close(alert, alert.getHandledByName() != null ? alert.getHandledByName() : "Stock update",
                    "Restocked – " + product.getQuantity() + " now in stock.");
        }
    }

    private StockAlert close(StockAlert alert, String by, String resolution) {
        alert.setStatus(StockAlert.Status.RESOLVED);
        alert.setHandledByName(by);
        alert.setHandledAt(LocalDateTime.now());
        alert.setResolution(resolution);
        StockAlert saved = repository.save(alert);
        notificationService.markHandled(Notification.Type.STOCK_ALERT, alert.getProductId());
        notificationService.notify(Notification.Type.STOCK_ALERT_UPDATE, "Sorted: " + alert.getProductName(),
                "Your stock alert for " + alert.getProductName() + " has been closed. " + resolution,
                alert.getProductId(), alert.getRaisedById());
        return saved;
    }

    private StockAlert find(Long id) {
        return repository.findById(id).orElseThrow(() -> new NoSuchElementException("Stock alert was not found."));
    }
}
