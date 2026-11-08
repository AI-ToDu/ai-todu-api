package com.aitodu.platform.inventory.application;

import com.aitodu.platform.inventory.domain.model.aggregates.*;
import com.aitodu.platform.inventory.domain.model.entities.StockMovement;
import com.aitodu.platform.inventory.domain.model.queries.*;
import java.util.List;

public interface InventoryQueryService {
  List<InventoryItem> handle(GetInventoryQuery query);

  List<StockMovement> handle(GetMovementsQuery query);
}
