package com.aitodu.platform.inventory.infrastructure.persistence;

import com.aitodu.platform.inventory.domain.model.valueobjects.Sku;
import com.aitodu.platform.shared.domain.*;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(
    name = "inventory_items",
    uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id", "sku"}))
public class InventoryPersistenceEntity {
  @Id public UUID id;

  @Column(name = "tenant_id")
  public UUID tenantId;

  @Convert(converter = SkuPersistenceConverter.class)
  public Sku sku;

  public String name;

  @Convert(converter = QuantityPersistenceConverter.class)
  public Quantity quantity;

  @Convert(converter = MoneyPersistenceConverter.class)
  public Money price;

  @Convert(converter = MoneyPersistenceConverter.class)
  public Money cost;

  public int minimum;
  public String conservation;
  @Version public Long version;
}
