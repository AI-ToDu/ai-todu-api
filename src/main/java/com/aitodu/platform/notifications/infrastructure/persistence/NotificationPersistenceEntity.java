package com.aitodu.platform.notifications.infrastructure.persistence;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notifications")
public class NotificationPersistenceEntity {
  @Id public UUID id;
  public UUID tenantId;
  public String type;
  public Instant createdAt;

  @Column(name = "is_read")
  public boolean read;
}
