package com.aitodu.platform.logistics.interfaces.rest;

import static com.aitodu.platform.logistics.interfaces.rest.LogisticsResources.*;

import com.aitodu.platform.iam.infrastructure.security.WorkspacePrincipal;
import com.aitodu.platform.logistics.application.LogisticsCommandService;
import com.aitodu.platform.logistics.application.LogisticsQueryService;
import com.aitodu.platform.logistics.domain.model.commands.*;
import com.aitodu.platform.logistics.domain.model.commands.PrepareDispatchCommand;
import com.aitodu.platform.logistics.domain.model.queries.*;
import com.aitodu.platform.logistics.interfaces.rest.transform.CandidateResourceFromEntityAssembler;
import com.aitodu.platform.logistics.interfaces.rest.transform.DispatchResourceFromEntityAssembler;
import com.aitodu.platform.logistics.interfaces.rest.transform.DriverResponseResourceFromEntityAssembler;
import com.aitodu.platform.logistics.interfaces.rest.transform.VehicleResponseResourceFromEntityAssembler;
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
  private final LogisticsCommandService commands;
  private final LogisticsQueryService queries;

  public LogisticsController(LogisticsCommandService commands, LogisticsQueryService queries) {
    this.commands = commands;
    this.queries = queries;
  }

  @GetMapping("/dispatches")
  public CollectionResource<DispatchResource> list(@AuthenticationPrincipal WorkspacePrincipal p) {
    return new CollectionResource<>(
        queries.handle(new GetDispatchesQuery(p.tenantId())).stream()
            .map(DispatchResourceFromEntityAssembler::toResourceFromEntity)
            .toList());
  }

  @PostMapping("/dispatches")
  public ResponseEntity<?> prepare(
      @AuthenticationPrincipal WorkspacePrincipal p,
      @jakarta.validation.Valid @RequestBody PrepareDispatchResource r) {
    return ResponseEntityAssembler.from(
        commands.handle(
            new PrepareDispatchCommand(
                p.tenantId(), r.saleIds(), r.driverId(), r.vehicleId(), r.destination())),
        DispatchResourceFromEntityAssembler::toResourceFromEntity,
        201);
  }

  @PatchMapping("/dispatches/{id}")
  public ResponseEntity<?> transition(
      @AuthenticationPrincipal WorkspacePrincipal p,
      @PathVariable UUID id,
      @jakarta.validation.Valid @RequestBody TransitionResource r) {
    return ResponseEntityAssembler.from(
        commands.handle(new TransitionDispatchCommand(p.tenantId(), id, r.status(), r.recipient())),
        DispatchResourceFromEntityAssembler::toResourceFromEntity,
        200);
  }

  @PostMapping("/dispatches/{id}/incidents")
  public ResponseEntity<?> incident(
      @AuthenticationPrincipal WorkspacePrincipal p,
      @PathVariable UUID id,
      @jakarta.validation.Valid @RequestBody IncidentResource r) {
    return ResponseEntityAssembler.from(
        commands.handle(new ReportDeliveryIncidentCommand(p.tenantId(), id, r.description())),
        DispatchResourceFromEntityAssembler::toResourceFromEntity,
        201);
  }

  @GetMapping("/dispatch-candidates")
  public CollectionResource<CandidateResource> candidates(
      @AuthenticationPrincipal WorkspacePrincipal p) {
    return new CollectionResource<>(
        queries.handle(new GetDispatchCandidatesQuery(p.tenantId())).stream()
            .map(CandidateResourceFromEntityAssembler::toResourceFromEntity)
            .toList());
  }

  @GetMapping("/drivers")
  public CollectionResource<DriverResponseResource> drivers(
      @AuthenticationPrincipal WorkspacePrincipal p) {
    return new CollectionResource<>(
        queries.handle(new GetDriversQuery(p.tenantId())).stream()
            .map(DriverResponseResourceFromEntityAssembler::toResourceFromEntity)
            .toList());
  }

  @GetMapping("/vehicles")
  public CollectionResource<VehicleResponseResource> vehicles(
      @AuthenticationPrincipal WorkspacePrincipal p) {
    return new CollectionResource<>(
        queries.handle(new GetVehiclesQuery(p.tenantId())).stream()
            .map(VehicleResponseResourceFromEntityAssembler::toResourceFromEntity)
            .toList());
  }

  @PostMapping("/drivers")
  public ResponseEntity<?> driver(
      @AuthenticationPrincipal WorkspacePrincipal p,
      @jakarta.validation.Valid @RequestBody DriverResource r) {
    return ResponseEntityAssembler.from(
        commands.handle(new RegisterDriverCommand(p.tenantId(), r.name(), r.license())),
        DriverResponseResourceFromEntityAssembler::toResourceFromEntity,
        201);
  }

  @PostMapping("/vehicles")
  public ResponseEntity<?> vehicle(
      @AuthenticationPrincipal WorkspacePrincipal p,
      @jakarta.validation.Valid @RequestBody VehicleResource r) {
    return ResponseEntityAssembler.from(
        commands.handle(new RegisterVehicleCommand(p.tenantId(), r.name(), r.plate())),
        VehicleResponseResourceFromEntityAssembler::toResourceFromEntity,
        201);
  }
}
