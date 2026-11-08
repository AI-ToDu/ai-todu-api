package com.aitodu.platform.logistics.domain.model.commands;

import java.util.UUID;

public record RegisterDriverCommand(UUID tenantId, String name, String license) {}
