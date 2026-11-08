package com.aitodu.platform.logistics.interfaces.rest;

import com.aitodu.platform.logistics.domain.model.aggregates.*;
import java.time.Instant;
import java.util.*;

public final class LogisticsResources {
  private LogisticsResources() {}

  public record PrepareDispatchResource(
      @jakarta.validation.constraints.NotEmpty
          List<@jakarta.validation.constraints.NotNull UUID> saleIds,
      @jakarta.validation.constraints.NotNull UUID driverId,
      @jakarta.validation.constraints.NotNull UUID vehicleId,
      @jakarta.validation.constraints.NotBlank String destination) {}

  public record TransitionResource(
      @jakarta.validation.constraints.NotBlank String status, String recipient) {}

  public record IncidentResource(@jakarta.validation.constraints.NotBlank String description) {}

  public record DriverResource(
      @jakarta.validation.constraints.NotBlank String name,
      @jakarta.validation.constraints.NotBlank String license) {}

  public record VehicleResource(
      @jakarta.validation.constraints.NotBlank String name,
      @jakarta.validation.constraints.NotBlank String plate) {}

  public record DriverResponseResource(UUID id, String name, String license) {}

  public record VehicleResponseResource(UUID id, String name, String plate) {}

  public record DispatchLineResource(
      UUID id, UUID saleId, UUID saleLineId, UUID productId, String name, int quantity) {}

  public record IncidentResponseResource(UUID id, String description, Instant createdAt) {}

  public record CandidateResource(UUID id, String number) {}

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
      String recipient) {}
}
