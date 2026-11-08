package com.aitodu.platform.logistics.domain.model.aggregates;

import com.aitodu.platform.shared.domain.Text;
import java.util.UUID;

public record Driver(UUID id, UUID tenantId, String name, String license) {
  public Driver {
    name = Text.required(name, 160);
    license = Text.required(license, 40);
  }
}
