package com.aitodu.platform.notifications.application;

import com.aitodu.platform.notifications.domain.model.aggregates.*;
import com.aitodu.platform.notifications.domain.model.queries.*;
import java.util.List;

public interface NotificationQueryService {
  List<Notification> handle(GetNotificationsQuery query);
}
