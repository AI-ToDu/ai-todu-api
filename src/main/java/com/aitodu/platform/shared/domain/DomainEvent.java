package com.aitodu.platform.shared.domain;

import java.time.Instant;
import java.util.UUID;

public interface DomainEvent {
  UUID eventId();

  UUID tenantId();

  Instant occurredAt();
}
