package com.aitodu.platform.logistics.infrastructure.persistence;

import com.aitodu.platform.logistics.domain.model.aggregates.DispatchManifest;
import com.aitodu.platform.logistics.domain.model.entities.*;
import com.aitodu.platform.logistics.domain.model.valueobjects.DeliveryEvidence;

public final class DispatchPersistenceAssembler {
  private DispatchPersistenceAssembler() {}

  public static DispatchManifest toDomain(DispatchPersistenceEntity e) {
    return DispatchManifest.reconstitute(
        e.id,
        e.tenantId,
        e.driverId,
        e.vehicleId,
        e.destination,
        e.lines.stream()
            .map(
                l ->
                    new DispatchLine(l.id, l.saleId, l.saleLineId, l.productId, l.name, l.quantity))
            .toList(),
        e.status,
        e.createdAt,
        e.evidence == null || e.evidence.deliveredAt == null
            ? null
            : new DeliveryEvidence(e.evidence.recipient, e.evidence.deliveredAt),
        e.incidents.stream()
            .map(i -> new DeliveryIncident(i.id, i.description, i.createdAt))
            .toList());
  }

  public static void copy(DispatchManifest a, DispatchPersistenceEntity e) {
    e.id = a.id();
    e.tenantId = a.tenantId();
    e.driverId = a.driverId();
    e.vehicleId = a.vehicleId();
    e.destination = a.destination();
    e.status = a.status();
    e.createdAt = a.createdAt();
    if (a.evidence() != null) {
      e.evidence = new DeliveryEvidenceEmbeddable();
      e.evidence.recipient = a.evidence().recipient();
      e.evidence.deliveredAt = a.evidence().deliveredAt();
    }
    e.lines.clear();
    for (var l : a.lines()) {
      var r = new DispatchLineEmbeddable();
      r.id = l.id();
      r.saleId = l.saleId();
      r.saleLineId = l.saleLineId();
      r.productId = l.productId();
      r.name = l.name();
      r.quantity = l.quantity();
      e.lines.add(r);
    }
    e.incidents.clear();
    for (var i : a.incidents()) {
      var r = new IncidentEmbeddable();
      r.id = i.id();
      r.description = i.description();
      r.createdAt = i.createdAt();
      e.incidents.add(r);
    }
  }
}
