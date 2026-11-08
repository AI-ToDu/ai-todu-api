package com.aitodu.platform.sales.application;

import com.aitodu.platform.sales.domain.model.aggregates.Sale;
import com.aitodu.platform.sales.domain.model.commands.*;
import com.aitodu.platform.shared.application.result.Result;

public interface SaleCommandService {
  Result<Sale> handle(ConfirmSaleCommand command);

  Result<Sale> handle(CancelSaleCommand command);
}
