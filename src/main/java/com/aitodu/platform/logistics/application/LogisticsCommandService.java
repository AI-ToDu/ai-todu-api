package com.aitodu.platform.logistics.application;

import com.aitodu.platform.logistics.domain.model.aggregates.*;
import com.aitodu.platform.logistics.domain.model.commands.*;
import com.aitodu.platform.shared.application.result.Result;

public interface LogisticsCommandService {
  Result<DispatchManifest> handle(PrepareDispatchCommand command);

  Result<DispatchManifest> handle(TransitionDispatchCommand command);

  Result<DispatchManifest> handle(ReportDeliveryIncidentCommand command);

  Result<Driver> handle(RegisterDriverCommand command);

  Result<Vehicle> handle(RegisterVehicleCommand command);
}
