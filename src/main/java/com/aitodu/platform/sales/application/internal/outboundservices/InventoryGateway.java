package com.aitodu.platform.sales.application.internal.outboundservices;

import com.aitodu.platform.inventory.interfaces.acl.InventoryContextFacade;
import com.aitodu.platform.sales.domain.model.entities.SaleLine;
import com.aitodu.platform.shared.domain.Money;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class InventoryGateway {
  private final InventoryContextFacade inventory;

  public InventoryGateway(InventoryContextFacade i) {
    inventory = i;
  }

  public SaleLine take(UUID tenant, UUID actor, UUID product, int quantity, String reason) {
    var p = inventory.fetch(tenant, product);
    inventory.issueForSale(tenant, actor, product, quantity, reason);
    return new SaleLine(
        UUID.randomUUID(),
        p.id(),
        p.name(),
        p.sku(),
        quantity,
        Money.of(p.price()),
        Money.of(p.cost()),
        0);
  }

  public void restore(UUID tenant, UUID actor, UUID product, int quantity, String reason) {
    inventory.receive(tenant, actor, product, quantity, reason);
  }
}
