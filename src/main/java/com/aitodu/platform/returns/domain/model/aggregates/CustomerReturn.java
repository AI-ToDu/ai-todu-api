package com.aitodu.platform.returns.domain.model.aggregates;

import com.aitodu.platform.shared.domain.*;
import java.time.Instant;
import java.util.UUID;

public record CustomerReturn(
    UUID id,
    UUID tenantId,
    UUID saleId,
    UUID saleLineId,
    UUID productId,
    int quantity,
    String disposition,
    String reason,
    Instant createdAt,
    long lossCents) {
  public CustomerReturn {
    DomainViolation.require(quantity > 0, "INVALID_QUANTITY");
    DomainViolation.require(
        java.util.Set.of("RESTOCK", "LOSS").contains(disposition == null ? "" : disposition),
        "INVALID_STATUS");
    reason = Text.required(reason, 500);
  }
}
