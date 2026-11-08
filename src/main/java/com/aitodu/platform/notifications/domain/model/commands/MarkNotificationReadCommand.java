package com.aitodu.platform.notifications.domain.model.commands;

import java.util.UUID;

public record MarkNotificationReadCommand(UUID tenantId, UUID notificationId) {}
