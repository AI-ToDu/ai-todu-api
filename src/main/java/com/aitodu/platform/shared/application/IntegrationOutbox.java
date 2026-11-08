package com.aitodu.platform.shared.application;

import java.time.Instant;
import java.util.*;

public interface IntegrationOutbox {
  void append(
      UUID eventId,
      UUID tenantId,
      String type,
      int version,
      Instant occurredAt,
      Map<String, Object> payload);
}
