package com.aitodu.platform.shared.domain;

import java.util.ArrayList;
import java.util.List;

public abstract class AggregateRoot {
  private final List<DomainEvent> events = new ArrayList<>();

  protected final void record(DomainEvent event) {
    events.add(event);
  }

  public final List<DomainEvent> domainEvents() {
    return List.copyOf(events);
  }

  public final void clearDomainEvents() {
    events.clear();
  }
}
