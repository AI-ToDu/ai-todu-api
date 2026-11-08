package com.aitodu.platform.shared.application;

import com.aitodu.platform.shared.application.result.Result;
import java.util.UUID;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

@Component
public class CommandExecutor {
  private final TransactionTemplate transactions;
  private final WorkspaceLock locks;

  public CommandExecutor(
      org.springframework.transaction.PlatformTransactionManager manager, WorkspaceLock locks) {
    this.transactions = new TransactionTemplate(manager);
    this.locks = locks;
  }

  public <T> Result<T> execute(UUID tenant, Supplier<T> work) {
    return Result.attempt(
        () ->
            transactions.execute(
                status -> {
                  locks.acquire(tenant);
                  return work.get();
                }));
  }

  public <T> Result<T> create(Supplier<T> work) {
    return Result.attempt(() -> transactions.execute(status -> work.get()));
  }
}
