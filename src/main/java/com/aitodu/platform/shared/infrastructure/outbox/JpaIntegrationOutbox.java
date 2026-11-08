package com.aitodu.platform.shared.infrastructure.outbox;

import com.aitodu.platform.shared.application.IntegrationOutbox;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

@Component
public class JpaIntegrationOutbox implements IntegrationOutbox {
  @PersistenceContext private EntityManager em;
  private final JsonMapper json = JsonMapper.builder().build();

  @Transactional(propagation = Propagation.MANDATORY)
  public void append(
      UUID id, UUID tenant, String type, int version, Instant at, Map<String, Object> payload) {
    var e = new OutboxPersistenceEntity();
    e.id = id;
    e.tenantId = tenant;
    e.type = type;
    e.schemaVersion = version;
    e.occurredAt = at;
    e.payload = json.writeValueAsString(payload);
    e.status = "PENDING";
    e.nextAttemptAt = Instant.now();
    em.persist(e);
  }
}
