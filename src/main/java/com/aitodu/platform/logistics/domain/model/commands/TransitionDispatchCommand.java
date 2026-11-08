package com.aitodu.platform.logistics.domain.model.commands;

import java.util.UUID;

public record TransitionDispatchCommand(
    UUID tenantId, UUID dispatchId, String status, String recipient) {}
