package com.aitodu.platform.procurement.domain.model.commands;

import java.util.UUID;

public record RegisterSupplierCommand(UUID tenantId, String name, String contact) {}
