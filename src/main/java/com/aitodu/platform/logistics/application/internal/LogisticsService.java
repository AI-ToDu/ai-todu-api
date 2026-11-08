package com.aitodu.platform.logistics.application.internal;

import com.aitodu.platform.logistics.application.LogisticsCommandService;
import com.aitodu.platform.logistics.application.LogisticsQueryService;
import com.aitodu.platform.logistics.application.internal.outboundservices.SalesGateway;
import com.aitodu.platform.logistics.domain.model.aggregates.*;
import com.aitodu.platform.logistics.domain.model.commands.*;
import com.aitodu.platform.logistics.domain.model.commands.PrepareDispatchCommand;
import com.aitodu.platform.logistics.domain.model.queries.*;
import com.aitodu.platform.logistics.domain.repositories.*;
import com.aitodu.platform.shared.application.*;
import com.aitodu.platform.shared.application.result.Result;
import com.aitodu.platform.shared.domain.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LogisticsService
    implements com.aitodu.platform.logistics.interfaces.acl.LogisticsContextFacade,
        LogisticsCommandService,
        LogisticsQueryService {
  private final DispatchRepository manifests;
  private final DriverRepository drivers;
  private final VehicleRepository vehicles;
  private final SalesGateway sales;
  private final CommandExecutor commands;
  private final com.aitodu.platform.logistics.application.internal.eventhandlers
          .DeliveryIntegrationPublisher
      events;

  public LogisticsService(
      DispatchRepository d,
      DriverRepository r,
      VehicleRepository v,
      SalesGateway s,
      CommandExecutor c,
      com.aitodu.platform.logistics.application.internal.eventhandlers.DeliveryIntegrationPublisher
          e) {
    manifests = d;
    drivers = r;
    vehicles = v;
    sales = s;
    commands = c;
    events = e;
  }

  public Result<DispatchManifest> handle(PrepareDispatchCommand c) {
    return commands.execute(
        c.tenantId(),
        () -> {
          DomainViolation.require(!c.saleIds().isEmpty(), "EMPTY_LINES");
          DomainViolation.require(
              c.saleIds().stream().distinct().count() == c.saleIds().size(), "DUPLICATE_LINE");
          drivers
              .find(c.tenantId(), c.driverId())
              .orElseThrow(() -> new DomainViolation("NOT_FOUND"));
          vehicles
              .find(c.tenantId(), c.vehicleId())
              .orElseThrow(() -> new DomainViolation("NOT_FOUND"));
          var id = UUID.randomUUID();
          var lines =
              c.saleIds().stream().flatMap(s -> sales.claim(c.tenantId(), s, id).stream()).toList();
          var a =
              DispatchManifest.prepare(
                  id, c.tenantId(), c.driverId(), c.vehicleId(), c.destination(), lines);
          manifests.save(a);
          return a;
        });
  }

  public Result<DispatchManifest> handle(TransitionDispatchCommand c) {
    UUID t = c.tenantId(), id = c.dispatchId();
    String state = c.status(), recipient = c.recipient();
    return commands.execute(
        t,
        () -> {
          var a = load(t, id);
          if ("IN_TRANSIT".equals(state)) {
            DomainViolation.require(
                !manifests.busy(t, a.driverId(), a.vehicleId()), "RESOURCE_BUSY");
            a.start();
          } else if ("DELIVERED".equals(state)) a.deliver(recipient);
          else throw new DomainViolation("INVALID_TRANSITION");
          for (var sale :
              a.lines().stream()
                  .map(com.aitodu.platform.logistics.domain.model.entities.DispatchLine::saleId)
                  .distinct()
                  .toList()) sales.fulfillment(t, sale, id, a.status());
          manifests.save(a);
          events.stage(a);
          return a;
        });
  }

  public Result<DispatchManifest> handle(ReportDeliveryIncidentCommand c) {
    UUID t = c.tenantId(), id = c.dispatchId();
    String description = c.description();
    return commands.execute(
        t,
        () -> {
          var a = load(t, id);
          a.reportIncident(description);
          manifests.save(a);
          return a;
        });
  }

  private DispatchManifest load(UUID t, UUID id) {
    return manifests.find(t, id).orElseThrow(() -> new DomainViolation("NOT_FOUND"));
  }

  @Transactional(readOnly = true)
  public List<DispatchManifest> list(UUID t) {
    return manifests.findAll(t);
  }

  @Transactional(readOnly = true)
  public List<Driver> drivers(UUID t) {
    return drivers.findAll(t);
  }

  @Transactional(readOnly = true)
  public List<Vehicle> vehicles(UUID t) {
    return vehicles.findAll(t);
  }

  public Result<Driver> handle(RegisterDriverCommand c) {
    UUID t = c.tenantId();
    String name = c.name(), license = c.license();
    return commands.execute(
        t,
        () -> {
          var a = new Driver(UUID.randomUUID(), t, name, license);
          drivers.save(a);
          return a;
        });
  }

  public Result<Vehicle> handle(RegisterVehicleCommand c) {
    UUID t = c.tenantId();
    String name = c.name(), plate = c.plate();
    return commands.execute(
        t,
        () -> {
          var a = new Vehicle(UUID.randomUUID(), t, name, plate);
          vehicles.save(a);
          return a;
        });
  }

  public List<com.aitodu.platform.sales.interfaces.acl.SalesContextFacade.SaleSnapshot> candidates(
      UUID t) {
    return sales.candidates(t);
  }

  @Transactional(readOnly = true)
  public long activeDispatches(UUID t) {
    return manifests.findAll(t).stream().filter(d -> d.status().equals("IN_TRANSIT")).count();
  }

  @Transactional(readOnly = true)
  public List<DispatchManifest> handle(GetDispatchesQuery q) {
    return list(q.tenantId());
  }

  @Transactional(readOnly = true)
  public List<Driver> handle(GetDriversQuery q) {
    return drivers(q.tenantId());
  }

  @Transactional(readOnly = true)
  public List<Vehicle> handle(GetVehiclesQuery q) {
    return vehicles(q.tenantId());
  }

  @Transactional(readOnly = true)
  public List<com.aitodu.platform.sales.interfaces.acl.SalesContextFacade.SaleSnapshot> handle(
      GetDispatchCandidatesQuery q) {
    return candidates(q.tenantId());
  }
}
