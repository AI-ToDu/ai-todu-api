package com.aitodu.platform.inventory.application.internal;

import com.aitodu.platform.inventory.application.InventoryCommandService;
import com.aitodu.platform.inventory.application.InventoryQueryService;
import com.aitodu.platform.inventory.domain.model.aggregates.InventoryItem;
import com.aitodu.platform.inventory.domain.model.commands.*;
import com.aitodu.platform.inventory.domain.model.entities.StockMovement;
import com.aitodu.platform.inventory.domain.model.queries.*;
import com.aitodu.platform.inventory.domain.repositories.InventoryRepository;
import com.aitodu.platform.inventory.interfaces.acl.InventoryContextFacade;
import com.aitodu.platform.shared.application.*;
import com.aitodu.platform.shared.application.result.Result;
import com.aitodu.platform.shared.domain.*;
import java.time.Instant;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryService
    implements InventoryCommandService, InventoryContextFacade, InventoryQueryService {
  private final InventoryRepository repository;
  private final CommandExecutor commands;
  private final com.aitodu.platform.inventory.application.internal.eventhandlers
          .InventoryIntegrationPublisher
      events;

  public InventoryService(
      InventoryRepository r,
      CommandExecutor c,
      com.aitodu.platform.inventory.application.internal.eventhandlers.InventoryIntegrationPublisher
          e) {
    repository = r;
    commands = c;
    events = e;
  }

  public Result<InventoryItem> handle(CreateProductCommand c) {
    return commands.execute(
        c.tenantId(),
        () -> {
          DomainViolation.require(!repository.exists(c.tenantId(), c.sku()), "SKU_EXISTS");
          var a =
              InventoryItem.create(
                  c.tenantId(), c.sku(), c.name(), c.price(), c.cost(), c.minimum());
          repository.save(a);
          return a;
        });
  }

  public Result<InventoryItem> handle(RecordMovementCommand c) {
    return commands.execute(
        c.tenantId(),
        () ->
            move(
                c.tenantId(),
                c.actorId(),
                c.productId(),
                c.quantity().value(),
                c.type(),
                c.reason(),
                false));
  }

  public Result<InventoryItem> handle(ChangeConservationCommand c) {
    UUID tenant = c.tenantId(), id = c.productId();
    String state = c.conservation();
    return commands.execute(
        tenant,
        () -> {
          var a = load(tenant, id);
          a.changeConservation(state);
          repository.save(a);
          return a;
        });
  }

  private InventoryItem load(UUID tenant, UUID id) {
    return repository.find(tenant, id).orElseThrow(() -> new DomainViolation("NOT_FOUND"));
  }

  private InventoryItem move(
      UUID tenant, UUID actor, UUID id, int quantity, String type, String reason, boolean sale) {
    var a = load(tenant, id);
    var q = new Quantity(quantity);
    boolean incoming =
        type.equals("RECEIPT")
            || type.equals("CANCELLATION")
            || type.equals("RETURN")
            || type.equals("PURCHASE_RECEIPT");
    if (incoming) a.receive(q);
    else a.issue(q, sale);
    repository.save(a);
    repository.record(
        new StockMovement(
            UUID.randomUUID(),
            tenant,
            id,
            incoming ? quantity : -quantity,
            type,
            reason,
            actor,
            Instant.now()));
    events.stage(a);
    return a;
  }

  @Transactional(readOnly = true)
  public ProductSnapshot fetch(UUID t, UUID id) {
    return snapshot(load(t, id));
  }

  @Transactional(readOnly = true)
  public List<ProductSnapshot> list(UUID t) {
    return repository.findAll(t).stream().map(InventoryService::snapshot).toList();
  }

  public void issueForSale(UUID t, UUID a, UUID p, int q, String reason) {
    move(t, a, p, q, "SALE", reason, true);
  }

  public void receive(UUID t, UUID a, UUID p, int q, String reason) {
    move(t, a, p, q, "RECEIPT", reason, false);
  }

  private static ProductSnapshot snapshot(InventoryItem a) {
    return new ProductSnapshot(
        a.id(),
        a.name(),
        a.sku().value(),
        a.quantity().value(),
        a.price().amount(),
        a.cost().amount(),
        a.minimum(),
        a.conservation());
  }

  @Transactional(readOnly = true)
  public List<StockMovement> movements(UUID t) {
    return repository.movements(t);
  }

  @Transactional(readOnly = true)
  public List<InventoryItem> handle(GetInventoryQuery q) {
    return repository.findAll(q.tenantId());
  }

  @Transactional(readOnly = true)
  public List<StockMovement> handle(GetMovementsQuery q) {
    return movements(q.tenantId());
  }
}
