package com.aitodu.platform.procurement.application.internal;

import com.aitodu.platform.inventory.interfaces.acl.InventoryContextFacade;
import com.aitodu.platform.procurement.application.ProcurementCommandService;
import com.aitodu.platform.procurement.application.ProcurementQueryService;
import com.aitodu.platform.procurement.domain.model.aggregates.*;
import com.aitodu.platform.procurement.domain.model.commands.*;
import com.aitodu.platform.procurement.domain.model.entities.PurchaseLine;
import com.aitodu.platform.procurement.domain.model.queries.*;
import com.aitodu.platform.procurement.domain.repositories.*;
import com.aitodu.platform.shared.application.*;
import com.aitodu.platform.shared.application.result.Result;
import com.aitodu.platform.shared.domain.*;
import java.time.Instant;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProcurementService implements ProcurementCommandService, ProcurementQueryService {

  private final PurchaseRepository orders;
  private final SupplierRepository suppliers;
  private final InventoryContextFacade inventory;
  private final CommandExecutor commands;

  public ProcurementService(
      PurchaseRepository o, SupplierRepository s, InventoryContextFacade i, CommandExecutor c) {
    orders = o;
    suppliers = s;
    inventory = i;
    commands = c;
  }

  public Result<Supplier> handle(RegisterSupplierCommand c) {
    UUID t = c.tenantId();
    String name = c.name(), contact = c.contact();
    return commands.execute(
        t,
        () -> {
          var a = new Supplier(UUID.randomUUID(), t, name, contact);
          suppliers.save(a);
          return a;
        });
  }

  public Result<PurchaseOrder> handle(PlacePurchaseCommand c) {
    UUID t = c.tenantId(), supplier = c.supplierId();
    var lines = c.lines();
    return commands.execute(
        t,
        () -> {
          suppliers.find(t, supplier).orElseThrow(() -> new DomainViolation("NOT_FOUND"));
          var detail =
              lines.stream()
                  .map(
                      l -> {
                        inventory.fetch(t, l.productId());
                        return new PurchaseLine(
                            UUID.randomUUID(), l.productId(), l.quantity().value(), 0);
                      })
                  .toList();
          var a = new PurchaseOrder(UUID.randomUUID(), t, supplier, detail, Instant.now());
          orders.save(a);
          return a;
        });
  }

  public Result<PurchaseOrder> handle(ReceivePurchaseCommand c) {
    UUID t = c.tenantId(), actor = c.actorId(), id = c.purchaseId();
    var lines = c.lines();
    return commands.execute(
        t,
        () -> {
          DomainViolation.require(
              lines.stream().map(ReceivePurchaseCommand.Line::lineId).distinct().count()
                  == lines.size(),
              "DUPLICATE_LINE");
          var a = orders.find(t, id).orElseThrow(() -> new DomainViolation("NOT_FOUND"));
          var quantities = new HashMap<UUID, Integer>();
          for (var l : lines) quantities.put(l.lineId(), l.quantity().value());
          a.receive(quantities);
          for (var l : lines) {
            var product =
                a.lines().stream().filter(x -> x.id().equals(l.lineId())).findFirst().orElseThrow();
            inventory.receive(
                t,
                actor,
                product.productId(),
                l.quantity().value(),
                "Purchase receipt " + a.number());
          }
          orders.save(a);
          return a;
        });
  }

  @Transactional(readOnly = true)
  public List<PurchaseOrder> orders(UUID t) {
    return orders.findAll(t);
  }

  @Transactional(readOnly = true)
  public List<Supplier> suppliers(UUID t) {
    return suppliers.findAll(t);
  }

  @Transactional(readOnly = true)
  public List<Supplier> handle(GetSuppliersQuery q) {
    return suppliers(q.tenantId());
  }

  @Transactional(readOnly = true)
  public List<PurchaseOrder> handle(GetPurchasesQuery q) {
    return orders(q.tenantId());
  }
}
