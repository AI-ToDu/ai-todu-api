package com.aitodu.platform.inventory.domain.model.commands;

import com.aitodu.platform.inventory.domain.model.valueobjects.Sku;
import com.aitodu.platform.shared.domain.*;
import java.util.UUID;

public record CreateProductCommand(
    UUID tenantId, Sku sku, String name, Money price, Money cost, int minimum) {}
