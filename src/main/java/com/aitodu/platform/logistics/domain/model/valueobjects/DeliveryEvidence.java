package com.aitodu.platform.logistics.domain.model.valueobjects;

import com.aitodu.platform.shared.domain.Text;
import java.time.Instant;
import java.util.Objects;

public record DeliveryEvidence(String recipient, Instant deliveredAt) {
  public DeliveryEvidence {
    recipient = Text.required(recipient, 160);
    Objects.requireNonNull(deliveredAt);
  }
}
