package com.aitodu.platform.iam.infrastructure.security;

import java.util.UUID;

public record WorkspacePrincipal(UUID id, UUID tenantId, String name, String email, String role) {}
