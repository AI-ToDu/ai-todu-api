package com.aitodu.platform.inventory.infrastructure.persistence;

import com.aitodu.platform.inventory.domain.model.aggregates.InventoryItem;

public final class InventoryPersistenceAssembler {
  private InventoryPersistenceAssembler() {}

  public static InventoryItem toDomain(InventoryPersistenceEntity e) {
    return InventoryItem.reconstitute(
        e.id, e.tenantId, e.sku, e.name, e.quantity, e.price, e.cost, e.minimum, e.conservation);
  }

  public static void copy(InventoryItem a, InventoryPersistenceEntity e) {
    e.id = a.id();
    e.tenantId = a.tenantId();
    e.sku = a.sku();
    e.name = a.name();
    e.quantity = a.quantity();
    e.price = a.price();
    e.cost = a.cost();
    e.minimum = a.minimum();
    e.conservation = a.conservation();
  }
}
