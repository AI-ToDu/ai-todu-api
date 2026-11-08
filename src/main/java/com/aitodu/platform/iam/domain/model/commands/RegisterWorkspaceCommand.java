package com.aitodu.platform.iam.domain.model.commands;

import com.aitodu.platform.shared.domain.*;

public record RegisterWorkspaceCommand(String name, String company, String email, String password) {
  public RegisterWorkspaceCommand {
    name = Text.required(name, 120);
    company = Text.required(company, 120);
    DomainViolation.require(
        password != null
            && password.length() >= 8
            && password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length <= 72,
        "INVALID_PASSWORD");
  }
}
