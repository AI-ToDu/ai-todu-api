package com.aitodu.platform.logistics.interfaces.acl;

import java.util.UUID;

public interface LogisticsContextFacade {
  long activeDispatches(UUID tenant);
}
