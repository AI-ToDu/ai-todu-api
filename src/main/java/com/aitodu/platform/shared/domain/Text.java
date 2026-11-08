package com.aitodu.platform.shared.domain;

public final class Text {
  private Text() {}

  public static String required(String text, int max) {
    DomainViolation.require(
        text != null && !text.isBlank() && text.strip().length() <= max, "INVALID_TEXT");
    return text.strip();
  }
}
