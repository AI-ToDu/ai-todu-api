package com.aitodu.platform.shared.interfaces.events;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record IntegrationMessage(
    UUID eventId,
    UUID tenantId,
    String type,
    int version,
    Instant occurredAt,
    Map<String, Object> data) {
  public IntegrationMessage {
    data = Map.copyOf(data);
  }
}
