package com.aitodu.platform.inventory.infrastructure.persistence;

import com.aitodu.platform.shared.domain.Money;
import jakarta.persistence.*;

@Converter
public class MoneyPersistenceConverter implements AttributeConverter<Money, Long> {
  public Long convertToDatabaseColumn(Money x) {
    return x == null ? null : x.cents();
  }

  public Money convertToEntityAttribute(Long x) {
    return x == null ? null : new Money(x);
  }
}
