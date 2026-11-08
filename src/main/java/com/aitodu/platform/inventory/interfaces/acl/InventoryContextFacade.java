package com.aitodu.platform.inventory.interfaces.acl;

import java.math.BigDecimal;
import java.util.*;

public interface InventoryContextFacade {
  record ProductSnapshot(
      UUID id,
      String name,
      String sku,
      int quantity,
      BigDecimal price,
      BigDecimal cost,
      int minimum,
      String conservation) {}

  ProductSnapshot fetch(UUID tenant, UUID product);

  List<ProductSnapshot> list(UUID tenant);

  void issueForSale(UUID tenant, UUID actor, UUID product, int quantity, String reason);

  void receive(UUID tenant, UUID actor, UUID product, int quantity, String reason);
}
