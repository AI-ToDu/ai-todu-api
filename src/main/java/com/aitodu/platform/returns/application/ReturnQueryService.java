package com.aitodu.platform.returns.application;

import com.aitodu.platform.returns.domain.model.aggregates.*;
import com.aitodu.platform.returns.domain.model.queries.*;
import java.util.List;

public interface ReturnQueryService {
  List<CustomerReturn> handle(GetReturnsQuery query);
}
