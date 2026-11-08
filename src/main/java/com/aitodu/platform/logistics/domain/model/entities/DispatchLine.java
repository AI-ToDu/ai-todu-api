package com.aitodu.platform.logistics.domain.model.entities;

import com.aitodu.platform.shared.domain.DomainViolation;
import java.util.UUID;

public record DispatchLine(
    UUID id, UUID saleId, UUID saleLineId, UUID productId, String name, int quantity) {
  public DispatchLine {
    DomainViolation.require(quantity > 0, "INVALID_QUANTITY");
  }
}
