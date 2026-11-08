package com.aitodu.platform.returns.interfaces.rest.transform;

import static com.aitodu.platform.returns.interfaces.rest.ReturnsController.*;

import com.aitodu.platform.returns.domain.model.aggregates.CustomerReturn;
import com.aitodu.platform.returns.domain.model.queries.*;
import com.aitodu.platform.shared.interfaces.rest.*;
import java.util.*;
import org.springframework.web.bind.annotation.*;

public final class ReturnResourceFromEntityAssembler {
  private ReturnResourceFromEntityAssembler() {}

  public static ReturnResource toResourceFromEntity(CustomerReturn a) {
    return new ReturnResource(
        a.id(),
        a.saleId(),
        a.saleLineId(),
        a.quantity(),
        a.disposition(),
        a.reason(),
        a.createdAt());
  }
}
