package com.aitodu.platform.sales.application.internal;

import com.aitodu.platform.sales.application.SaleCommandService;
import com.aitodu.platform.sales.application.internal.outboundservices.InventoryGateway;
import com.aitodu.platform.sales.domain.model.aggregates.Sale;
import com.aitodu.platform.sales.domain.model.commands.*;
import com.aitodu.platform.sales.domain.repositories.SaleRepository;
import com.aitodu.platform.sales.interfaces.acl.SalesContextFacade;
import com.aitodu.platform.shared.application.*;
import com.aitodu.platform.shared.application.result.Result;
import com.aitodu.platform.shared.domain.*;
import java.math.BigDecimal;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SaleService implements SaleCommandService, SalesContextFacade {
  private final SaleRepository repository;
  private final InventoryGateway inventory;
  private final CommandExecutor commands;
  private final com.aitodu.platform.sales.application.internal.eventhandlers
          .SaleIntegrationPublisher
      events;

  public SaleService(
      SaleRepository r,
      InventoryGateway i,
      CommandExecutor c,
      com.aitodu.platform.sales.application.internal.eventhandlers.SaleIntegrationPublisher e) {
    repository = r;
    inventory = i;
    commands = c;
    events = e;
  }

  public Result<Sale> handle(ConfirmSaleCommand c) {
    return commands.execute(
        c.tenantId(),
        () -> {
          DomainViolation.require(!c.lines().isEmpty(), "EMPTY_LINES");
          DomainViolation.require(
              c.lines().stream().map(ConfirmSaleCommand.Line::productId).distinct().count()
                  == c.lines().size(),
              "DUPLICATE_LINE");
          String fingerprint =
              c.lines().stream()
                  .sorted(Comparator.comparing(l -> l.productId().toString()))
                  .map(l -> l.productId() + ":" + l.quantity().value())
                  .collect(java.util.stream.Collectors.joining(","));
          if (c.requestKey() != null) {
            DomainViolation.require(c.requestKey().length() <= 100, "INVALID_TEXT");
            var previous = repository.findByRequestKey(c.tenantId(), c.requestKey());
            if (previous.isPresent()) {
              DomainViolation.require(
                  fingerprint.equals(repository.fingerprint(previous.get().id())),
                  "IDEMPOTENCY_CONFLICT");
              return previous.get();
            }
          }
          var lines =
              c.lines().stream()
                  .map(
                      l ->
                          inventory.take(
                              c.tenantId(),
                              c.actorId(),
                              l.productId(),
                              l.quantity().value(),
                              "Sale confirmation"))
                  .toList();
          var sale = Sale.confirm(c.tenantId(), lines);
          repository.save(sale);
          repository.remember(sale.id(), c.requestKey(), fingerprint);
          events.stage(sale);
          return sale;
        });
  }

  public Result<Sale> handle(CancelSaleCommand c) {
    UUID t = c.tenantId(), actor = c.actorId(), id = c.saleId();
    String reason = c.reason();
    return commands.execute(
        t,
        () -> {
          Text.required(reason, 500);
          var a = load(t, id);
          a.cancel();
          for (var l : a.lines()) inventory.restore(t, actor, l.productId(), l.quantity(), reason);
          repository.save(a);
          return a;
        });
  }

  private Sale load(UUID t, UUID id) {
    return repository.find(t, id).orElseThrow(() -> new DomainViolation("NOT_FOUND"));
  }

  @Transactional(readOnly = true)
  public List<Sale> list(UUID t) {
    return repository.findAll(t);
  }

  public SaleSnapshot claim(UUID t, UUID id, UUID dispatch) {
    var a = load(t, id);
    a.assignDispatch(dispatch);
    repository.save(a);
    return snapshot(a);
  }

  public void fulfillment(UUID t, UUID id, UUID dispatch, String state) {
    var a = load(t, id);
    a.updateFulfillment(dispatch, state);
    repository.save(a);
  }

  public ReturnedLineSnapshot returnUnits(UUID t, UUID id, UUID line, int quantity) {
    var a = load(t, id);
    var source = a.returnUnits(line, quantity);
    repository.save(a);
    return new ReturnedLineSnapshot(source.productId(), source.cost().cents());
  }

  @Transactional(readOnly = true)
  public List<SaleSnapshot> candidates(UUID t) {
    return repository.findAll(t).stream()
        .filter(Sale::dispatchable)
        .map(SaleService::snapshot)
        .toList();
  }

  @Transactional(readOnly = true)
  public SalesMetrics metrics(UUID t) {
    var all = repository.findAll(t);
    return new SalesMetrics(
        BigDecimal.valueOf(all.stream().mapToLong(Sale::netCents).sum(), 2),
        BigDecimal.valueOf(all.stream().mapToLong(Sale::contributionCents).sum(), 2),
        (int) all.stream().filter(a -> a.status().equals("CONFIRMED")).count());
  }

  private static SaleSnapshot snapshot(Sale a) {
    return new SaleSnapshot(
        a.id(),
        a.number(),
        a.lines().stream()
            .map(l -> new LineSnapshot(l.id(), l.productId(), l.name(), l.quantity()))
            .toList());
  }
}
