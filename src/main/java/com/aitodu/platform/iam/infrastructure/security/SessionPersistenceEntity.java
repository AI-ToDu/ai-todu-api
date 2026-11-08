package com.aitodu.platform.iam.infrastructure.security;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "sessions")
public class SessionPersistenceEntity {
  @Id public String tokenHash;
  public UUID userId;
  public Instant expiresAt;
}
