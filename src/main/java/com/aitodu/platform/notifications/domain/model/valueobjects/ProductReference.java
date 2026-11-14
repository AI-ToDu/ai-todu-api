package com.aitodu.platform.notifications.domain.model.valueobjects;

import java.util.Objects;
import java.util.UUID;

/** Product identity received through the published integration contract. */
public record ProductReference(UUID value) {
  public ProductReference {
    Objects.requireNonNull(value, "Product reference is required");
  }
}
