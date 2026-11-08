package com.aitodu.platform.iam.interfaces.rest;

import com.aitodu.platform.iam.application.internal.EmployeeService;
import com.aitodu.platform.iam.domain.model.commands.CreateEmployeeCommand;
import com.aitodu.platform.iam.infrastructure.security.WorkspacePrincipal;
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
  private final EmployeeService service;

  public EmployeesController(EmployeeService s) {
    service = s;
  }

  public record CreateEmployeeResource(String name, String email, String role, String password) {}

  public record EmployeeResource(UUID id, String name, String email, String role, boolean active) {
    static EmployeeResource from(com.aitodu.platform.iam.domain.model.aggregates.UserAccount u) {
      return new EmployeeResource(u.id(), u.name(), u.email().value(), u.role(), u.active());
    }
  }

  @GetMapping
  public CollectionResource<EmployeeResource> list(@AuthenticationPrincipal WorkspacePrincipal p) {
    return new CollectionResource<>(
        service.list(p.tenantId()).stream().map(EmployeeResource::from).toList());
  }

  @PostMapping
  public ResponseEntity<EmployeeResource> create(
      @AuthenticationPrincipal WorkspacePrincipal p, @RequestBody CreateEmployeeResource r) {
    return ResponseEntityAssembler.from(
        service.handle(
            new CreateEmployeeCommand(p.tenantId(), r.name(), r.email(), r.role(), r.password())),
        EmployeeResource::from,
        201);
  }
}
