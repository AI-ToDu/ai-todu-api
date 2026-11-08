package com.aitodu.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PlatformApplication {
  public static void main(String[] args) throws java.sql.SQLException {
    com.aitodu.platform.shared.infrastructure.provisioning.DatabaseProvisioner.runIfRequested();
    SpringApplication.run(PlatformApplication.class, args);
  }
}
