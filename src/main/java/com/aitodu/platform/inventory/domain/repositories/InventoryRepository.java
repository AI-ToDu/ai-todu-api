package com.aitodu.platform.inventory.domain.repositories;

import com.aitodu.platform.inventory.domain.model.aggregates.InventoryItem;
import com.aitodu.platform.inventory.domain.model.entities.StockMovement;
import com.aitodu.platform.inventory.domain.model.valueobjects.Sku;
import java.util.*;

public interface InventoryRepository {
  List<InventoryItem> findAll(UUID tenant);

  Optional<InventoryItem> find(UUID tenant, UUID id);

  boolean exists(UUID tenant, Sku sku);

  void save(InventoryItem item);

  void record(StockMovement movement);

  List<StockMovement> movements(UUID tenant);
}
