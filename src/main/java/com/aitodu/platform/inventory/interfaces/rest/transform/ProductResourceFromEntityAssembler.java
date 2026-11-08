package com.aitodu.platform.inventory.interfaces.rest.transform;

import static com.aitodu.platform.inventory.interfaces.rest.InventoryResources.*;

import com.aitodu.platform.inventory.domain.model.aggregates.InventoryItem;

public final class ProductResourceFromEntityAssembler {
  private ProductResourceFromEntityAssembler() {}

  public static ProductResource toResourceFromEntity(InventoryItem a) {
    return new ProductResource(
        a.id(),
        a.sku().value(),
        a.name(),
        a.quantity().value(),
        a.price().amount(),
        a.cost().amount(),
        a.minimum(),
        a.conservation());
  }

  public static ProductResource toResourceFromEntity(
      com.aitodu.platform.inventory.interfaces.acl.InventoryContextFacade.ProductSnapshot a) {
    return new ProductResource(
        a.id(),
        a.sku(),
        a.name(),
        a.quantity(),
        a.price(),
        a.cost(),
        a.minimum(),
        a.conservation());
  }
}
