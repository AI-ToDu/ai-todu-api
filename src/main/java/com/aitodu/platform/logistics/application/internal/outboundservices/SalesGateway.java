package com.aitodu.platform.logistics.application.internal.outboundservices;

import com.aitodu.platform.logistics.domain.model.entities.DispatchLine;
import com.aitodu.platform.sales.interfaces.acl.SalesContextFacade;
import java.util.*;
import org.springframework.stereotype.Component;

@Component
public class SalesGateway {
  private final SalesContextFacade sales;

  public SalesGateway(SalesContextFacade s) {
    sales = s;
  }

  public List<DispatchLine> claim(UUID tenant, UUID sale, UUID dispatch) {
    return sales.claim(tenant, sale, dispatch).lines().stream()
        .map(
            l ->
                new DispatchLine(
                    UUID.randomUUID(), sale, l.id(), l.productId(), l.name(), l.quantity()))
        .toList();
  }

  public void fulfillment(UUID t, UUID sale, UUID dispatch, String state) {
    sales.fulfillment(t, sale, dispatch, state);
  }

  public List<SalesContextFacade.SaleSnapshot> candidates(UUID t) {
    return sales.candidates(t);
  }
}
