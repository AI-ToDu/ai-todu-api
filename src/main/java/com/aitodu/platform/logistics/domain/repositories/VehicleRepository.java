package com.aitodu.platform.logistics.domain.repositories;

import com.aitodu.platform.logistics.domain.model.aggregates.Vehicle;
import java.util.*;

public interface VehicleRepository {
  Optional<Vehicle> find(UUID tenant, UUID id);

  List<Vehicle> findAll(UUID tenant);

  void save(Vehicle vehicle);
}
