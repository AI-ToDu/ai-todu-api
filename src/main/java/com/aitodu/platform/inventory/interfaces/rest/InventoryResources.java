package com.aitodu.platform.inventory.interfaces.rest;

import java.math.BigDecimal;
import java.util.UUID;

public final class InventoryResources {
  private InventoryResources() {}

  public record CreateProductResource(
      @jakarta.validation.constraints.NotBlank String sku,
      @jakarta.validation.constraints.NotBlank String name,
      @jakarta.validation.constraints.NotNull @jakarta.validation.constraints.DecimalMin("0")
          BigDecimal price,
      @jakarta.validation.constraints.NotNull @jakarta.validation.constraints.DecimalMin("0")
          BigDecimal cost,
      @jakarta.validation.constraints.NotNull @jakarta.validation.constraints.Min(0)
          Integer minimum) {}

  public record MovementResource(
      @jakarta.validation.constraints.NotNull UUID productId,
      @jakarta.validation.constraints.NotNull @jakarta.validation.constraints.Positive
          BigDecimal quantity,
      @jakarta.validation.constraints.NotBlank String type,
      @jakarta.validation.constraints.NotBlank String reason) {}

  public record ConservationResource(
      @jakarta.validation.constraints.NotBlank String conservation) {}

  public record MovementResponseResource(
      UUID id,
      UUID productId,
      int delta,
      String type,
      String reason,
      UUID actorId,
      java.time.Instant createdAt) {}

  public record ProductResource(
      UUID id,
      String sku,
      String name,
      int quantity,
      BigDecimal price,
      BigDecimal cost,
      int minimum,
      String conservation) {}
}
