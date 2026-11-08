package com.aitodu.platform.logistics.interfaces.rest.transform;

import static com.aitodu.platform.logistics.interfaces.rest.LogisticsResources.*;

import com.aitodu.platform.logistics.domain.model.aggregates.*;
import java.util.*;

public final class DispatchResourceFromEntityAssembler {
  private DispatchResourceFromEntityAssembler() {}

  public static DispatchResource toResourceFromEntity(DispatchManifest a) {
    return new DispatchResource(
        a.id(),
        a.number(),
        a.driverId(),
        a.vehicleId(),
        a.destination(),
        a.status(),
        a.createdAt(),
        a.lines().stream()
            .map(
                l ->
                    new DispatchLineResource(
                        l.id(), l.saleId(), l.saleLineId(), l.productId(), l.name(), l.quantity()))
            .toList(),
        a.incidents().stream()
            .map(i -> new IncidentResponseResource(i.id(), i.description(), i.createdAt()))
            .toList(),
        a.evidence() == null ? null : a.evidence().recipient());
  }
}
