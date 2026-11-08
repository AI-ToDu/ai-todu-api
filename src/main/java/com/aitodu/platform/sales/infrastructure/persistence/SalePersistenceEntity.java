package com.aitodu.platform.sales.infrastructure.persistence;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.*;

@Entity
@Table(
    name = "sales",
    uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id", "request_key"}))
public class SalePersistenceEntity {
  @Id public UUID id;
  public UUID tenantId;
  public String number, status, fulfillmentStatus, requestKey, fingerprint;
  public UUID dispatchId;
  public Instant createdAt;

  @ElementCollection
  @CollectionTable(name = "sale_lines", joinColumns = @JoinColumn(name = "sale_id"))
  @OrderColumn(name = "line_order")
  public List<SaleLineEmbeddable> lines = new ArrayList<>();

  @Version public Long version;
}
