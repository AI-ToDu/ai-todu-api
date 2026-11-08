package com.aitodu.platform.logistics.domain.model.commands;

import java.util.*;

public record PrepareDispatchCommand(
    UUID tenantId, List<UUID> saleIds, UUID driverId, UUID vehicleId, String destination) {
  public PrepareDispatchCommand {
    saleIds = saleIds == null ? List.of() : List.copyOf(saleIds);
  }
}
