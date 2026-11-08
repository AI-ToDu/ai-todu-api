package com.aitodu.platform.logistics.domain.model.aggregates;

import com.aitodu.platform.shared.domain.Text;
import java.util.UUID;

public record Vehicle(UUID id, UUID tenantId, String name, String plate) {
  public Vehicle {
    name = Text.required(name, 160);
    plate = Text.required(plate, 20).toUpperCase(java.util.Locale.ROOT);
  }
}
