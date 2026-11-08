package com.aitodu.platform.procurement.domain.model.aggregates;

import com.aitodu.platform.procurement.domain.model.entities.PurchaseLine;
import com.aitodu.platform.shared.domain.*;
import java.time.Instant;
import java.util.*;

public final class PurchaseOrder {
  private final UUID id, tenantId, supplierId;
  private final Instant createdAt;
  private List<PurchaseLine> lines;

  public PurchaseOrder(UUID id, UUID tenant, UUID supplier, List<PurchaseLine> lines, Instant at) {
    DomainViolation.require(lines != null && !lines.isEmpty(), "EMPTY_LINES");
    DomainViolation.require(
        lines.stream().map(PurchaseLine::productId).distinct().count() == lines.size(),
        "DUPLICATE_LINE");
    this.id = id;
    tenantId = tenant;
    supplierId = Objects.requireNonNull(supplier);
    this.lines = List.copyOf(lines);
    createdAt = at;
  }

  public void receive(Map<UUID, Integer> units) {
    DomainViolation.require(units != null && !units.isEmpty(), "EMPTY_LINES");
    DomainViolation.require(
        lines.stream().map(PurchaseLine::id).toList().containsAll(units.keySet()), "NOT_FOUND");
    var accepted =
        lines.stream()
            .map(l -> units.containsKey(l.id()) ? l.receive(units.get(l.id())) : l)
            .toList();
    lines = accepted;
  }

  public String status() {
    return lines.stream().allMatch(l -> l.received() == l.quantity())
        ? "RECEIVED"
        : lines.stream().anyMatch(l -> l.received() > 0) ? "PARTIAL" : "OPEN";
  }

  public UUID id() {
    return id;
  }

  public UUID tenantId() {
    return tenantId;
  }

  public UUID supplierId() {
    return supplierId;
  }

  public List<PurchaseLine> lines() {
    return lines;
  }

  public Instant createdAt() {
    return createdAt;
  }

  public String number() {
    return "PO-" + id.toString().substring(0, 8);
  }
}
