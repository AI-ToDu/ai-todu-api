package com.aitodu.platform.logistics.domain.repositories;

import com.aitodu.platform.logistics.domain.model.aggregates.Driver;
import java.util.*;

public interface DriverRepository {
  Optional<Driver> find(UUID tenant, UUID id);

  List<Driver> findAll(UUID tenant);

  void save(Driver driver);
}
