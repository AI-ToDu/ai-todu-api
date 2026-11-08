package com.aitodu.platform.iam.interfaces.rest.transform;

import static com.aitodu.platform.iam.interfaces.rest.AccountResources.*;

import com.aitodu.platform.iam.domain.model.aggregates.UserAccount;

public final class UserResourceFromEntityAssembler {
  private UserResourceFromEntityAssembler() {}

  public static UserResource toResourceFromEntity(UserAccount u) {
    return new UserResource(u.id(), u.tenantId(), u.name(), u.email().value(), u.role());
  }

  public static UserResource toResourceFromEntity(
      com.aitodu.platform.iam.application.AccountIdentity p) {
    return new UserResource(p.id(), p.tenantId(), p.name(), p.email(), p.role());
  }
}
