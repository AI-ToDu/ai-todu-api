package com.aitodu.platform.sales.domain.model.commands;

import com.aitodu.platform.shared.domain.Quantity;
import java.util.*;

public record ConfirmSaleCommand(UUID tenantId, UUID actorId, List<Line> lines, String requestKey) {
  public ConfirmSaleCommand {
    lines = lines == null ? List.of() : List.copyOf(lines);
  }

  public record Line(UUID productId, Quantity quantity) {}
}
