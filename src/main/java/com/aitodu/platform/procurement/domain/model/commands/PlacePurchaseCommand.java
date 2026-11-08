package com.aitodu.platform.procurement.domain.model.commands;

import com.aitodu.platform.shared.domain.Quantity;
import java.util.*;

public record PlacePurchaseCommand(UUID tenantId, UUID supplierId, List<Line> lines) {
  public PlacePurchaseCommand {
    lines = lines == null ? List.of() : List.copyOf(lines);
  }

  public record Line(UUID productId, Quantity quantity) {}
}
