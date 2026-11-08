package com.aitodu.platform.procurement.application;

import com.aitodu.platform.procurement.domain.model.aggregates.*;
import com.aitodu.platform.procurement.domain.model.queries.*;
import java.util.List;

public interface ProcurementQueryService {
  List<Supplier> handle(GetSuppliersQuery query);

  List<PurchaseOrder> handle(GetPurchasesQuery query);
}
