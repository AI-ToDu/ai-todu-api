package com.aitodu.platform.sales.domain.repositories;

import com.aitodu.platform.sales.domain.model.aggregates.Sale;
import java.util.*;

public interface SaleRepository {
  Optional<Sale> find(UUID tenant, UUID id);

  List<Sale> findAll(UUID tenant);

  void save(Sale sale);

  Optional<Sale> findByRequestKey(UUID tenant, String key);

  void remember(UUID sale, String key, String fingerprint);

  String fingerprint(UUID sale);
}
