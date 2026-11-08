package com.aitodu.platform.shared.application;

import com.aitodu.platform.shared.interfaces.events.IntegrationMessage;

public interface IntegrationSubscriber {
  String name();

  void accept(IntegrationMessage message);
}
