package com.aitodu.platform.shared.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record Money(long cents) {
  public Money {
    DomainViolation.require(cents >= 0, "INVALID_MONEY");
  }

  public static Money of(BigDecimal amount) {
    try {
      return new Money(
          amount.setScale(2, RoundingMode.UNNECESSARY).movePointRight(2).longValueExact());
    } catch (ArithmeticException | NullPointerException e) {
      throw new DomainViolation("INVALID_MONEY");
    }
  }

  public BigDecimal amount() {
    return BigDecimal.valueOf(cents, 2);
  }

  public Money times(int quantity) {
    return new Money(Math.multiplyExact(cents, quantity));
  }
}
