package com.aitodu.platform.sales.interfaces.rest;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

public final class SaleResources {
  private SaleResources() {}

  public record LineRequest(
      @jakarta.validation.constraints.NotNull UUID productId,
      @jakarta.validation.constraints.NotNull @jakarta.validation.constraints.Positive
          BigDecimal quantity) {}

  public record ConfirmSaleResource(
      @jakarta.validation.constraints.NotEmpty
          List<@jakarta.validation.Valid @jakarta.validation.constraints.NotNull LineRequest>
              lines) {}

  public record CancelResource(@jakarta.validation.constraints.NotBlank String reason) {}

  public record LineResource(
      UUID id,
      UUID productId,
      String name,
      String sku,
      int quantity,
      BigDecimal price,
      BigDecimal cost,
      int returned) {}

  public record ReceiptResource(String number) {}

  public record SaleResource(
      UUID id,
      String number,
      String status,
      BigDecimal total,
      Instant createdAt,
      List<LineResource> lines,
      ReceiptResource receipt) {}
}
