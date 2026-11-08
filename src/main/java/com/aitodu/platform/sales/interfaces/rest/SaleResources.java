package com.aitodu.platform.sales.interfaces.rest;

import com.aitodu.platform.sales.domain.model.aggregates.Sale;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

public final class SaleResources {
  private SaleResources() {}

  public record LineRequest(UUID productId, BigDecimal quantity) {}

  public record ConfirmSaleResource(List<LineRequest> lines) {}

  public record CancelResource(String reason) {}

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
      ReceiptResource receipt) {
    public static SaleResource from(Sale a) {
      return new SaleResource(
          a.id(),
          a.number(),
          a.status(),
          BigDecimal.valueOf(a.totalCents(), 2),
          a.createdAt(),
          a.lines().stream()
              .map(
                  l ->
                      new LineResource(
                          l.id(),
                          l.productId(),
                          l.name(),
                          l.sku(),
                          l.quantity(),
                          l.price().amount(),
                          l.cost().amount(),
                          l.returned()))
              .toList(),
          new ReceiptResource("REC-" + a.number()));
    }
  }
}
