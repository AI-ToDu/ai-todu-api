package com.aitodu.platform.iam.infrastructure.persistence;

import com.aitodu.platform.iam.domain.model.aggregates.UserAccount;
import com.aitodu.platform.iam.domain.model.valueobjects.EmailAddress;

public final class AccountPersistenceAssembler {
  private AccountPersistenceAssembler() {}

  public static UserAccount toDomain(UserPersistenceEntity row) {
    return new UserAccount(
        row.id,
        row.tenantId,
        row.name,
        new EmailAddress(row.email),
        row.passwordHash,
        row.role,
        row.active);
  }

  public static UserPersistenceEntity toPersistence(UserAccount u) {
    var r = new UserPersistenceEntity();
    r.id = u.id();
    r.tenantId = u.tenantId();
    r.name = u.name();
    r.email = u.email().value();
    r.passwordHash = u.passwordHash();
    r.role = u.role();
    r.active = u.active();
    return r;
  }
}
