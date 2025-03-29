package com.hmtmcse.v3base;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "xyz",
                version = "0.0.1",
                contact = @Contact(
                        name = "xyz", email = "xyz", url = "xyz"
                ),
                license = @License(
                        name = "", url = ""
                ),
                termsOfService = "",
                description = ""
        ),
        servers = @Server(
                url = "http://localhost:8080",
                description = "Localhost"
        )
)
public class OpenAPISecurityConfiguration {
}