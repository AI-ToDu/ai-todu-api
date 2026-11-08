package com.aitodu.platform.sales.interfaces.acl;

import java.math.BigDecimal;
import java.util.*;

public interface SalesContextFacade {
  record LineSnapshot(UUID id, UUID productId, String name, int quantity) {}

  record SaleSnapshot(UUID id, String number, List<LineSnapshot> lines) {}

  record SalesMetrics(BigDecimal revenue, BigDecimal contribution, int count) {}

  SaleSnapshot claim(UUID tenant, UUID sale, UUID dispatch);

  void fulfillment(UUID tenant, UUID sale, UUID dispatch, String state);

  List<SaleSnapshot> candidates(UUID tenant);

  SalesMetrics metrics(UUID tenant);

  record ReturnedLineSnapshot(UUID productId, long unitCostCents) {}

  ReturnedLineSnapshot returnUnits(UUID tenant, UUID sale, UUID line, int quantity);
}
