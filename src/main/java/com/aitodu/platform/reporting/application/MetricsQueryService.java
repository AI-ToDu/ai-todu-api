package com.aitodu.platform.reporting.application;

import java.math.BigDecimal;

public interface MetricsQueryService {
  public record Metrics(
      BigDecimal revenue,
      BigDecimal grossMargin,
      BigDecimal inventoryValue,
      long lowStock,
      long dispatches,
      long sales) {}

  Metrics handle(com.aitodu.platform.reporting.domain.model.queries.GetMetricsQuery query);
}
