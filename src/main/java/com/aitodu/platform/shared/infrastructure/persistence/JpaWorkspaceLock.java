package com.aitodu.platform.shared.infrastructure.persistence;

import com.aitodu.platform.shared.application.WorkspaceLock;
import com.aitodu.platform.shared.domain.DomainViolation;
import jakarta.persistence.*;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class JpaWorkspaceLock implements WorkspaceLock {
  @PersistenceContext private EntityManager entities;

  public void create(UUID tenant) {
    entities.persist(new WorkspaceLockEntity(tenant));
  }

  public void acquire(UUID tenant) {
    DomainViolation.require(
        entities.find(WorkspaceLockEntity.class, tenant, LockModeType.PESSIMISTIC_WRITE) != null,
        "NOT_FOUND");
  }
}
