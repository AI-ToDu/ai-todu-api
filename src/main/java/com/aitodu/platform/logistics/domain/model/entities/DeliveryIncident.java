package com.aitodu.platform.logistics.domain.model.entities;

import com.aitodu.platform.shared.domain.Text;
import java.time.Instant;
import java.util.UUID;

public record DeliveryIncident(UUID id, String description, Instant createdAt) {
  public DeliveryIncident {
    description = Text.required(description, 500);
  }
}
