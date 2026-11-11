package za.ac.cput.adp_assignment02.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import za.ac.cput.adp_assignment02.domain.Notification;

import java.util.Collection;
import java.util.List;

/*
 * NotificationRepository.java
 * Stores in-app notifications and finds them by audience (ADMIN, ALL or one employee).
 */
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("select n from Notification n where n.audience in :audiences order by n.createdAt desc")
    List<Notification> findForAudiences(@Param("audiences") Collection<String> audiences);

    boolean existsByTypeAndRefIdAndSeenFalse(Notification.Type type, String refId);

    List<Notification> findByTypeAndRefIdAndSeenFalse(Notification.Type type, String refId);
}
