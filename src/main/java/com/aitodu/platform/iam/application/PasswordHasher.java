package com.aitodu.platform.iam.application;

public interface PasswordHasher {
  String hash(String raw);

  boolean matches(String raw, String encoded);
}
