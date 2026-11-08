package com.aitodu.platform.inventory.interfaces.rest;

import com.aitodu.platform.inventory.domain.model.aggregates.InventoryItem;
import java.math.BigDecimal;
import java.util.UUID;

public final class InventoryResources {
  private InventoryResources() {}

  public record CreateProductResource(
      String sku, String name, BigDecimal price, BigDecimal cost, Integer minimum) {}

  public record MovementResource(UUID productId, BigDecimal quantity, String type, String reason) {}

  public record ConservationResource(String conservation) {}

  public record MovementResponseResource(
      UUID id,
      UUID productId,
      int delta,
      String type,
      String reason,
      UUID actorId,
      java.time.Instant createdAt) {
    public static MovementResponseResource from(
        com.aitodu.platform.inventory.domain.model.entities.StockMovement m) {
      return new MovementResponseResource(
          m.id(), m.productId(), m.delta(), m.type(), m.reason(), m.actorId(), m.createdAt());
    }
  }

  public record ProductResource(
      UUID id,
      String sku,
      String name,
      int quantity,
      BigDecimal price,
      BigDecimal cost,
      int minimum,
      String conservation) {
    public static ProductResource from(InventoryItem a) {
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

    public static ProductResource from(
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
}
