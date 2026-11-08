package com.aitodu.platform.sales.interfaces.rest.transform;

import static com.aitodu.platform.sales.interfaces.rest.SaleResources.*;

import com.aitodu.platform.sales.domain.model.aggregates.Sale;
import java.math.BigDecimal;
import java.util.*;

public final class SaleResourceFromEntityAssembler {
  private SaleResourceFromEntityAssembler() {}

  public static SaleResource toResourceFromEntity(Sale a) {
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
