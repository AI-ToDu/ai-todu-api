package com.aitodu.platform.procurement.interfaces.rest.transform;

import static com.aitodu.platform.procurement.interfaces.rest.ProcurementController.*;

import com.aitodu.platform.procurement.domain.model.aggregates.PurchaseOrder;
import com.aitodu.platform.procurement.domain.model.commands.*;
import com.aitodu.platform.procurement.domain.model.queries.*;
import com.aitodu.platform.shared.interfaces.rest.*;
import java.util.*;
import org.springframework.web.bind.annotation.*;

public final class PurchaseResourceFromEntityAssembler {
  private PurchaseResourceFromEntityAssembler() {}

  public static PurchaseResource toResourceFromEntity(PurchaseOrder a) {
    return new PurchaseResource(
        a.id(),
        a.number(),
        a.supplierId(),
        a.status(),
        a.createdAt(),
        a.lines().stream()
            .map(l -> new PurchaseLineResource(l.id(), l.productId(), l.quantity(), l.received()))
            .toList());
  }
}
