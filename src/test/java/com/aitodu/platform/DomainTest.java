package com.aitodu.platform;

import static org.junit.jupiter.api.Assertions.*;

import com.aitodu.platform.inventory.domain.model.aggregates.InventoryItem;
import com.aitodu.platform.inventory.domain.model.valueobjects.Sku;
import com.aitodu.platform.sales.domain.model.aggregates.Sale;
import com.aitodu.platform.sales.domain.model.entities.SaleLine;
import com.aitodu.platform.shared.domain.*;
import java.math.BigDecimal;
import java.util.*;
import org.junit.jupiter.api.Test;

class DomainTest {
  @Test
  void inventoryProtectsStockConservationAndEventSnapshot() {
    var a =
        InventoryItem.create(
            UUID.randomUUID(), new Sku("rice-01"), "Rice", new Money(1000), new Money(600), 2);
    a.receive(new Quantity(5));
    a.issue(new Quantity(3), true);
    assertEquals(2, a.quantity().value());
    assertEquals(1, a.domainEvents().size());
    assertThrows(UnsupportedOperationException.class, () -> a.domainEvents().clear());
    assertThrows(DomainViolation.class, () -> a.issue(new Quantity(3), true));
    assertEquals(2, a.quantity().value());
    a.changeConservation("QUARANTINED");
    assertThrows(DomainViolation.class, () -> a.issue(new Quantity(1), true));
  }

  @Test
  void moneyAndQuantityRejectPrecisionLoss() {
    assertEquals(1234, Money.of(new BigDecimal("12.34")).cents());
    assertThrows(DomainViolation.class, () -> Money.of(new BigDecimal("12.345")));
    assertThrows(DomainViolation.class, () -> Quantity.positive(new BigDecimal("1.2")));
    assertThrows(DomainViolation.class, () -> new Sku("invalid sku"));
  }

  @Test
  void salePreservesHistoricalPricesAndForbidsDoubleCancellation() {
    var l =
        new SaleLine(
            UUID.randomUUID(),
            UUID.randomUUID(),
            "Rice",
            "RI-01",
            2,
            new Money(1000),
            new Money(600),
            0);
    var sale = Sale.confirm(UUID.randomUUID(), List.of(l));
    assertEquals(2000, sale.totalCents());
    sale.cancel();
    assertThrows(DomainViolation.class, sale::cancel);
    assertThrows(DomainViolation.class, () -> sale.assignDispatch(UUID.randomUUID()));
    assertThrows(UnsupportedOperationException.class, () -> sale.lines().clear());
  }

  @Test
  void returnCannotExceedSoldQuantityOrRaceDispatch() {
    var l =
        new SaleLine(
            UUID.randomUUID(),
            UUID.randomUUID(),
            "Rice",
            "RI-01",
            2,
            new Money(1000),
            new Money(600),
            0);
    var sale = Sale.confirm(UUID.randomUUID(), List.of(l));
    sale.returnUnits(l.id(), 1);
    assertThrows(DomainViolation.class, () -> sale.returnUnits(l.id(), 2));
    assertFalse(sale.dispatchable());
  }
}
