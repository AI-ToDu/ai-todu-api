package com.aitodu.platform.returns.interfaces.rest;

import com.aitodu.platform.iam.infrastructure.security.WorkspacePrincipal;
import com.aitodu.platform.returns.application.internal.ReturnService;
import com.aitodu.platform.returns.domain.model.aggregates.CustomerReturn;
import com.aitodu.platform.returns.domain.model.commands.RecordReturnCommand;
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
@RequestMapping("/api/v1/returns")
@PreAuthorize("hasAnyRole('ADMIN','SALES')")
public class ReturnsController {
  public record RecordReturnResource(
      UUID saleId, UUID saleLineId, BigDecimal quantity, String disposition, String reason) {}

  public record ReturnResource(
      UUID id,
      UUID saleId,
      UUID saleLineId,
      int quantity,
      String disposition,
      String reason,
      Instant createdAt) {
    static ReturnResource from(CustomerReturn a) {
      return new ReturnResource(
          a.id(),
          a.saleId(),
          a.saleLineId(),
          a.quantity(),
          a.disposition(),
          a.reason(),
          a.createdAt());
    }
  }

  private final ReturnService service;

  public ReturnsController(ReturnService s) {
    service = s;
  }

  @GetMapping
  public CollectionResource<ReturnResource> list(@AuthenticationPrincipal WorkspacePrincipal p) {
    return new CollectionResource<>(
        service.list(p.tenantId()).stream().map(ReturnResource::from).toList());
  }

  @PostMapping
  public ResponseEntity<ReturnResource> record(
      @AuthenticationPrincipal WorkspacePrincipal p, @RequestBody RecordReturnResource r) {
    return ResponseEntityAssembler.from(
        service.handle(
            new RecordReturnCommand(
                p.tenantId(),
                p.id(),
                r.saleId(),
                r.saleLineId(),
                Quantity.positive(r.quantity()).value(),
                r.disposition(),
                r.reason())),
        ReturnResource::from,
        201);
  }
}
