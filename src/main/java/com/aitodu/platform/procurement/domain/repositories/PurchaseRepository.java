package com.aitodu.platform.procurement.domain.repositories;

import com.aitodu.platform.procurement.domain.model.aggregates.PurchaseOrder;
import java.util.*;

public interface PurchaseRepository {
  Optional<PurchaseOrder> find(UUID tenant, UUID id);

  List<PurchaseOrder> findAll(UUID tenant);

  void save(PurchaseOrder order);
}
