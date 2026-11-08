package com.aitodu.platform.shared.interfaces.rest;

import java.util.List;

public record CollectionResource<T>(List<T> items) {
  public CollectionResource {
    items = List.copyOf(items);
  }
}
