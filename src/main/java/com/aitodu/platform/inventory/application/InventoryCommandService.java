package com.aitodu.platform.inventory.application;

import com.aitodu.platform.inventory.domain.model.aggregates.InventoryItem;
import com.aitodu.platform.inventory.domain.model.commands.*;
import com.aitodu.platform.shared.application.result.Result;
import java.util.UUID;

public interface InventoryCommandService {
  Result<InventoryItem> handle(CreateProductCommand command);

  Result<InventoryItem> handle(RecordMovementCommand command);

  Result<InventoryItem> handle(ChangeConservationCommand command);
}
