package com.aitodu.platform.shared.infrastructure.documentation;

import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.*;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info =
        @Info(
            title = "AI-ToDu Platform API",
            version = "v1",
            description =
                "Academic workspace API. Inventory, purchasing, sales, returns and dispatches."
                    + " Amounts are PEN; quantities are whole units. Business commands are atomic"
                    + " per workspace."),
    security = @SecurityRequirement(name = "bearerAuth"))
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT")
public class OpenApiConfiguration {}
