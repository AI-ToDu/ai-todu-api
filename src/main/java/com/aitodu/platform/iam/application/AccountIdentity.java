package com.aitodu.platform.iam.application;

import java.util.UUID;

/** Authenticated identity exposed without a dependency on the security adapter. */
public interface AccountIdentity {
  UUID id();

  UUID tenantId();

  String name();

  String email();

  String role();
}
