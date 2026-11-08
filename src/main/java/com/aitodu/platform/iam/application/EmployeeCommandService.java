package com.aitodu.platform.iam.application;

import com.aitodu.platform.iam.domain.model.aggregates.*;
import com.aitodu.platform.iam.domain.model.commands.*;
import com.aitodu.platform.shared.application.result.Result;

public interface EmployeeCommandService {
  Result<UserAccount> handle(CreateEmployeeCommand command);
}
