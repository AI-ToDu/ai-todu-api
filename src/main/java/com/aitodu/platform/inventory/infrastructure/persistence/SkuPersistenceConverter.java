package com.aitodu.platform.inventory.infrastructure.persistence;

import com.aitodu.platform.inventory.domain.model.valueobjects.Sku;
import jakarta.persistence.*;

@Converter
public class SkuPersistenceConverter implements AttributeConverter<Sku, String> {
  public String convertToDatabaseColumn(Sku x) {
    return x == null ? null : x.value();
  }

  public Sku convertToEntityAttribute(String x) {
    return x == null ? null : new Sku(x);
  }
}
