package com.aitodu.platform.shared.domain;

import java.math.BigDecimal;

public record Quantity(int value) {
  public Quantity {
    DomainViolation.require(value >= 0, "INVALID_QUANTITY");
  }

  public static Quantity positive(BigDecimal value) {
    try {
      var result = new Quantity(value.intValueExact());
      DomainViolation.require(result.value > 0, "INVALID_QUANTITY");
      return result;
    } catch (ArithmeticException | NullPointerException e) {
      throw new DomainViolation("INVALID_QUANTITY");
    }
  }
}
