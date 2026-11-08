package com.aitodu.platform.returns.domain.model.commands;

import java.util.UUID;

public record RecordReturnCommand(
    UUID tenantId,
    UUID actorId,
    UUID saleId,
    UUID saleLineId,
    int quantity,
    String disposition,
    String reason) {}
