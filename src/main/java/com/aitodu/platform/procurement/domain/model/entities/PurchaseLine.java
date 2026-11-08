package com.aitodu.platform.procurement.domain.model.entities;

import com.aitodu.platform.shared.domain.*;
import java.util.UUID;

public record PurchaseLine(UUID id, UUID productId, int quantity, int received) {
  public PurchaseLine {
    DomainViolation.require(
        quantity > 0 && received >= 0 && received <= quantity, "INVALID_QUANTITY");
  }

  public PurchaseLine receive(int units) {
    DomainViolation.require(units > 0 && units <= quantity - received, "EXCESS_RECEIPT");
    return new PurchaseLine(id, productId, quantity, received + units);
  }
}
