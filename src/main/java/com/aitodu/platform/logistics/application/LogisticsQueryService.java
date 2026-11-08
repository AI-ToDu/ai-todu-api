package com.aitodu.platform.logistics.application;

import com.aitodu.platform.logistics.domain.model.aggregates.*;
import com.aitodu.platform.logistics.domain.model.queries.*;
import java.util.List;

public interface LogisticsQueryService {
  List<DispatchManifest> handle(GetDispatchesQuery query);

  List<Driver> handle(GetDriversQuery query);

  List<Vehicle> handle(GetVehiclesQuery query);

  List<com.aitodu.platform.sales.interfaces.acl.SalesContextFacade.SaleSnapshot> handle(
      GetDispatchCandidatesQuery query);
}
