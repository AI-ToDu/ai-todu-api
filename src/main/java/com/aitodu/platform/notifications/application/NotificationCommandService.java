package com.aitodu.platform.notifications.application;

import com.aitodu.platform.notifications.domain.model.aggregates.*;
import com.aitodu.platform.notifications.domain.model.commands.*;
import com.aitodu.platform.shared.application.result.Result;

public interface NotificationCommandService {
  Result<Notification> handle(MarkNotificationReadCommand command);
}
