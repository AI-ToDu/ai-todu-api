package com.aitodu.platform.procurement.domain.model.commands;

import com.aitodu.platform.shared.domain.Quantity;
import java.util.*;

public record ReceivePurchaseCommand(
    UUID tenantId, UUID actorId, UUID purchaseId, List<Line> lines) {
  public ReceivePurchaseCommand {
    lines = lines == null ? List.of() : List.copyOf(lines);
  }

  public record Line(UUID lineId, Quantity quantity) {}
}
