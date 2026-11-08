package com.aitodu.platform.notifications.infrastructure.persistence;

import com.aitodu.platform.notifications.domain.model.aggregates.Notification;
import com.aitodu.platform.notifications.domain.repositories.NotificationRepository;
import jakarta.persistence.*;
import java.util.*;
import org.springframework.stereotype.Repository;

@Repository
public class JpaNotificationRepository implements NotificationRepository {
  @PersistenceContext private EntityManager em;

  private Notification domain(NotificationPersistenceEntity e) {
    return new Notification(e.id, e.tenantId, e.type, e.createdAt, e.read);
  }

  public Optional<Notification> find(UUID t, UUID id) {
    return Optional.ofNullable(em.find(NotificationPersistenceEntity.class, id))
        .filter(e -> e.tenantId.equals(t))
        .map(this::domain);
  }

  public List<Notification> findAll(UUID t) {
    return em.createQuery(
            "from NotificationPersistenceEntity where tenantId=:t order by createdAt desc",
            NotificationPersistenceEntity.class)
        .setParameter("t", t)
        .getResultStream()
        .map(this::domain)
        .toList();
  }

  public void save(Notification a) {
    var e = new NotificationPersistenceEntity();
    e.id = a.id();
    e.tenantId = a.tenantId();
    e.type = a.type();
    e.createdAt = a.createdAt();
    e.read = a.read();
    em.merge(e);
  }
}
