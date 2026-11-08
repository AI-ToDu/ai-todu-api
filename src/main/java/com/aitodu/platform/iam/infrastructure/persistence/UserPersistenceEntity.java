package com.aitodu.platform.iam.infrastructure.persistence;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "users")
public class UserPersistenceEntity {
  @Id public UUID id;
  public UUID tenantId;
  public String name;
  public String email;
  public String passwordHash;
  public String role;
  public boolean active;
}
