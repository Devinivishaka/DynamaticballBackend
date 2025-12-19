package com.protonestiot.dynamaticball.Config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Dynamatic Ball API",
                version = "v1",
                description = "REST API for match management, users, players, game setups, and recordings",
                contact = @Contact(name = "Protones Tiot", email = "support@example.com"),
                license = @License(name = "Proprietary")
        ),
        servers = {
                @Server(url = "/", description = "Default Server")
        },
        security = {
                @SecurityRequirement(name = "bearer-jwt")
        }
)
@SecurityScheme(
        name = "bearer-jwt",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
public class SwaggerConfig {

}

