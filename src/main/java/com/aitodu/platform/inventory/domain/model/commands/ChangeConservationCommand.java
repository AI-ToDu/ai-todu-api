package com.aitodu.platform.inventory.domain.model.commands;

import java.util.UUID;

public record ChangeConservationCommand(UUID tenantId, UUID productId, String conservation) {}
