package com.aitodu.platform.sales.infrastructure.persistence;

import com.aitodu.platform.sales.domain.model.aggregates.Sale;
import com.aitodu.platform.sales.domain.model.entities.SaleLine;
import com.aitodu.platform.shared.domain.Money;

public final class SalePersistenceAssembler {
  private SalePersistenceAssembler() {}

  public static Sale toDomain(SalePersistenceEntity e) {
    return Sale.reconstitute(
        e.id,
        e.tenantId,
        e.number,
        e.createdAt,
        e.lines.stream()
            .map(
                l ->
                    new SaleLine(
                        l.id,
                        l.productId,
                        l.name,
                        l.sku,
                        l.quantity,
                        new Money(l.priceCents),
                        new Money(l.costCents),
                        l.returned))
            .toList(),
        e.status,
        e.dispatchId,
        e.fulfillmentStatus);
  }

  public static void copy(Sale a, SalePersistenceEntity e) {
    e.id = a.id();
    e.tenantId = a.tenantId();
    e.number = a.number();
    e.createdAt = a.createdAt();
    e.status = a.status();
    e.dispatchId = a.dispatchId();
    e.fulfillmentStatus = a.fulfillmentStatus();
    e.lines.clear();
    for (var l : a.lines()) {
      var r = new SaleLineEmbeddable();
      r.id = l.id();
      r.productId = l.productId();
      r.name = l.name();
      r.sku = l.sku();
      r.quantity = l.quantity();
      r.returned = l.returned();
      r.priceCents = l.price().cents();
      r.costCents = l.cost().cents();
      e.lines.add(r);
    }
  }
}
