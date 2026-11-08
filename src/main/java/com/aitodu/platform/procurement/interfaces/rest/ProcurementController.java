package com.aitodu.platform.procurement.interfaces.rest;

import com.aitodu.platform.iam.infrastructure.security.WorkspacePrincipal;
import com.aitodu.platform.procurement.application.internal.ProcurementService;
import com.aitodu.platform.procurement.domain.model.aggregates.PurchaseOrder;
import com.aitodu.platform.procurement.domain.model.commands.*;
import com.aitodu.platform.shared.domain.Quantity;
import com.aitodu.platform.shared.interfaces.rest.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE')")
public class ProcurementController {
  public record SupplierResource(UUID id, String name, String contact) {}

  public record CreateSupplierResource(String name, String contact) {}

  public record RequestedPurchaseLineResource(UUID productId, BigDecimal quantity) {}

  public record PlacePurchaseResource(UUID supplierId, List<RequestedPurchaseLineResource> lines) {}

  public record ReceivedPurchaseLineResource(UUID lineId, BigDecimal quantity) {}

  public record ReceivePurchaseResource(List<ReceivedPurchaseLineResource> lines) {}

  public record PurchaseLineResource(UUID id, UUID productId, int quantity, int received) {}

  public record PurchaseResource(
      UUID id,
      String number,
      UUID supplierId,
      String status,
      Instant createdAt,
      List<PurchaseLineResource> lines) {
    static PurchaseResource from(PurchaseOrder a) {
      return new PurchaseResource(
          a.id(),
          a.number(),
          a.supplierId(),
          a.status(),
          a.createdAt(),
          a.lines().stream()
              .map(l -> new PurchaseLineResource(l.id(), l.productId(), l.quantity(), l.received()))
              .toList());
    }
  }

  private final ProcurementService service;

  public ProcurementController(ProcurementService s) {
    service = s;
  }

  @GetMapping("/suppliers")
  public CollectionResource<SupplierResource> suppliers(
      @AuthenticationPrincipal WorkspacePrincipal p) {
    return new CollectionResource<>(
        service.suppliers(p.tenantId()).stream()
            .map(s -> new SupplierResource(s.id(), s.name(), s.contact()))
            .toList());
  }

  @PostMapping("/suppliers")
  public ResponseEntity<SupplierResource> supplier(
      @AuthenticationPrincipal WorkspacePrincipal p, @RequestBody CreateSupplierResource r) {
    return ResponseEntityAssembler.from(
        service.handle(new RegisterSupplierCommand(p.tenantId(), r.name(), r.contact())),
        s -> new SupplierResource(s.id(), s.name(), s.contact()),
        201);
  }

  @GetMapping("/purchases")
  public CollectionResource<PurchaseResource> orders(
      @AuthenticationPrincipal WorkspacePrincipal p) {
    return new CollectionResource<>(
        service.orders(p.tenantId()).stream().map(PurchaseResource::from).toList());
  }

  @PostMapping("/purchases")
  public ResponseEntity<PurchaseResource> order(
      @AuthenticationPrincipal WorkspacePrincipal p, @RequestBody PlacePurchaseResource r) {
    return ResponseEntityAssembler.from(
        service.handle(
            new PlacePurchaseCommand(
                p.tenantId(),
                r.supplierId(),
                r.lines() == null
                    ? List.of()
                    : r.lines().stream()
                        .map(
                            l ->
                                new PlacePurchaseCommand.Line(
                                    l.productId(), Quantity.positive(l.quantity())))
                        .toList())),
        PurchaseResource::from,
        201);
  }

  @PostMapping("/purchases/{id}/receipts")
  public ResponseEntity<PurchaseResource> receipt(
      @AuthenticationPrincipal WorkspacePrincipal p,
      @PathVariable UUID id,
      @RequestBody ReceivePurchaseResource r) {
    return ResponseEntityAssembler.from(
        service.handle(
            new ReceivePurchaseCommand(
                p.tenantId(),
                p.id(),
                id,
                r.lines() == null
                    ? List.of()
                    : r.lines().stream()
                        .map(
                            l ->
                                new ReceivePurchaseCommand.Line(
                                    l.lineId(), Quantity.positive(l.quantity())))
                        .toList())),
        PurchaseResource::from,
        201);
  }
}
