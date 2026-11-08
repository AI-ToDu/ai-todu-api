package com.aitodu.platform.inventory.infrastructure.persistence;

import com.aitodu.platform.shared.domain.Quantity;
import jakarta.persistence.*;

@Converter
public class QuantityPersistenceConverter implements AttributeConverter<Quantity, Integer> {
  public Integer convertToDatabaseColumn(Quantity x) {
    return x == null ? null : x.value();
  }

  public Quantity convertToEntityAttribute(Integer x) {
    return x == null ? null : new Quantity(x);
  }
}
