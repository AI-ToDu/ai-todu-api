package com.aitodu.platform.logistics.domain.model.commands;

import java.util.UUID;

public record ReportDeliveryIncidentCommand(UUID tenantId, UUID dispatchId, String description) {}
