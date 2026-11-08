package com.aitodu.platform.logistics.domain.repositories;

import com.aitodu.platform.logistics.domain.model.aggregates.DispatchManifest;
import java.util.*;

public interface DispatchRepository {
  Optional<DispatchManifest> find(UUID tenant, UUID id);

  List<DispatchManifest> findAll(UUID tenant);

  void save(DispatchManifest manifest);

  boolean busy(UUID tenant, UUID driver, UUID vehicle);
}
