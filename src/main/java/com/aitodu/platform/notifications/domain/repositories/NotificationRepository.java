package com.aitodu.platform.notifications.domain.repositories;

import com.aitodu.platform.notifications.domain.model.aggregates.Notification;
import java.util.*;

public interface NotificationRepository {
  Optional<Notification> find(UUID tenant, UUID id);

  List<Notification> findAll(UUID tenant);

  void save(Notification notification);
}
