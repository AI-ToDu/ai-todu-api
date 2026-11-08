package com.aitodu.platform.iam.application;

public interface SessionCommandService {
  String login(String email, String password);

  void logout(String token);
}
