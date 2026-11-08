package com.aitodu.platform.iam.application.internal;

import com.aitodu.platform.iam.application.EmployeeCommandService;
import com.aitodu.platform.iam.application.EmployeeQueryService;
import com.aitodu.platform.iam.application.PasswordHasher;
import com.aitodu.platform.iam.domain.model.aggregates.UserAccount;
import com.aitodu.platform.iam.domain.model.commands.CreateEmployeeCommand;
import com.aitodu.platform.iam.domain.model.queries.*;
import com.aitodu.platform.iam.domain.model.valueobjects.EmailAddress;
import com.aitodu.platform.iam.domain.repositories.AccountRepository;
import com.aitodu.platform.shared.application.*;
import com.aitodu.platform.shared.application.result.Result;
import com.aitodu.platform.shared.domain.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmployeeService implements EmployeeCommandService, EmployeeQueryService {
  private final AccountRepository accounts;
  private final PasswordHasher passwords;
  private final CommandExecutor commands;

  public EmployeeService(AccountRepository a, PasswordHasher p, CommandExecutor c) {
    accounts = a;
    passwords = p;
    commands = c;
  }

  @Transactional(readOnly = true)
  public List<UserAccount> list(UUID tenant) {
    return accounts.findAll(tenant);
  }

  public Result<UserAccount> handle(CreateEmployeeCommand c) {
    return commands.execute(
        c.tenantId(),
        () -> {
          var email = new EmailAddress(c.email());
          DomainViolation.require(accounts.findByEmail(email).isEmpty(), "EMAIL_EXISTS");
          var user =
              new UserAccount(
                  UUID.randomUUID(),
                  c.tenantId(),
                  c.name(),
                  email,
                  passwords.hash(c.password()),
                  c.role(),
                  true);
          accounts.save(user);
          return user;
        });
  }

  @Transactional(readOnly = true)
  public List<UserAccount> handle(GetEmployeesQuery q) {
    return list(q.tenantId());
  }
}
