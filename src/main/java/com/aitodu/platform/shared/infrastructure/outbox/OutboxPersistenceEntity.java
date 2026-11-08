package com.aitodu.platform.shared.infrastructure.outbox;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "integration_outbox")
public class OutboxPersistenceEntity {
  @Id public UUID id;
  public UUID tenantId;
  public String type;
  public int schemaVersion;
  public Instant occurredAt;

  @Column(length = 1000000)
  public String payload;

  public String status;
  public int attempts;
  public Instant nextAttemptAt, publishedAt;
  public String lastError;
}
