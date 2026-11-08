package com.aitodu.platform.inventory.application.internal.eventhandlers;

import com.aitodu.platform.inventory.domain.model.aggregates.InventoryItem;
import com.aitodu.platform.inventory.domain.model.events.StockBelowMinimum;
import com.aitodu.platform.shared.application.IntegrationOutbox;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class InventoryIntegrationPublisher {
  private final IntegrationOutbox outbox;

  public InventoryIntegrationPublisher(IntegrationOutbox o) {
    outbox = o;
  }

  public void stage(InventoryItem a) {
    for (var e : a.domainEvents())
      if (e instanceof StockBelowMinimum s)
        outbox.append(
            s.eventId(),
            s.tenantId(),
            "inventory.stock-below-minimum",
            1,
            s.occurredAt(),
            Map.of(
                "productId",
                s.productId().toString(),
                "quantity",
                s.quantity(),
                "minimum",
                s.minimum()));
    a.clearDomainEvents();
  }
}
