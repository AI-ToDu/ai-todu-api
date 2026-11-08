package com.aitodu.platform.sales.application;

import com.aitodu.platform.sales.domain.model.aggregates.*;
import com.aitodu.platform.sales.domain.model.queries.*;
import java.util.List;

public interface SaleQueryService {
  List<Sale> handle(GetSalesQuery query);
}
