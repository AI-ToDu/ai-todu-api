package com.aitodu.platform.reporting.application.internal;

import com.aitodu.platform.inventory.interfaces.acl.InventoryContextFacade;
import com.aitodu.platform.logistics.interfaces.acl.LogisticsContextFacade;
import com.aitodu.platform.reporting.application.MetricsQueryService;
import com.aitodu.platform.returns.interfaces.acl.ReturnsContextFacade;
import com.aitodu.platform.sales.interfaces.acl.SalesContextFacade;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MetricsQueryServiceImpl implements MetricsQueryService {

  private final InventoryContextFacade inventory;
  private final SalesContextFacade sales;
  private final LogisticsContextFacade logistics;
  private final ReturnsContextFacade returns;

  public MetricsQueryServiceImpl(
      InventoryContextFacade i,
      SalesContextFacade s,
      LogisticsContextFacade l,
      ReturnsContextFacade r) {
    inventory = i;
    sales = s;
    logistics = l;
    returns = r;
  }

  @Transactional(readOnly = true)
  public Metrics handle(com.aitodu.platform.reporting.domain.model.queries.GetMetricsQuery query) {
    UUID t = query.tenantId();
    var products = inventory.list(t);
    var commercial = sales.metrics(t);
    var valuation =
        products.stream()
            .map(p -> p.cost().multiply(BigDecimal.valueOf(p.quantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    return new Metrics(
        commercial.revenue(),
        commercial.contribution().subtract(BigDecimal.valueOf(returns.recordedLossCents(t), 2)),
        valuation,
        products.stream().filter(p -> p.quantity() <= p.minimum()).count(),
        logistics.activeDispatches(t),
        commercial.count());
  }
}
