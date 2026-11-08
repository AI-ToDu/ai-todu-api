package com.aitodu.platform.logistics.infrastructure.persistence;

import jakarta.persistence.*;
import java.time.Instant;

@Embeddable
public class DeliveryEvidenceEmbeddable {
  public String recipient;
  public Instant deliveredAt;
}
