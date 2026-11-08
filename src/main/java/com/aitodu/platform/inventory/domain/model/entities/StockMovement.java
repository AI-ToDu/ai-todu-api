package com.aitodu.platform.inventory.domain.model.entities;

import com.aitodu.platform.shared.domain.Text;
import java.time.Instant;
import java.util.UUID;

public record StockMovement(
    UUID id,
    UUID tenantId,
    UUID productId,
    int delta,
    String type,
    String reason,
    UUID actorId,
    Instant createdAt) {
  public StockMovement {
    reason = Text.required(reason, 500);
  }
}
