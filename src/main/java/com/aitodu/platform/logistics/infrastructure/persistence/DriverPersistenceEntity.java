package com.aitodu.platform.logistics.infrastructure.persistence;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(
    name = "drivers",
    uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id", "license"}))
public class DriverPersistenceEntity {
  @Id public UUID id;
  public UUID tenantId;
  public String name, license;
}
