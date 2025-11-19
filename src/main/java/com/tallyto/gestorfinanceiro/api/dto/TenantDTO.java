package com.tallyto.gestorfinanceiro.api.dto;

import com.tallyto.gestorfinanceiro.core.domain.entities.Tenant.SubscriptionPlan;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Schema(description = "DTO para representar um Tenant")
@Getter
@Setter
public class TenantDTO {
        @NotBlank
        @Schema(description = "Domínio exclusivo do tenant", example = "empresa.com")
        private String domain;

        @NotBlank
        @Schema(description = "Nome do tenant", example = "Empresa XYZ")
        private String name;

        @NotBlank
        @Email
        @Schema(description = "E-mail de contato do tenant", example = "contato@empresa.com")
        private String email;

        @Size(max = 15)
        @Schema(description = "Número de telefone do tenant", example = "+5511999999999")
        private String phoneNumber;

        @Schema(description = "Endereço do tenant", example = "Rua Exemplo, 123, São Paulo - SP")
        private String address;

        // Customização de Marca
        @Schema(description = "Nome de exibição customizado", example = "Minha Empresa")
        private String displayName;

        @Schema(description = "URL do logotipo")
        private String logoUrl;

        @Schema(description = "URL do favicon")
        private String faviconUrl;

        // Configurações de Plano
        @Schema(description = "Plano de assinatura", example = "PREMIUM")
        private SubscriptionPlan subscriptionPlan;

        @Schema(description = "Máximo de usuários permitidos", example = "50")
        private Integer maxUsers;

        @Schema(description = "Máximo de armazenamento em GB", example = "100.00")
        private BigDecimal maxStorageGb;

        @Schema(description = "Data de fim do período trial")
        private LocalDateTime trialEndDate;

        @Schema(description = "Features habilitadas")
        private Map<String, Boolean> enabledFeatures;

        // Configurações de Notificação
        @Schema(description = "Host SMTP customizado")
        private String customSmtpHost;

        @Schema(description = "Porta SMTP customizada")
        private Integer customSmtpPort;

        @Schema(description = "Usuário SMTP customizado")
        private String customSmtpUser;

        @Schema(description = "Email de origem customizado")
        private String customSmtpFromEmail;

        @Schema(description = "Nome de origem customizado")
        private String customSmtpFromName;

        @Schema(description = "SMS habilitado")
        private Boolean smsEnabled;

        @Schema(description = "URL do webhook")
        private String webhookUrl;

        // Configurações Regionais
        @Schema(description = "Timezone", example = "America/Sao_Paulo")
        private String timezone;

        @Schema(description = "Locale", example = "pt_BR")
        private String locale;

        @Schema(description = "Código da moeda", example = "BRL")
        private String currencyCode;

        @Schema(description = "Formato de data", example = "dd/MM/yyyy")
        private String dateFormat;

        @Schema(description = "Metadados customizados")
        private Map<String, Object> customMetadata;

}
