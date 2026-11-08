package com.aitodu.platform.sales.domain.model.aggregates;

import com.aitodu.platform.sales.domain.model.entities.SaleLine;
import com.aitodu.platform.sales.domain.model.events.SaleConfirmed;
import com.aitodu.platform.shared.domain.*;
import java.time.Instant;
import java.util.*;

public final class Sale extends AggregateRoot {
  private final UUID id, tenantId;
  private final String number;
  private final Instant createdAt;
  private final List<SaleLine> lines;
  private String status, fulfillmentStatus;
  private UUID dispatchId;

  private Sale(
      UUID id,
      UUID tenant,
      String number,
      Instant createdAt,
      List<SaleLine> lines,
      String status,
      UUID dispatch,
      String fulfillment) {
    DomainViolation.require(lines != null && !lines.isEmpty(), "EMPTY_LINES");
    DomainViolation.require(
        lines.stream().map(SaleLine::productId).distinct().count() == lines.size(),
        "DUPLICATE_LINE");
    this.id = id;
    tenantId = tenant;
    this.number = number;
    this.createdAt = createdAt;
    this.lines = new ArrayList<>(lines);
    this.status = status;
    dispatchId = dispatch;
    fulfillmentStatus = fulfillment;
  }

  public static Sale confirm(UUID tenant, List<SaleLine> lines) {
    var id = UUID.randomUUID();
    var a =
        new Sale(
            id,
            tenant,
            "SAL-" + id.toString().substring(0, 8),
            Instant.now(),
            lines,
            "CONFIRMED",
            null,
            "NONE");
    a.record(new SaleConfirmed(UUID.randomUUID(), tenant, id, a.totalCents(), a.createdAt));
    return a;
  }

  public static Sale reconstitute(
      UUID id,
      UUID tenant,
      String number,
      Instant at,
      List<SaleLine> lines,
      String status,
      UUID dispatch,
      String fulfillment) {
    return new Sale(id, tenant, number, at, lines, status, dispatch, fulfillment);
  }

  public void cancel() {
    DomainViolation.require(status.equals("CONFIRMED"), "ALREADY_CANCELLED");
    DomainViolation.require(dispatchId == null, "SALE_DISPATCHED");
    DomainViolation.require(lines.stream().allMatch(l -> l.returned() == 0), "SALE_HAS_RETURN");
    status = "CANCELLED";
  }

  public void assignDispatch(UUID dispatch) {
    DomainViolation.require(
        status.equals("CONFIRMED")
            && dispatchId == null
            && lines.stream().allMatch(l -> l.returned() == 0),
        "SALE_DISPATCHED");
    dispatchId = dispatch;
    fulfillmentStatus = "PENDING";
  }

  public void updateFulfillment(UUID dispatch, String state) {
    DomainViolation.require(Objects.equals(dispatchId, dispatch), "NOT_FOUND");
    fulfillmentStatus = state;
  }

  public SaleLine returnUnits(UUID lineId, int quantity) {
    DomainViolation.require(
        status.equals("CONFIRMED") && (dispatchId == null || fulfillmentStatus.equals("DELIVERED")),
        "SALE_DISPATCHED");
    for (int i = 0; i < lines.size(); i++)
      if (lines.get(i).id().equals(lineId)) {
        var old = lines.get(i);
        lines.set(i, old.returnUnits(quantity));
        return old;
      }
    throw new DomainViolation("NOT_FOUND");
  }

  public boolean dispatchable() {
    return status.equals("CONFIRMED")
        && dispatchId == null
        && lines.stream().allMatch(l -> l.returned() == 0);
  }

  public long totalCents() {
    return lines.stream().mapToLong(l -> Math.multiplyExact(l.price().cents(), l.quantity())).sum();
  }

  public long netCents() {
    return status.equals("CANCELLED")
        ? 0
        : lines.stream()
            .mapToLong(l -> Math.multiplyExact(l.price().cents(), l.quantity() - l.returned()))
            .sum();
  }

  public long contributionCents() {
    return status.equals("CANCELLED")
        ? 0
        : lines.stream()
            .mapToLong(
                l ->
                    Math.multiplyExact(
                        l.price().cents() - l.cost().cents(), l.quantity() - l.returned()))
            .sum();
  }

  public UUID id() {
    return id;
  }

  public UUID tenantId() {
    return tenantId;
  }

  public String number() {
    return number;
  }

  public Instant createdAt() {
    return createdAt;
  }

  public List<SaleLine> lines() {
    return List.copyOf(lines);
  }

  public String status() {
    return status;
  }

  public UUID dispatchId() {
    return dispatchId;
  }

  public String fulfillmentStatus() {
    return fulfillmentStatus;
  }
}
