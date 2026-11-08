package com.aitodu.platform.iam.domain.model.valueobjects;

import com.aitodu.platform.shared.domain.DomainViolation;
import java.util.Locale;

public record EmailAddress(String value) {
  public EmailAddress {
    DomainViolation.require(
        value != null && value.length() <= 254 && value.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"),
        "INVALID_EMAIL");
    value = value.strip().toLowerCase(Locale.ROOT);
  }
}
