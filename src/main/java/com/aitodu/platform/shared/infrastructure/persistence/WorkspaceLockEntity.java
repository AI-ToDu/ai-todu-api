package com.aitodu.platform.shared.infrastructure.persistence;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "workspace_locks")
public class WorkspaceLockEntity {
  @Id public UUID id;

  protected WorkspaceLockEntity() {}

  public WorkspaceLockEntity(UUID id) {
    this.id = id;
  }
}
