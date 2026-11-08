package com.aitodu.platform.sales.application.internal.eventhandlers;

import com.aitodu.platform.sales.domain.model.aggregates.Sale;
import com.aitodu.platform.sales.domain.model.events.SaleConfirmed;
import com.aitodu.platform.shared.application.IntegrationOutbox;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class SaleIntegrationPublisher {
  private final IntegrationOutbox outbox;

  public SaleIntegrationPublisher(IntegrationOutbox o) {
    outbox = o;
  }

  public void stage(Sale a) {
    for (var e : a.domainEvents())
      if (e instanceof SaleConfirmed s)
        outbox.append(
            s.eventId(),
            s.tenantId(),
            "sales.sale-confirmed",
            1,
            s.occurredAt(),
            Map.of(
                "saleId", s.saleId().toString(), "totalCents", s.totalCents(), "currency", "PEN"));
    a.clearDomainEvents();
  }
}
