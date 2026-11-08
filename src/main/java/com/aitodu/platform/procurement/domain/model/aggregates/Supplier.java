package com.aitodu.platform.procurement.domain.model.aggregates;

import com.aitodu.platform.shared.domain.Text;
import java.util.UUID;

public record Supplier(UUID id, UUID tenantId, String name, String contact) {
  public Supplier {
    name = Text.required(name, 160);
    contact = Text.required(contact, 240);
  }
}
