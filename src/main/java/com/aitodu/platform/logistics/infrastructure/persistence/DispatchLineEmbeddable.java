package com.aitodu.platform.logistics.infrastructure.persistence;

import jakarta.persistence.*;
import java.util.UUID;

@Embeddable
public class DispatchLineEmbeddable {
  public UUID id, saleId, saleLineId, productId;
  public String name;
  public int quantity;
}
