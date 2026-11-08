package com.aitodu.platform.shared.domain;

public final class DomainViolation extends RuntimeException {
  private final String code;

  public DomainViolation(String code) {
    super(code);
    this.code = code;
  }

  public String code() {
    return code;
  }

  public static void require(boolean condition, String code) {
    if (!condition) throw new DomainViolation(code);
  }
}
