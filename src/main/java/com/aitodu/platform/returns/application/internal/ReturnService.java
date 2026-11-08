package com.aitodu.platform.returns.application.internal;

import com.aitodu.platform.inventory.interfaces.acl.InventoryContextFacade;
import com.aitodu.platform.returns.application.ReturnCommandService;
import com.aitodu.platform.returns.application.ReturnQueryService;
import com.aitodu.platform.returns.domain.model.aggregates.CustomerReturn;
import com.aitodu.platform.returns.domain.model.commands.RecordReturnCommand;
import com.aitodu.platform.returns.domain.model.queries.*;
import com.aitodu.platform.returns.domain.repositories.ReturnRepository;
import com.aitodu.platform.returns.interfaces.acl.ReturnsContextFacade;
import com.aitodu.platform.sales.interfaces.acl.SalesContextFacade;
import com.aitodu.platform.shared.application.*;
import com.aitodu.platform.shared.application.result.Result;
import com.aitodu.platform.shared.domain.*;
import java.time.Instant;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReturnService
    implements ReturnsContextFacade, ReturnCommandService, ReturnQueryService {
  private final ReturnRepository repository;
  private final SalesContextFacade sales;
  private final InventoryContextFacade inventory;
  private final CommandExecutor commands;

  public ReturnService(
      ReturnRepository r, SalesContextFacade s, InventoryContextFacade i, CommandExecutor c) {
    repository = r;
    sales = s;
    inventory = i;
    commands = c;
  }

  public Result<CustomerReturn> handle(RecordReturnCommand c) {
    UUID t = c.tenantId(), actor = c.actorId(), sale = c.saleId(), line = c.saleLineId();
    int quantity = c.quantity();
    String disposition = c.disposition(), reason = c.reason();
    return commands.execute(
        t,
        () -> {
          var source = sales.returnUnits(t, sale, line, quantity);
          var a =
              new CustomerReturn(
                  UUID.randomUUID(),
                  t,
                  sale,
                  line,
                  source.productId(),
                  quantity,
                  disposition,
                  reason,
                  Instant.now(),
                  disposition != null && disposition.equals("LOSS")
                      ? Math.multiplyExact(source.unitCostCents(), quantity)
                      : 0);
          if (disposition.equals("RESTOCK"))
            inventory.receive(t, actor, source.productId(), quantity, reason);
          repository.save(a);
          return a;
        });
  }

  @Transactional(readOnly = true)
  public List<CustomerReturn> list(UUID t) {
    return repository.findAll(t);
  }

  @Transactional(readOnly = true)
  public long recordedLossCents(UUID t) {
    return repository.findAll(t).stream().mapToLong(CustomerReturn::lossCents).sum();
  }

  @Transactional(readOnly = true)
  public List<CustomerReturn> handle(GetReturnsQuery q) {
    return list(q.tenantId());
  }
}
