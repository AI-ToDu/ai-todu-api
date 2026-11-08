package com.aitodu.platform.notifications.application.internal;

import com.aitodu.platform.notifications.application.NotificationCommandService;
import com.aitodu.platform.notifications.application.NotificationQueryService;
import com.aitodu.platform.notifications.domain.model.aggregates.Notification;
import com.aitodu.platform.notifications.domain.model.commands.*;
import com.aitodu.platform.notifications.domain.model.queries.*;
import com.aitodu.platform.notifications.domain.repositories.NotificationRepository;
import com.aitodu.platform.shared.application.*;
import com.aitodu.platform.shared.application.result.Result;
import com.aitodu.platform.shared.domain.DomainViolation;
import com.aitodu.platform.shared.interfaces.events.IntegrationMessage;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService
    implements IntegrationSubscriber, NotificationCommandService, NotificationQueryService {
  private final NotificationRepository repository;
  private final CommandExecutor commands;

  public NotificationService(NotificationRepository r, CommandExecutor c) {
    repository = r;
    commands = c;
  }

  public String name() {
    return "notifications.inbox.v1";
  }

  public void accept(IntegrationMessage m) {
    String type =
        switch (m.type()) {
          case "inventory.stock-below-minimum" -> "LOW_STOCK";
          case "sales.sale-confirmed" -> "SALE_CONFIRMED";
          case "logistics.delivery-confirmed" -> "DELIVERY_CONFIRMED";
          default -> null;
        };
    if (type != null && m.version() != 1)
      throw new IllegalArgumentException("Unsupported integration message version");
    if (type != null && repository.find(m.tenantId(), m.eventId()).isEmpty())
      repository.save(new Notification(m.eventId(), m.tenantId(), type, m.occurredAt(), false));
  }

  @Transactional(readOnly = true)
  public List<Notification> list(UUID t) {
    return repository.findAll(t);
  }

  public Result<Notification> handle(MarkNotificationReadCommand c) {
    UUID t = c.tenantId(), id = c.notificationId();
    return commands.execute(
        t,
        () -> {
          var n = repository.find(t, id).orElseThrow(() -> new DomainViolation("NOT_FOUND"));
          n.markRead();
          repository.save(n);
          return n;
        });
  }

  @Transactional(readOnly = true)
  public List<Notification> handle(GetNotificationsQuery q) {
    return list(q.tenantId());
  }
}
