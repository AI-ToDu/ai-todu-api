package com.aitodu.platform.iam.infrastructure.security;

import com.aitodu.platform.iam.application.PasswordHasher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BCryptPasswordHasher implements PasswordHasher {
  private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

  public String hash(String raw) {
    return encoder.encode(raw);
  }

  public boolean matches(String raw, String encoded) {
    return raw != null && encoded != null && encoder.matches(raw, encoded);
  }
}
