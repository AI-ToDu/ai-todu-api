package com.aitodu.platform.procurement.application;

import com.aitodu.platform.procurement.domain.model.aggregates.*;
import com.aitodu.platform.procurement.domain.model.commands.*;
import com.aitodu.platform.shared.application.result.Result;

public interface ProcurementCommandService {
  Result<Supplier> handle(RegisterSupplierCommand command);

  Result<PurchaseOrder> handle(PlacePurchaseCommand command);

  Result<PurchaseOrder> handle(ReceivePurchaseCommand command);
}
