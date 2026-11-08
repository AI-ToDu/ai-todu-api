package com.aitodu.platform.inventory.domain.model.events;

import com.aitodu.platform.shared.domain.DomainEvent;
import java.time.Instant;
import java.util.UUID;

public record StockBelowMinimum(
    UUID eventId, UUID tenantId, UUID productId, int quantity, int minimum, Instant occurredAt)
    implements DomainEvent {}
