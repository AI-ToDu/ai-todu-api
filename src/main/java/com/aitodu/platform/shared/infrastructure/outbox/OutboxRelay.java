package com.aitodu.platform.shared.infrastructure.outbox;

import com.aitodu.platform.shared.application.IntegrationSubscriber;
import com.aitodu.platform.shared.interfaces.events.IntegrationMessage;
import jakarta.persistence.*;
import java.time.*;
import java.util.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.json.JsonMapper;

@Component
@ConditionalOnProperty(name = "app.outbox.enabled", havingValue = "true", matchIfMissing = true)
public class OutboxRelay {
  @PersistenceContext private EntityManager em;
  private final TransactionTemplate transactions;
  private final List<IntegrationSubscriber> consumers;
  private final JsonMapper json = JsonMapper.builder().build();

  public OutboxRelay(
      org.springframework.transaction.PlatformTransactionManager manager,
      List<IntegrationSubscriber> consumers) {
    transactions = new TransactionTemplate(manager);
    this.consumers = List.copyOf(consumers);
  }

  @Scheduled(
      fixedDelayString = "${app.outbox.delay-ms}",
      initialDelayString = "${app.outbox.initial-delay-ms:3000}")
  public void relay() {
    var ids =
        transactions.execute(
            s ->
                em.createQuery(
                        "select e.id from OutboxPersistenceEntity e where e.status='PENDING' and"
                            + " e.nextAttemptAt<=:now order by e.occurredAt",
                        UUID.class)
                    .setParameter("now", Instant.now())
                    .setMaxResults(25)
                    .getResultList());
    if (ids == null) return;
    for (var id : ids)
      try {
        transactions.executeWithoutResult(s -> publish(id));
      } catch (RuntimeException failure) {
        transactions.executeWithoutResult(
            s -> {
              var e = em.find(OutboxPersistenceEntity.class, id, LockModeType.PESSIMISTIC_WRITE);
              if (e != null && e.status.equals("PENDING")) {
                e.attempts++;
                e.nextAttemptAt =
                    Instant.now().plusSeconds(Math.min(300, 1L << Math.min(e.attempts, 8)));
                e.lastError = failure.getClass().getSimpleName();
              }
            });
      }
  }

  @SuppressWarnings("unchecked")
  private void publish(UUID id) {
    var e = em.find(OutboxPersistenceEntity.class, id, LockModeType.PESSIMISTIC_WRITE);
    if (e == null || !e.status.equals("PENDING")) return;
    Map<String, Object> data = json.readValue(e.payload, Map.class);
    var message =
        new IntegrationMessage(e.id, e.tenantId, e.type, e.schemaVersion, e.occurredAt, data);
    for (var c : consumers) c.accept(message);
    e.status = "PUBLISHED";
    e.publishedAt = Instant.now();
    e.lastError = null;
  }
}
