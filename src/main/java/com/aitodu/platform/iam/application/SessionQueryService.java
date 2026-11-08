package com.aitodu.platform.iam.application;

public interface SessionQueryService {
  AccountIdentity authenticate(String token);
}
