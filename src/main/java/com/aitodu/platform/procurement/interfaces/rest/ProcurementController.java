package com.aitodu.platform.procurement.interfaces.rest;

import com.aitodu.platform.iam.infrastructure.security.WorkspacePrincipal;
import com.aitodu.platform.procurement.application.ProcurementCommandService;
import com.aitodu.platform.procurement.application.ProcurementQueryService;
import com.aitodu.platform.procurement.domain.model.commands.*;
import com.aitodu.platform.procurement.domain.model.queries.*;
import com.aitodu.platform.procurement.interfaces.rest.transform.PurchaseResourceFromEntityAssembler;
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

  public record CreateSupplierResource(
      @jakarta.validation.constraints.NotBlank String name,
      @jakarta.validation.constraints.NotBlank String contact) {}

  public record RequestedPurchaseLineResource(
      @jakarta.validation.constraints.NotNull UUID productId,
      @jakarta.validation.constraints.NotNull @jakarta.validation.constraints.Positive
          BigDecimal quantity) {}

  public record PlacePurchaseResource(
      @jakarta.validation.constraints.NotNull UUID supplierId,
      @jakarta.validation.constraints.NotEmpty
          List<
                  @jakarta.validation.Valid @jakarta.validation.constraints.NotNull
                  RequestedPurchaseLineResource>
              lines) {}

  public record ReceivedPurchaseLineResource(
      @jakarta.validation.constraints.NotNull UUID lineId,
      @jakarta.validation.constraints.NotNull @jakarta.validation.constraints.Positive
          BigDecimal quantity) {}

  public record ReceivePurchaseResource(
      @jakarta.validation.constraints.NotEmpty
          List<
                  @jakarta.validation.Valid @jakarta.validation.constraints.NotNull
                  ReceivedPurchaseLineResource>
              lines) {}

  public record PurchaseLineResource(UUID id, UUID productId, int quantity, int received) {}

  public record PurchaseResource(
      UUID id,
      String number,
      UUID supplierId,
      String status,
      Instant createdAt,
      List<PurchaseLineResource> lines) {}

  private final ProcurementCommandService commands;
  private final ProcurementQueryService queries;

  public ProcurementController(
      ProcurementCommandService commands, ProcurementQueryService queries) {
    this.commands = commands;
    this.queries = queries;
  }

  @GetMapping("/suppliers")
  public CollectionResource<SupplierResource> suppliers(
      @AuthenticationPrincipal WorkspacePrincipal p) {
    return new CollectionResource<>(
        queries.handle(new GetSuppliersQuery(p.tenantId())).stream()
            .map(s -> new SupplierResource(s.id(), s.name(), s.contact()))
            .toList());
  }

  @PostMapping("/suppliers")
  public ResponseEntity<?> supplier(
      @AuthenticationPrincipal WorkspacePrincipal p,
      @jakarta.validation.Valid @RequestBody CreateSupplierResource r) {
    return ResponseEntityAssembler.from(
        commands.handle(new RegisterSupplierCommand(p.tenantId(), r.name(), r.contact())),
        s -> new SupplierResource(s.id(), s.name(), s.contact()),
        201);
  }

  @GetMapping("/purchases")
  public CollectionResource<PurchaseResource> orders(
      @AuthenticationPrincipal WorkspacePrincipal p) {
    return new CollectionResource<>(
        queries.handle(new GetPurchasesQuery(p.tenantId())).stream()
            .map(PurchaseResourceFromEntityAssembler::toResourceFromEntity)
            .toList());
  }

  @PostMapping("/purchases")
  public ResponseEntity<?> order(
      @AuthenticationPrincipal WorkspacePrincipal p,
      @jakarta.validation.Valid @RequestBody PlacePurchaseResource r) {
    return ResponseEntityAssembler.from(
        commands.handle(
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
        PurchaseResourceFromEntityAssembler::toResourceFromEntity,
        201);
  }

  @PostMapping("/purchases/{id}/receipts")
  public ResponseEntity<?> receipt(
      @AuthenticationPrincipal WorkspacePrincipal p,
      @PathVariable UUID id,
      @jakarta.validation.Valid @RequestBody ReceivePurchaseResource r) {
    return ResponseEntityAssembler.from(
        commands.handle(
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
        PurchaseResourceFromEntityAssembler::toResourceFromEntity,
        201);
  }
}
