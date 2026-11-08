package com.aitodu.platform.sales.domain.model.commands;

import java.util.UUID;

public record CancelSaleCommand(UUID tenantId, UUID actorId, UUID saleId, String reason) {}
