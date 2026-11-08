package com.aitodu.platform.inventory.interfaces.rest.transform;

import static com.aitodu.platform.inventory.interfaces.rest.InventoryResources.*;

public final class MovementResponseResourceFromEntityAssembler {
  private MovementResponseResourceFromEntityAssembler() {}

  public static MovementResponseResource toResourceFromEntity(
      com.aitodu.platform.inventory.domain.model.entities.StockMovement m) {
    return new MovementResponseResource(
        m.id(), m.productId(), m.delta(), m.type(), m.reason(), m.actorId(), m.createdAt());
  }
}
