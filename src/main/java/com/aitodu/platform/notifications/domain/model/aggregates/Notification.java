package com.aitodu.platform.notifications.domain.model.aggregates;

import com.aitodu.platform.notifications.domain.model.valueobjects.ProductReference;
import java.time.Instant;
import java.util.UUID;

public final class Notification {
  private final UUID id, tenantId;
  private final String type;
  private final ProductReference product;
  private final Instant createdAt;
  private boolean read;

  public Notification(UUID id, UUID tenant, String type, Instant at, boolean read) {
    this(id, tenant, type, at, read, null);
  }

  public Notification(
      UUID id, UUID tenant, String type, Instant at, boolean read, ProductReference product) {
    this.id = id;
    tenantId = tenant;
    this.type = type;
    this.product = product;
    createdAt = at;
    this.read = read;
  }

  public void markRead() {
    read = true;
  }

  public UUID id() {
    return id;
  }

  public UUID tenantId() {
    return tenantId;
  }

  public String type() {
    return type;
  }

  public ProductReference product() {
    return product;
  }

  public Instant createdAt() {
    return createdAt;
  }

  public boolean read() {
    return read;
  }
}
