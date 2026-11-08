package com.aitodu.platform.iam.application.internal;

import com.aitodu.platform.iam.application.*;
import com.aitodu.platform.iam.domain.model.aggregates.UserAccount;
import com.aitodu.platform.iam.domain.model.commands.RegisterWorkspaceCommand;
import com.aitodu.platform.iam.domain.model.valueobjects.EmailAddress;
import com.aitodu.platform.iam.domain.repositories.AccountRepository;
import com.aitodu.platform.shared.application.*;
import com.aitodu.platform.shared.application.result.Result;
import com.aitodu.platform.shared.domain.*;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class AccountCommandServiceImpl implements AccountCommandService {
  private final AccountRepository accounts;
  private final PasswordHasher passwords;
  private final CommandExecutor commands;
  private final WorkspaceLock locks;

  public AccountCommandServiceImpl(
      AccountRepository a, PasswordHasher p, CommandExecutor c, WorkspaceLock l) {
    accounts = a;
    passwords = p;
    commands = c;
    locks = l;
  }

  public Result<UserAccount> handle(RegisterWorkspaceCommand command) {
    return commands.create(
        () -> {
          var email = new EmailAddress(command.email());
          DomainViolation.require(accounts.findByEmail(email).isEmpty(), "EMAIL_EXISTS");
          var tenant = UUID.randomUUID();
          accounts.createWorkspace(tenant, command.company());
          locks.create(tenant);
          var user =
              new UserAccount(
                  UUID.randomUUID(),
                  tenant,
                  command.name(),
                  email,
                  passwords.hash(command.password()),
                  "ADMIN",
                  true);
          accounts.save(user);
          return user;
        });
  }
}
