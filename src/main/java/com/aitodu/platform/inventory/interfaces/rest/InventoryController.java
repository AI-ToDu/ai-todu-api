package com.aitodu.platform.inventory.interfaces.rest;

import static com.aitodu.platform.inventory.interfaces.rest.InventoryResources.*;

import com.aitodu.platform.iam.infrastructure.security.WorkspacePrincipal;
import com.aitodu.platform.inventory.application.InventoryCommandService;
import com.aitodu.platform.inventory.application.InventoryQueryService;
import com.aitodu.platform.inventory.domain.model.commands.*;
import com.aitodu.platform.inventory.domain.model.queries.*;
import com.aitodu.platform.inventory.domain.model.valueobjects.Sku;
import com.aitodu.platform.inventory.interfaces.rest.transform.MovementResponseResourceFromEntityAssembler;
import com.aitodu.platform.inventory.interfaces.rest.transform.ProductResourceFromEntityAssembler;
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
  private final InventoryCommandService commands;
  private final InventoryQueryService queries;

  public InventoryController(InventoryCommandService commands, InventoryQueryService queries) {
    this.commands = commands;
    this.queries = queries;
  }

  @GetMapping("/products")
  public CollectionResource<ProductResource> products(
      @AuthenticationPrincipal WorkspacePrincipal p) {
    return new CollectionResource<>(
        queries.handle(new GetInventoryQuery(p.tenantId())).stream()
            .map(ProductResourceFromEntityAssembler::toResourceFromEntity)
            .toList());
  }

  @PostMapping("/products")
  @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE')")
  public ResponseEntity<?> create(
      @AuthenticationPrincipal WorkspacePrincipal p,
      @jakarta.validation.Valid @RequestBody CreateProductResource r) {
    DomainViolation.require(r.minimum() != null, "INVALID_QUANTITY");
    return ResponseEntityAssembler.from(
        commands.handle(
            new CreateProductCommand(
                p.tenantId(),
                new Sku(r.sku()),
                r.name(),
                Money.of(r.price()),
                Money.of(r.cost()),
                r.minimum())),
        ProductResourceFromEntityAssembler::toResourceFromEntity,
        201);
  }

  @PatchMapping("/products/{id}")
  @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE')")
  public ResponseEntity<?> conservation(
      @AuthenticationPrincipal WorkspacePrincipal p,
      @PathVariable UUID id,
      @jakarta.validation.Valid @RequestBody ConservationResource r) {
    return ResponseEntityAssembler.from(
        commands.handle(new ChangeConservationCommand(p.tenantId(), id, r.conservation())),
        ProductResourceFromEntityAssembler::toResourceFromEntity,
        200);
  }

  @GetMapping("/movements")
  @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE')")
  public CollectionResource<MovementResponseResource> movements(
      @AuthenticationPrincipal WorkspacePrincipal p) {
    return new CollectionResource<>(
        queries.handle(new GetMovementsQuery(p.tenantId())).stream()
            .map(MovementResponseResourceFromEntityAssembler::toResourceFromEntity)
            .toList());
  }

  @PostMapping("/movements")
  @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE')")
  public ResponseEntity<?> movement(
      @AuthenticationPrincipal WorkspacePrincipal p,
      @jakarta.validation.Valid @RequestBody MovementResource r) {
    return ResponseEntityAssembler.from(
        commands.handle(
            new RecordMovementCommand(
                p.tenantId(),
                p.id(),
                r.productId(),
                Quantity.positive(r.quantity()),
                r.type(),
                r.reason())),
        ProductResourceFromEntityAssembler::toResourceFromEntity,
        201);
  }
}
