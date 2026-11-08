package com.aitodu.platform.iam.domain.model.aggregates;

import com.aitodu.platform.iam.domain.model.valueobjects.EmailAddress;
import com.aitodu.platform.shared.domain.*;
import java.util.UUID;

public record UserAccount(
    UUID id,
    UUID tenantId,
    String name,
    EmailAddress email,
    String passwordHash,
    String role,
    boolean active) {
  public UserAccount {
    name = Text.required(name, 120);
    DomainViolation.require(
        role != null && java.util.Set.of("ADMIN", "WAREHOUSE", "SALES", "LOGISTICS").contains(role),
        "INVALID_ROLE");
  }
}
