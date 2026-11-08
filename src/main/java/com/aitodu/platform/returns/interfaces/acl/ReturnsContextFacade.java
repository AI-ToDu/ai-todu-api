package com.aitodu.platform.returns.interfaces.acl;

import java.util.UUID;

public interface ReturnsContextFacade {
  long recordedLossCents(UUID tenant);
}
