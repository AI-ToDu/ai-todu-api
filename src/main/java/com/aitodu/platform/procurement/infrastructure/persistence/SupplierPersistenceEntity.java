package com.aitodu.platform.procurement.infrastructure.persistence;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "suppliers")
public class SupplierPersistenceEntity {
  @Id public UUID id;
  public UUID tenantId;
  public String name, contact;
}
