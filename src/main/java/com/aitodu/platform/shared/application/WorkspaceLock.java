package com.aitodu.platform.shared.application;

import java.util.UUID;

public interface WorkspaceLock {
  void acquire(UUID tenantId);

  void create(UUID tenantId);
}
