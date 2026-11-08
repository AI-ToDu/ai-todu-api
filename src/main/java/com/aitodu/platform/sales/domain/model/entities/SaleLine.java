package com.aitodu.platform.sales.domain.model.entities;

import com.aitodu.platform.shared.domain.*;
import java.util.UUID;

public record SaleLine(
    UUID id,
    UUID productId,
    String name,
    String sku,
    int quantity,
    Money price,
    Money cost,
    int returned) {
  public SaleLine {
    DomainViolation.require(
        quantity > 0 && returned >= 0 && returned <= quantity, "INVALID_QUANTITY");
    name = Text.required(name, 160);
  }

  public SaleLine returnUnits(int amount) {
    DomainViolation.require(amount > 0 && amount <= quantity - returned, "EXCESS_RETURN");
    return new SaleLine(id, productId, name, sku, quantity, price, cost, returned + amount);
  }
}
