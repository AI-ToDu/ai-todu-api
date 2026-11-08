package com.aitodu.platform.inventory.interfaces.rest;

import static com.aitodu.platform.inventory.interfaces.rest.InventoryResources.*;

import com.aitodu.platform.iam.infrastructure.security.WorkspacePrincipal;
import com.aitodu.platform.inventory.application.internal.InventoryService;
import com.aitodu.platform.inventory.domain.model.commands.*;
import com.aitodu.platform.inventory.domain.model.valueobjects.Sku;
import com.aitodu.platform.shared.domain.*;
import com.aitodu.platform.shared.interfaces.rest.*;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class InventoryController {
  private final InventoryService service;

  public InventoryController(InventoryService s) {
    service = s;
  }

  @GetMapping("/products")
  public CollectionResource<ProductResource> products(
      @AuthenticationPrincipal WorkspacePrincipal p) {
    return new CollectionResource<>(
        service.list(p.tenantId()).stream().map(ProductResource::from).toList());
  }

  @PostMapping("/products")
  @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE')")
  public ResponseEntity<?> create(
      @AuthenticationPrincipal WorkspacePrincipal p, @RequestBody CreateProductResource r) {
    DomainViolation.require(r.minimum() != null, "INVALID_QUANTITY");
    return ResponseEntityAssembler.from(
        service.handle(
            new CreateProductCommand(
                p.tenantId(),
                new Sku(r.sku()),
                r.name(),
                Money.of(r.price()),
                Money.of(r.cost()),
                r.minimum())),
        ProductResource::from,
        201);
  }

  @PatchMapping("/products/{id}")
  @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE')")
  public ResponseEntity<?> conservation(
      @AuthenticationPrincipal WorkspacePrincipal p,
      @PathVariable UUID id,
      @RequestBody ConservationResource r) {
    return ResponseEntityAssembler.from(
        service.handle(new ChangeConservationCommand(p.tenantId(), id, r.conservation())),
        ProductResource::from,
        200);
  }

  @GetMapping("/movements")
  @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE')")
  public CollectionResource<MovementResponseResource> movements(
      @AuthenticationPrincipal WorkspacePrincipal p) {
    return new CollectionResource<>(
        service.movements(p.tenantId()).stream().map(MovementResponseResource::from).toList());
  }

  @PostMapping("/movements")
  @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE')")
  public ResponseEntity<?> movement(
      @AuthenticationPrincipal WorkspacePrincipal p, @RequestBody MovementResource r) {
    return ResponseEntityAssembler.from(
        service.handle(
            new RecordMovementCommand(
                p.tenantId(),
                p.id(),
                r.productId(),
                Quantity.positive(r.quantity()),
                r.type(),
                r.reason())),
        ProductResource::from,
        201);
  }
}
