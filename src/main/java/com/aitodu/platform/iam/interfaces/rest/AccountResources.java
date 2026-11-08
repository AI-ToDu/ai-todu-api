package com.aitodu.platform.iam.interfaces.rest;

import java.util.UUID;

public final class AccountResources {
  private AccountResources() {}

  public record RegistrationResource(
      @jakarta.validation.constraints.NotBlank String name,
      @jakarta.validation.constraints.NotBlank String company,
      @jakarta.validation.constraints.NotBlank @jakarta.validation.constraints.Email String email,
      @jakarta.validation.constraints.NotBlank String password) {}

  public record LoginResource(
      @jakarta.validation.constraints.NotBlank @jakarta.validation.constraints.Email String email,
      @jakarta.validation.constraints.NotBlank String password) {}

  public record UserResource(UUID id, UUID tenantId, String name, String email, String role) {}

  public record SessionResource(String token, UserResource user) {}
}
