package com.aitodu.platform.logistics.interfaces.rest.transform;

import static com.aitodu.platform.logistics.interfaces.rest.LogisticsResources.*;

import com.aitodu.platform.logistics.domain.model.aggregates.*;
import java.util.*;

public final class CandidateResourceFromEntityAssembler {
  private CandidateResourceFromEntityAssembler() {}

  public static CandidateResource toResourceFromEntity(
      com.aitodu.platform.sales.interfaces.acl.SalesContextFacade.SaleSnapshot s) {
    return new CandidateResource(s.id(), s.number());
  }
}
