package com.aitodu.platform.iam.application;

import com.aitodu.platform.iam.domain.model.aggregates.*;
import com.aitodu.platform.iam.domain.model.queries.*;
import java.util.List;

public interface EmployeeQueryService {
  List<UserAccount> handle(GetEmployeesQuery query);
}
