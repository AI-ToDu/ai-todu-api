package com.aitodu.platform.inventory.domain.model.aggregates;

import com.aitodu.platform.inventory.domain.model.events.StockBelowMinimum;
import com.aitodu.platform.inventory.domain.model.valueobjects.Sku;
import com.aitodu.platform.shared.domain.*;
import java.time.Instant;
import java.util.*;

public final class InventoryItem extends AggregateRoot {
  private final UUID id, tenantId;
  private final Sku sku;
  private final String name;
  private Quantity quantity;
  private final Money price, cost;
  private final int minimum;
  private String conservation;

  private InventoryItem(
      UUID id,
      UUID tenant,
      Sku sku,
      String name,
      Quantity q,
      Money price,
      Money cost,
      int minimum,
      String conservation) {
    this.id = Objects.requireNonNull(id);
    tenantId = Objects.requireNonNull(tenant);
    this.sku = Objects.requireNonNull(sku);
    this.name = Text.required(name, 160);
    quantity = Objects.requireNonNull(q);
    this.price = Objects.requireNonNull(price);
    this.cost = Objects.requireNonNull(cost);
    DomainViolation.require(minimum >= 0, "INVALID_QUANTITY");
    this.minimum = minimum;
    changeConservation(conservation);
  }

  public static InventoryItem create(
      UUID tenant, Sku sku, String name, Money price, Money cost, int minimum) {
    return new InventoryItem(
        UUID.randomUUID(), tenant, sku, name, new Quantity(0), price, cost, minimum, "GOOD");
  }

  public static InventoryItem reconstitute(
      UUID id,
      UUID tenant,
      Sku sku,
      String name,
      Quantity quantity,
      Money price,
      Money cost,
      int minimum,
      String conservation) {
    return new InventoryItem(id, tenant, sku, name, quantity, price, cost, minimum, conservation);
  }

  public void receive(Quantity amount) {
    DomainViolation.require(amount.value() > 0, "INVALID_QUANTITY");
    quantity = new Quantity(Math.addExact(quantity.value(), amount.value()));
  }

  public void issue(Quantity amount, boolean sale) {
    DomainViolation.require(amount.value() > 0, "INVALID_QUANTITY");
    if (sale) DomainViolation.require(conservation.equals("GOOD"), "PRODUCT_BLOCKED");
    DomainViolation.require(quantity.value() >= amount.value(), "INSUFFICIENT_STOCK");
    int previous = quantity.value();
    quantity = new Quantity(previous - amount.value());
    if (previous > minimum && quantity.value() <= minimum)
      record(
          new StockBelowMinimum(
              UUID.randomUUID(), tenantId, id, quantity.value(), minimum, Instant.now()));
  }

  public void changeConservation(String state) {
    DomainViolation.require(
        Set.of("GOOD", "DAMAGED", "QUARANTINED").contains(state == null ? "" : state),
        "INVALID_STATUS");
    conservation = state;
  }

  public UUID id() {
    return id;
  }

  public UUID tenantId() {
    return tenantId;
  }

  public Sku sku() {
    return sku;
  }

  public String name() {
    return name;
  }

  public Quantity quantity() {
    return quantity;
  }

  public Money price() {
    return price;
  }

  public Money cost() {
    return cost;
  }

  public int minimum() {
    return minimum;
  }

  public String conservation() {
    return conservation;
  }
}
