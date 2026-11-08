package com.aitodu.platform.iam.interfaces.rest.transform;

import static com.aitodu.platform.iam.interfaces.rest.EmployeesController.*;

import com.aitodu.platform.iam.domain.model.queries.*;
import com.aitodu.platform.shared.interfaces.rest.*;
import org.springframework.web.bind.annotation.*;

public final class EmployeeResourceFromEntityAssembler {
  private EmployeeResourceFromEntityAssembler() {}

  public static EmployeeResource toResourceFromEntity(
      com.aitodu.platform.iam.domain.model.aggregates.UserAccount u) {
    return new EmployeeResource(u.id(), u.name(), u.email().value(), u.role(), u.active());
  }
}
