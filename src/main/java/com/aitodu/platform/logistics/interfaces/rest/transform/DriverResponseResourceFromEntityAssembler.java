package com.aitodu.platform.logistics.interfaces.rest.transform;

import static com.aitodu.platform.logistics.interfaces.rest.LogisticsResources.*;

import com.aitodu.platform.logistics.domain.model.aggregates.*;
import java.util.*;

public final class DriverResponseResourceFromEntityAssembler {
  private DriverResponseResourceFromEntityAssembler() {}

  public static DriverResponseResource toResourceFromEntity(Driver a) {
    return new DriverResponseResource(a.id(), a.name(), a.license());
  }
}
