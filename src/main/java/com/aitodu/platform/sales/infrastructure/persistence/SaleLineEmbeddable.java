package com.aitodu.platform.sales.infrastructure.persistence;

import jakarta.persistence.*;
import java.util.UUID;

@Embeddable
public class SaleLineEmbeddable {
  public UUID id, productId;
  public String name, sku;
  public int quantity, returned;
  public long priceCents, costCents;
}
