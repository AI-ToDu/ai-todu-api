package com.aitodu.platform.procurement.infrastructure.persistence;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "purchase_orders")
public class PurchasePersistenceEntity {
  @Id public UUID id;
  public UUID tenantId, supplierId;
  public Instant createdAt;
  @Version public Long version;

  @ElementCollection
  @CollectionTable(name = "purchase_lines", joinColumns = @JoinColumn(name = "purchase_id"))
  @OrderColumn(name = "line_order")
  public List<PurchaseLineEmbeddable> lines = new ArrayList<>();
}
