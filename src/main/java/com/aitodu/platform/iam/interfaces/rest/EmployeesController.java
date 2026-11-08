package com.aitodu.platform.iam.interfaces.rest;

import com.aitodu.platform.iam.application.EmployeeCommandService;
import com.aitodu.platform.iam.application.EmployeeQueryService;
import com.aitodu.platform.iam.domain.model.commands.CreateEmployeeCommand;
import com.aitodu.platform.iam.domain.model.queries.*;
import com.aitodu.platform.iam.infrastructure.security.WorkspacePrincipal;
import com.aitodu.platform.iam.interfaces.rest.transform.EmployeeResourceFromEntityAssembler;
import com.aitodu.platform.shared.interfaces.rest.*;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/employees")
@PreAuthorize("hasRole('ADMIN')")
public class EmployeesController {
  private final EmployeeCommandService commands;
  private final EmployeeQueryService queries;

  public EmployeesController(EmployeeCommandService commands, EmployeeQueryService queries) {
    this.commands = commands;
    this.queries = queries;
  }

  public record CreateEmployeeResource(
      @jakarta.validation.constraints.NotBlank String name,
      @jakarta.validation.constraints.NotBlank @jakarta.validation.constraints.Email String email,
      @jakarta.validation.constraints.NotBlank String role,
      @jakarta.validation.constraints.NotBlank String password) {}

  public record EmployeeResource(UUID id, String name, String email, String role, boolean active) {}

  @GetMapping
  public CollectionResource<EmployeeResource> list(@AuthenticationPrincipal WorkspacePrincipal p) {
    return new CollectionResource<>(
        queries.handle(new GetEmployeesQuery(p.tenantId())).stream()
            .map(EmployeeResourceFromEntityAssembler::toResourceFromEntity)
            .toList());
  }

  @PostMapping
  public ResponseEntity<?> create(
      @AuthenticationPrincipal WorkspacePrincipal p,
      @jakarta.validation.Valid @RequestBody CreateEmployeeResource r) {
    return ResponseEntityAssembler.from(
        commands.handle(
            new CreateEmployeeCommand(p.tenantId(), r.name(), r.email(), r.role(), r.password())),
        EmployeeResourceFromEntityAssembler::toResourceFromEntity,
        201);
  }
}
