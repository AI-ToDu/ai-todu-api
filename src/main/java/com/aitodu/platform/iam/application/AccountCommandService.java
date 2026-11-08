package com.aitodu.platform.iam.application;

import com.aitodu.platform.iam.domain.model.aggregates.UserAccount;
import com.aitodu.platform.iam.domain.model.commands.RegisterWorkspaceCommand;
import com.aitodu.platform.shared.application.result.Result;

public interface AccountCommandService {
  Result<UserAccount> handle(RegisterWorkspaceCommand command);
}
