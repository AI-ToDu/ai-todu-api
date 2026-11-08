package com.aitodu.platform.iam.infrastructure.persistence;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "workspaces")
public class WorkspacePersistenceEntity {
  @Id public UUID id;
  public String name;
}
