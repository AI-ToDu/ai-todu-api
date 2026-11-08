package com.aitodu.platform.iam.domain.model.commands;

import com.aitodu.platform.shared.domain.*;
import java.util.UUID;

public record CreateEmployeeCommand(
    UUID tenantId, String name, String email, String role, String password) {
  public CreateEmployeeCommand {
    DomainViolation.require(
        password != null
            && password.length() >= 8
            && password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length <= 72,
        "INVALID_PASSWORD");
    DomainViolation.require(
        role != null && java.util.Set.of("WAREHOUSE", "SALES", "LOGISTICS").contains(role),
        "INVALID_ROLE");
  }
}
