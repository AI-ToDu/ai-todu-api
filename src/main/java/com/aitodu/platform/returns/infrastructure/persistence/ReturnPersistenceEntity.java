package com.aitodu.platform.returns.infrastructure.persistence;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "customer_returns")
public class ReturnPersistenceEntity {
  @Id public UUID id;
  public UUID tenantId, saleId, saleLineId, productId;
  public int quantity;
  public String disposition, reason;
  public Instant createdAt;
  public long lossCents;
}
