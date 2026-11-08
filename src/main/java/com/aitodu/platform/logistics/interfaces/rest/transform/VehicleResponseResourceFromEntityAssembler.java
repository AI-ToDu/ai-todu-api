package com.aitodu.platform.logistics.interfaces.rest.transform;

import static com.aitodu.platform.logistics.interfaces.rest.LogisticsResources.*;

import com.aitodu.platform.logistics.domain.model.aggregates.*;
import java.util.*;

public final class VehicleResponseResourceFromEntityAssembler {
  private VehicleResponseResourceFromEntityAssembler() {}

  public static VehicleResponseResource toResourceFromEntity(Vehicle a) {
    return new VehicleResponseResource(a.id(), a.name(), a.plate());
  }
}
