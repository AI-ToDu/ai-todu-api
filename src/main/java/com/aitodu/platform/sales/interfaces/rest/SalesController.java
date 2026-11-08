package com.aitodu.platform.sales.interfaces.rest;

import static com.aitodu.platform.sales.interfaces.rest.SaleResources.*;

import com.aitodu.platform.iam.infrastructure.security.WorkspacePrincipal;
import com.aitodu.platform.sales.application.SaleCommandService;
import com.aitodu.platform.sales.application.SaleQueryService;
import com.aitodu.platform.sales.domain.model.commands.*;
import com.aitodu.platform.sales.domain.model.queries.*;
import com.aitodu.platform.sales.interfaces.rest.transform.SaleResourceFromEntityAssembler;
import com.aitodu.platform.shared.domain.Quantity;
import com.aitodu.platform.shared.interfaces.rest.*;
import java.util.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/sales")
@PreAuthorize("hasAnyRole('ADMIN','SALES')")
public class SalesController {
  private final SaleCommandService commands;
  private final SaleQueryService queries;

  public SalesController(SaleCommandService commands, SaleQueryService queries) {
    this.commands = commands;
    this.queries = queries;
  }

  @GetMapping
  public CollectionResource<SaleResource> list(@AuthenticationPrincipal WorkspacePrincipal p) {
    return new CollectionResource<>(
        queries.handle(new GetSalesQuery(p.tenantId())).stream()
            .map(SaleResourceFromEntityAssembler::toResourceFromEntity)
            .toList());
  }

  @PostMapping
  public ResponseEntity<?> create(
      @AuthenticationPrincipal WorkspacePrincipal p,
      @jakarta.validation.Valid @RequestBody ConfirmSaleResource r,
      @RequestHeader(value = "Idempotency-Key", required = false) String key) {
    return ResponseEntityAssembler.from(
        commands.handle(
            new ConfirmSaleCommand(
                p.tenantId(),
                p.id(),
                r.lines() == null
                    ? List.of()
                    : r.lines().stream()
                        .map(
                            l ->
                                new ConfirmSaleCommand.Line(
                                    l.productId(), Quantity.positive(l.quantity())))
                        .toList(),
                key)),
        SaleResourceFromEntityAssembler::toResourceFromEntity,
        201);
  }

  @PostMapping("/{id}/cancel")
  public ResponseEntity<?> cancel(
      @AuthenticationPrincipal WorkspacePrincipal p,
      @PathVariable UUID id,
      @jakarta.validation.Valid @RequestBody CancelResource r) {
    return ResponseEntityAssembler.from(
        commands.handle(new CancelSaleCommand(p.tenantId(), p.id(), id, r.reason())),
        SaleResourceFromEntityAssembler::toResourceFromEntity,
        200);
  }
}
