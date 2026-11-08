package com.aitodu.platform.logistics.infrastructure.persistence;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "dispatches")
public class DispatchPersistenceEntity {
  @Id public UUID id;
  public UUID tenantId, driverId, vehicleId;
  public String destination, status;
  public Instant createdAt;
  @Embedded public DeliveryEvidenceEmbeddable evidence;

  @ElementCollection
  @CollectionTable(name = "dispatch_lines", joinColumns = @JoinColumn(name = "dispatch_id"))
  @OrderColumn(name = "line_order")
  public List<DispatchLineEmbeddable> lines = new ArrayList<>();

  @ElementCollection
  @CollectionTable(name = "delivery_incidents", joinColumns = @JoinColumn(name = "dispatch_id"))
  @OrderColumn(name = "incident_order")
  public List<IncidentEmbeddable> incidents = new ArrayList<>();

  @Version public Long version;
}
