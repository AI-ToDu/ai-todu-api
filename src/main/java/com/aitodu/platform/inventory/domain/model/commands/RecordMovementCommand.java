package com.aitodu.platform.inventory.domain.model.commands;

import com.aitodu.platform.shared.domain.*;
import java.util.UUID;

public record RecordMovementCommand(
    UUID tenantId, UUID actorId, UUID productId, Quantity quantity, String type, String reason) {
  public RecordMovementCommand {
    reason = Text.required(reason, 500);
    DomainViolation.require(
        java.util.Set.of("RECEIPT", "ISSUE", "LOSS").contains(type == null ? "" : type),
        "INVALID_STATUS");
  }
}
