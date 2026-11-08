package com.aitodu.platform.inventory.infrastructure.persistence;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "stock_movements")
public class MovementPersistenceEntity {
  @Id public UUID id;
  public UUID tenantId, productId, actorId;
  public int delta;
  public String type, reason;
  public Instant createdAt;
}
