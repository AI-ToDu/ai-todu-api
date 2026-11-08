package com.aitodu.platform.logistics.application.internal.eventhandlers;

import com.aitodu.platform.logistics.domain.model.aggregates.DispatchManifest;
import com.aitodu.platform.logistics.domain.model.events.DeliveryConfirmed;
import com.aitodu.platform.shared.application.IntegrationOutbox;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class DeliveryIntegrationPublisher {
  private final IntegrationOutbox outbox;

  public DeliveryIntegrationPublisher(IntegrationOutbox o) {
    outbox = o;
  }

  public void stage(DispatchManifest a) {
    for (var e : a.domainEvents())
      if (e instanceof DeliveryConfirmed s)
        outbox.append(
            s.eventId(),
            s.tenantId(),
            "logistics.delivery-confirmed",
            1,
            s.occurredAt(),
            Map.of("dispatchId", s.dispatchId().toString()));
    a.clearDomainEvents();
  }
}
