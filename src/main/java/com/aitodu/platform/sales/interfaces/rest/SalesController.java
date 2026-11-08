package com.aitodu.platform.sales.interfaces.rest;

import static com.aitodu.platform.sales.interfaces.rest.SaleResources.*;

import com.aitodu.platform.iam.infrastructure.security.WorkspacePrincipal;
import com.aitodu.platform.sales.application.internal.SaleService;
import com.aitodu.platform.sales.domain.model.commands.*;
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
  private final SaleService service;

  public SalesController(SaleService s) {
    service = s;
  }

  @GetMapping
  public CollectionResource<SaleResource> list(@AuthenticationPrincipal WorkspacePrincipal p) {
    return new CollectionResource<>(
        service.list(p.tenantId()).stream().map(SaleResource::from).toList());
  }

  @PostMapping
  public ResponseEntity<SaleResource> create(
      @AuthenticationPrincipal WorkspacePrincipal p,
      @RequestBody ConfirmSaleResource r,
      @RequestHeader(value = "Idempotency-Key", required = false) String key) {
    return ResponseEntityAssembler.from(
        service.handle(
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
        SaleResource::from,
        201);
  }

  @PostMapping("/{id}/cancel")
  public ResponseEntity<SaleResource> cancel(
      @AuthenticationPrincipal WorkspacePrincipal p,
      @PathVariable UUID id,
      @RequestBody CancelResource r) {
    return ResponseEntityAssembler.from(
        service.handle(new CancelSaleCommand(p.tenantId(), p.id(), id, r.reason())),
        SaleResource::from,
        200);
  }
}
