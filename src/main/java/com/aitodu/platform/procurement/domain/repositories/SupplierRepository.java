package com.aitodu.platform.procurement.domain.repositories;

import com.aitodu.platform.procurement.domain.model.aggregates.Supplier;
import java.util.*;

public interface SupplierRepository {
  Optional<Supplier> find(UUID tenant, UUID id);

  List<Supplier> findAll(UUID tenant);

  void save(Supplier supplier);
}
