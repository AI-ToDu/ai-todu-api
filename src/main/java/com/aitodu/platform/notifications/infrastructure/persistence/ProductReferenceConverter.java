package com.aitodu.platform.notifications.infrastructure.persistence;

import com.aitodu.platform.notifications.domain.model.valueobjects.ProductReference;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.UUID;

@Converter
public class ProductReferenceConverter implements AttributeConverter<ProductReference, UUID> {
  public UUID convertToDatabaseColumn(ProductReference value) {
    return value == null ? null : value.value();
  }

  public ProductReference convertToEntityAttribute(UUID value) {
    return value == null ? null : new ProductReference(value);
  }
}
