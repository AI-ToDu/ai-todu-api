package com.aitodu.platform.logistics.interfaces.rest;

import com.aitodu.platform.logistics.domain.model.aggregates.*;
import java.time.Instant;
import java.util.*;

public final class LogisticsResources {
  private LogisticsResources() {}

  public record PrepareDispatchResource(
      List<UUID> saleIds, UUID driverId, UUID vehicleId, String destination) {}

  public record TransitionResource(String status, String recipient) {}

  public record IncidentResource(String description) {}

  public record DriverResource(String name, String license) {}

  public record VehicleResource(String name, String plate) {}

  public record DriverResponseResource(UUID id, String name, String license) {
    public static DriverResponseResource from(Driver a) {
      return new DriverResponseResource(a.id(), a.name(), a.license());
    }
  }

  public record VehicleResponseResource(UUID id, String name, String plate) {
    public static VehicleResponseResource from(Vehicle a) {
      return new VehicleResponseResource(a.id(), a.name(), a.plate());
    }
  }

  public record DispatchLineResource(
      UUID id, UUID saleId, UUID saleLineId, UUID productId, String name, int quantity) {}

  public record IncidentResponseResource(UUID id, String description, Instant createdAt) {}

  public record CandidateResource(UUID id, String number) {
    public static CandidateResource from(
        com.aitodu.platform.sales.interfaces.acl.SalesContextFacade.SaleSnapshot s) {
      return new CandidateResource(s.id(), s.number());
    }
  }

  public record DispatchResource(
      UUID id,
      String number,
      UUID driverId,
      UUID vehicleId,
      String destination,
      String status,
      Instant createdAt,
      List<DispatchLineResource> lines,
      List<IncidentResponseResource> incidents,
      String recipient) {
    public static DispatchResource from(DispatchManifest a) {
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
                          l.id(),
                          l.saleId(),
                          l.saleLineId(),
                          l.productId(),
                          l.name(),
                          l.quantity()))
              .toList(),
          a.incidents().stream()
              .map(i -> new IncidentResponseResource(i.id(), i.description(), i.createdAt()))
              .toList(),
          a.evidence() == null ? null : a.evidence().recipient());
    }
  }
}
