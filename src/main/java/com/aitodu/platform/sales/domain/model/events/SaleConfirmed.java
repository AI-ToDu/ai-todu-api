package com.aitodu.platform.sales.domain.model.events;

import com.aitodu.platform.shared.domain.DomainEvent;
import java.time.Instant;
import java.util.UUID;

public record SaleConfirmed(
    UUID eventId, UUID tenantId, UUID saleId, long totalCents, Instant occurredAt)
    implements DomainEvent {}
