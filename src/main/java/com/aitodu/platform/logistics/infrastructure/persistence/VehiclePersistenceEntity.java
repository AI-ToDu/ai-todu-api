package com.aitodu.platform.logistics.infrastructure.persistence;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(
    name = "vehicles",
    uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id", "plate"}))
public class VehiclePersistenceEntity {
  @Id public UUID id;
  public UUID tenantId;
  public String name, plate;
}
