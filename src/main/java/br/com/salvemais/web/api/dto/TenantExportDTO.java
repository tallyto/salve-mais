package br.com.salvemais.web.api.dto;

import br.com.salvemais.domain.entities.Tenant;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Schema(description = "Exportação completa dos dados de um tenant")
public record TenantExportDTO(
        TenantInfoDTO tenant,
        List<UsuarioExportDTO> usuarios,
        long totalUsuarios,
        long usuariosAtivos,
        LocalDateTime dataExportacao
) {
    @Schema(description = "Informações públicas do tenant")
    public record TenantInfoDTO(
            UUID id,
            String name,
            String domain,
            String email,
            String phoneNumber,
            Boolean active,
            Tenant.SubscriptionPlan subscriptionPlan,
            LocalDateTime createdAt
    ) {}

    @Schema(description = "Usuário exportado sem dados sensíveis")
    public record UsuarioExportDTO(
            Long id,
            String nome,
            String email,
            Boolean ativo,
            LocalDateTime criadoEm,
            LocalDateTime ultimoAcesso
    ) {}
}
