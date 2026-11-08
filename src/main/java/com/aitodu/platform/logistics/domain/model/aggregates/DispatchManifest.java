package com.aitodu.platform.logistics.domain.model.aggregates;

import com.aitodu.platform.logistics.domain.model.entities.*;
import com.aitodu.platform.logistics.domain.model.events.DeliveryConfirmed;
import com.aitodu.platform.logistics.domain.model.valueobjects.DeliveryEvidence;
import com.aitodu.platform.shared.domain.*;
import java.time.Instant;
import java.util.*;

public final class DispatchManifest extends AggregateRoot {
  private final UUID id, tenantId, driverId, vehicleId;
  private final String number, destination;
  private final List<DispatchLine> lines;
  private final Instant createdAt;
  private final List<DeliveryIncident> incidents;
  private String status;
  private DeliveryEvidence evidence;

  private DispatchManifest(
      UUID id,
      UUID tenant,
      UUID driver,
      UUID vehicle,
      String destination,
      List<DispatchLine> lines,
      String status,
      Instant createdAt,
      DeliveryEvidence evidence,
      List<DeliveryIncident> incidents) {
    this.id = id;
    tenantId = tenant;
    driverId = Objects.requireNonNull(driver);
    vehicleId = Objects.requireNonNull(vehicle);
    number = "DSP-" + id.toString().substring(0, 8);
    this.destination = Text.required(destination, 240);
    DomainViolation.require(this.destination.length() >= 5, "INVALID_ADDRESS");
    DomainViolation.require(lines != null && !lines.isEmpty(), "EMPTY_LINES");
    this.lines = List.copyOf(lines);
    this.status = status;
    this.createdAt = createdAt;
    this.evidence = evidence;
    this.incidents = new ArrayList<>(incidents);
  }

  public static DispatchManifest prepare(
      UUID id,
      UUID tenant,
      UUID driver,
      UUID vehicle,
      String destination,
      List<DispatchLine> lines) {
    return new DispatchManifest(
        id, tenant, driver, vehicle, destination, lines, "PENDING", Instant.now(), null, List.of());
  }

  public static DispatchManifest reconstitute(
      UUID id,
      UUID tenant,
      UUID driver,
      UUID vehicle,
      String destination,
      List<DispatchLine> lines,
      String status,
      Instant createdAt,
      DeliveryEvidence evidence,
      List<DeliveryIncident> incidents) {
    return new DispatchManifest(
        id, tenant, driver, vehicle, destination, lines, status, createdAt, evidence, incidents);
  }

  public void start() {
    DomainViolation.require(status.equals("PENDING"), "INVALID_TRANSITION");
    status = "IN_TRANSIT";
  }

  public void deliver(String recipient) {
    DomainViolation.require(status.equals("IN_TRANSIT"), "INVALID_TRANSITION");
    evidence = new DeliveryEvidence(recipient, Instant.now());
    status = "DELIVERED";
    record(new DeliveryConfirmed(UUID.randomUUID(), tenantId, id, evidence.deliveredAt()));
  }

  public void reportIncident(String description) {
    DomainViolation.require(!status.equals("DELIVERED"), "INVALID_TRANSITION");
    incidents.add(new DeliveryIncident(UUID.randomUUID(), description, Instant.now()));
  }

  public UUID id() {
    return id;
  }

  public UUID tenantId() {
    return tenantId;
  }

  public UUID driverId() {
    return driverId;
  }

  public UUID vehicleId() {
    return vehicleId;
  }

  public String number() {
    return number;
  }

  public String destination() {
    return destination;
  }

  public String status() {
    return status;
  }

  public Instant createdAt() {
    return createdAt;
  }

  public List<DispatchLine> lines() {
    return lines;
  }

  public DeliveryEvidence evidence() {
    return evidence;
  }

  public List<DeliveryIncident> incidents() {
    return List.copyOf(incidents);
  }
}
