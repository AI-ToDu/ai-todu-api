package com.aitodu.platform.logistics.domain.model.commands;

import java.util.UUID;

public record RegisterVehicleCommand(UUID tenantId, String name, String plate) {}
