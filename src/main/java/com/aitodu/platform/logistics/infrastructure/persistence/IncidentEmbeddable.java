package com.aitodu.platform.logistics.infrastructure.persistence;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Embeddable
public class IncidentEmbeddable {
  public UUID id;
  public String description;
  public Instant createdAt;
}
