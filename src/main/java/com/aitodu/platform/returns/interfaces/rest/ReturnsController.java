package com.aitodu.platform.returns.interfaces.rest;

import com.aitodu.platform.iam.infrastructure.security.WorkspacePrincipal;
import com.aitodu.platform.returns.application.ReturnCommandService;
import com.aitodu.platform.returns.application.ReturnQueryService;
import com.aitodu.platform.returns.domain.model.commands.RecordReturnCommand;
import com.aitodu.platform.returns.domain.model.queries.*;
import com.aitodu.platform.returns.interfaces.rest.transform.ReturnResourceFromEntityAssembler;
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
      @jakarta.validation.constraints.NotNull UUID saleId,
      @jakarta.validation.constraints.NotNull UUID saleLineId,
      @jakarta.validation.constraints.NotNull @jakarta.validation.constraints.Positive
          BigDecimal quantity,
      @jakarta.validation.constraints.NotBlank String disposition,
      @jakarta.validation.constraints.NotBlank String reason) {}

  public record ReturnResource(
      UUID id,
      UUID saleId,
      UUID saleLineId,
      int quantity,
      String disposition,
      String reason,
      Instant createdAt) {}

  private final ReturnCommandService commands;
  private final ReturnQueryService queries;

  public ReturnsController(ReturnCommandService commands, ReturnQueryService queries) {
    this.commands = commands;
    this.queries = queries;
  }

  @GetMapping
  public CollectionResource<ReturnResource> list(@AuthenticationPrincipal WorkspacePrincipal p) {
    return new CollectionResource<>(
        queries.handle(new GetReturnsQuery(p.tenantId())).stream()
            .map(ReturnResourceFromEntityAssembler::toResourceFromEntity)
            .toList());
  }

  @PostMapping
  public ResponseEntity<?> record(
      @AuthenticationPrincipal WorkspacePrincipal p,
      @jakarta.validation.Valid @RequestBody RecordReturnResource r) {
    return ResponseEntityAssembler.from(
        commands.handle(
            new RecordReturnCommand(
                p.tenantId(),
                p.id(),
                r.saleId(),
                r.saleLineId(),
                Quantity.positive(r.quantity()).value(),
                r.disposition(),
                r.reason())),
        ReturnResourceFromEntityAssembler::toResourceFromEntity,
        201);
  }
}
