package com.aitodu.platform.procurement.infrastructure.persistence;

import jakarta.persistence.*;
import java.util.UUID;

@Embeddable
public class PurchaseLineEmbeddable {
  public UUID id, productId;
  public int quantity, received;
}
