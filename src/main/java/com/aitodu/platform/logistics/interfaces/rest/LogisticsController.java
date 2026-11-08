package com.aitodu.platform.logistics.interfaces.rest;

import static com.aitodu.platform.logistics.interfaces.rest.LogisticsResources.*;

import com.aitodu.platform.iam.infrastructure.security.WorkspacePrincipal;
import com.aitodu.platform.logistics.application.internal.LogisticsService;
import com.aitodu.platform.logistics.domain.model.commands.*;
import com.aitodu.platform.logistics.domain.model.commands.PrepareDispatchCommand;
import com.aitodu.platform.shared.interfaces.rest.*;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@PreAuthorize("hasAnyRole('ADMIN','LOGISTICS')")
public class LogisticsController {
  private final LogisticsService service;

  public LogisticsController(LogisticsService s) {
    service = s;
  }

  @GetMapping("/dispatches")
  public CollectionResource<DispatchResource> list(@AuthenticationPrincipal WorkspacePrincipal p) {
    return new CollectionResource<>(
        service.list(p.tenantId()).stream().map(DispatchResource::from).toList());
  }

  @PostMapping("/dispatches")
  public ResponseEntity<DispatchResource> prepare(
      @AuthenticationPrincipal WorkspacePrincipal p, @RequestBody PrepareDispatchResource r) {
    return ResponseEntityAssembler.from(
        service.handle(
            new PrepareDispatchCommand(
                p.tenantId(), r.saleIds(), r.driverId(), r.vehicleId(), r.destination())),
        DispatchResource::from,
        201);
  }

  @PatchMapping("/dispatches/{id}")
  public ResponseEntity<DispatchResource> transition(
      @AuthenticationPrincipal WorkspacePrincipal p,
      @PathVariable UUID id,
      @RequestBody TransitionResource r) {
    return ResponseEntityAssembler.from(
        service.handle(new TransitionDispatchCommand(p.tenantId(), id, r.status(), r.recipient())),
        DispatchResource::from,
        200);
  }

  @PostMapping("/dispatches/{id}/incidents")
  public ResponseEntity<DispatchResource> incident(
      @AuthenticationPrincipal WorkspacePrincipal p,
      @PathVariable UUID id,
      @RequestBody IncidentResource r) {
    return ResponseEntityAssembler.from(
        service.handle(new ReportDeliveryIncidentCommand(p.tenantId(), id, r.description())),
        DispatchResource::from,
        201);
  }

  @GetMapping("/dispatch-candidates")
  public CollectionResource<CandidateResource> candidates(
      @AuthenticationPrincipal WorkspacePrincipal p) {
    return new CollectionResource<>(
        service.candidates(p.tenantId()).stream().map(CandidateResource::from).toList());
  }

  @GetMapping("/drivers")
  public CollectionResource<DriverResponseResource> drivers(
      @AuthenticationPrincipal WorkspacePrincipal p) {
    return new CollectionResource<>(
        service.drivers(p.tenantId()).stream().map(DriverResponseResource::from).toList());
  }

  @GetMapping("/vehicles")
  public CollectionResource<VehicleResponseResource> vehicles(
      @AuthenticationPrincipal WorkspacePrincipal p) {
    return new CollectionResource<>(
        service.vehicles(p.tenantId()).stream().map(VehicleResponseResource::from).toList());
  }

  @PostMapping("/drivers")
  public ResponseEntity<DriverResponseResource> driver(
      @AuthenticationPrincipal WorkspacePrincipal p, @RequestBody DriverResource r) {
    return ResponseEntityAssembler.from(
        service.handle(new RegisterDriverCommand(p.tenantId(), r.name(), r.license())),
        DriverResponseResource::from,
        201);
  }

  @PostMapping("/vehicles")
  public ResponseEntity<VehicleResponseResource> vehicle(
      @AuthenticationPrincipal WorkspacePrincipal p, @RequestBody VehicleResource r) {
    return ResponseEntityAssembler.from(
        service.handle(new RegisterVehicleCommand(p.tenantId(), r.name(), r.plate())),
        VehicleResponseResource::from,
        201);
  }
}
