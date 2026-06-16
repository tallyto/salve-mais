package br.com.salvemais.web.api.controllers;

import br.com.salvemais.infrastructure.config.openapi.OpenApiPublic;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Infra", description = "Endpoints básicos de saúde da aplicação")
@OpenApiPublic
@RestController
public class HealthController {

    @GetMapping("/")
    @Operation(summary = "Verificar se a API está online")
    public String home() {
        return "API ONLINE";
    }

    @GetMapping("/health")
    @Operation(summary = "Healthcheck simples")
    public String health() {
        return "OK";
    }
}
