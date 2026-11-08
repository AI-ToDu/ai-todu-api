package com.aitodu.platform.iam.interfaces.rest;

import com.aitodu.platform.iam.domain.model.aggregates.UserAccount;
import com.aitodu.platform.iam.infrastructure.security.WorkspacePrincipal;
import java.util.UUID;

public final class AccountResources {
  private AccountResources() {}

  public record RegistrationResource(String name, String company, String email, String password) {}

  public record LoginResource(String email, String password) {}

  public record UserResource(UUID id, UUID tenantId, String name, String email, String role) {
    public static UserResource from(UserAccount u) {
      return new UserResource(u.id(), u.tenantId(), u.name(), u.email().value(), u.role());
    }

    public static UserResource from(WorkspacePrincipal p) {
      return new UserResource(p.id(), p.tenantId(), p.name(), p.email(), p.role());
    }
  }

  public record SessionResource(String token, UserResource user) {}
}
