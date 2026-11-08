package com.aitodu.platform.logistics.domain.model.events;

import com.aitodu.platform.shared.domain.DomainEvent;
import java.time.Instant;
import java.util.UUID;

public record DeliveryConfirmed(UUID eventId, UUID tenantId, UUID dispatchId, Instant occurredAt)
    implements DomainEvent {}
