package com.aitodu.platform.notifications.interfaces.rest;

import com.aitodu.platform.iam.infrastructure.security.WorkspacePrincipal;
import com.aitodu.platform.notifications.application.NotificationCommandService;
import com.aitodu.platform.notifications.application.NotificationQueryService;
import com.aitodu.platform.notifications.domain.model.commands.MarkNotificationReadCommand;
import com.aitodu.platform.notifications.domain.model.queries.*;
import com.aitodu.platform.notifications.interfaces.rest.transform.NotificationResourceFromEntityAssembler;
import com.aitodu.platform.shared.interfaces.rest.*;
import java.time.Instant;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationsController {
  public record NotificationResource(
      UUID id, String type, Instant createdAt, boolean read, UUID productId) {}

  private final NotificationCommandService commands;
  private final NotificationQueryService queries;

  public NotificationsController(
      NotificationCommandService commands, NotificationQueryService queries) {
    this.commands = commands;
    this.queries = queries;
  }

  @GetMapping
  public CollectionResource<NotificationResource> list(
      @AuthenticationPrincipal WorkspacePrincipal p) {
    return new CollectionResource<>(
        queries.handle(new GetNotificationsQuery(p.tenantId())).stream()
            .map(NotificationResourceFromEntityAssembler::toResourceFromEntity)
            .toList());
  }

  @PatchMapping("/{id}")
  public ResponseEntity<?> read(
      @AuthenticationPrincipal WorkspacePrincipal p, @PathVariable UUID id) {
    return ResponseEntityAssembler.from(
        commands.handle(new MarkNotificationReadCommand(p.tenantId(), id)),
        NotificationResourceFromEntityAssembler::toResourceFromEntity,
        200);
  }
}
