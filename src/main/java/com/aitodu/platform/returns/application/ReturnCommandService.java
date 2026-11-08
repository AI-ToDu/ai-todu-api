package com.aitodu.platform.returns.application;

import com.aitodu.platform.returns.domain.model.aggregates.*;
import com.aitodu.platform.returns.domain.model.commands.*;
import com.aitodu.platform.shared.application.result.Result;

public interface ReturnCommandService {
  Result<CustomerReturn> handle(RecordReturnCommand command);
}
