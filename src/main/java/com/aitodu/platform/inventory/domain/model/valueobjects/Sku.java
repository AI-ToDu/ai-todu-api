package com.aitodu.platform.inventory.domain.model.valueobjects;

import com.aitodu.platform.shared.domain.DomainViolation;
import java.util.Locale;

public record Sku(String value) {
  public Sku {
    DomainViolation.require(
        value != null && value.strip().matches("[A-Za-z0-9-]{2,32}"), "INVALID_SKU");
    value = value.strip().toUpperCase(Locale.ROOT);
  }
}
