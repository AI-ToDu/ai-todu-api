package com.aitodu.platform.notifications.interfaces.rest.transform;

import static com.aitodu.platform.notifications.interfaces.rest.NotificationsController.*;

import com.aitodu.platform.notifications.domain.model.aggregates.Notification;
import com.aitodu.platform.notifications.domain.model.queries.*;
import com.aitodu.platform.shared.interfaces.rest.*;
import org.springframework.web.bind.annotation.*;

public final class NotificationResourceFromEntityAssembler {
  private NotificationResourceFromEntityAssembler() {}

  public static NotificationResource toResourceFromEntity(Notification n) {
    return new NotificationResource(n.id(), n.type(), n.createdAt(), n.read());
  }
}
