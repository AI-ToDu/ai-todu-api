package com.aitodu.platform.notifications.infrastructure.persistence;

import com.aitodu.platform.notifications.domain.model.valueobjects.ProductReference;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notifications")
public class NotificationPersistenceEntity {
  @Id public UUID id;
  public UUID tenantId;
  public String type;

  @Convert(converter = ProductReferenceConverter.class)
  @Column(name = "product_id")
  public ProductReference product;

  public Instant createdAt;

  @Column(name = "is_read")
  public boolean read;
}
