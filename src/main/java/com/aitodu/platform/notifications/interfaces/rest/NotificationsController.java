package com.aitodu.platform.notifications.interfaces.rest;

import com.aitodu.platform.iam.infrastructure.security.WorkspacePrincipal;
import com.aitodu.platform.notifications.application.internal.NotificationService;
import com.aitodu.platform.notifications.domain.model.aggregates.Notification;
import com.aitodu.platform.notifications.domain.model.commands.MarkNotificationReadCommand;
import com.aitodu.platform.shared.interfaces.rest.*;
import java.time.Instant;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationsController {
  public record NotificationResource(UUID id, String type, Instant createdAt, boolean read) {
    static NotificationResource from(Notification n) {
      return new NotificationResource(n.id(), n.type(), n.createdAt(), n.read());
    }
  }

  private final NotificationService service;

  public NotificationsController(NotificationService s) {
    service = s;
  }

  @GetMapping
  public CollectionResource<NotificationResource> list(
      @AuthenticationPrincipal WorkspacePrincipal p) {
    return new CollectionResource<>(
        service.list(p.tenantId()).stream().map(NotificationResource::from).toList());
  }

  @PatchMapping("/{id}")
  public ResponseEntity<NotificationResource> read(
      @AuthenticationPrincipal WorkspacePrincipal p, @PathVariable UUID id) {
    return ResponseEntityAssembler.from(
        service.handle(new MarkNotificationReadCommand(p.tenantId(), id)),
        NotificationResource::from,
        200);
  }
}
