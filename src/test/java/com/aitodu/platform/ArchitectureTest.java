package com.aitodu.platform;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.*;
import org.junit.jupiter.api.Test;

class ArchitectureTest {
  @Test
  void domainIsFrameworkIndependentAndContextsUsePublicContracts() throws Exception {
    try (var files = Files.walk(Path.of("src/main/java/com/aitodu/platform"))) {
      for (var file : files.filter(p -> p.toString().endsWith(".java")).toList()) {
        var path = file.toString().replace('\\', '/');
        var text = Files.readString(file);
        if (path.contains("/domain/")) {
          assertFalse(text.contains("import org.springframework"), path);
          assertFalse(text.contains("import jakarta."), path);
          assertFalse(text.contains(".infrastructure."), path);
          assertFalse(text.contains(".interfaces."), path);
        }
        if (path.contains("/logistics/"))
          assertFalse(text.contains("import com.aitodu.platform.sales.domain"), path);
        if (path.contains("/sales/"))
          assertFalse(text.contains("import com.aitodu.platform.inventory.domain"), path);
        if (path.endsWith("Controller.java"))
          assertFalse(text.contains(".application.internal."), path);
        if (path.endsWith("Resources.java"))
          assertFalse(text.contains("static ") && text.contains(" from("), path);
      }
    }
  }
}
