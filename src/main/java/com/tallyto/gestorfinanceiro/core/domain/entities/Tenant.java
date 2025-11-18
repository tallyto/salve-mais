package com.tallyto.gestorfinanceiro.core.domain.entities;



import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "tenants", schema = "public")
@Getter
@Setter
public class Tenant extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "domain", unique = true, nullable = false)
    private String domain;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "address")
    private String address;
    
    @Column(name = "active", nullable = false)
    private Boolean active = false;
    
    @Column(name = "confirmation_token")
    private String confirmationToken;

    // Customização de Marca
    @Column(name = "display_name")
    private String displayName;

    @Column(name = "logo_url")
    private String logoUrl;

    @Column(name = "favicon_url")
    private String faviconUrl;

    @Column(name = "primary_color", length = 7)
    private String primaryColor;

    @Column(name = "secondary_color", length = 7)
    private String secondaryColor;

    @Column(name = "accent_color", length = 7)
    private String accentColor;

    // Configurações de Plano e Recursos
    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_plan", nullable = false)
    private SubscriptionPlan subscriptionPlan = SubscriptionPlan.FREE;

    @Column(name = "max_users")
    private Integer maxUsers;

    @Column(name = "max_storage_gb")
    private BigDecimal maxStorageGb;

    @Column(name = "trial_end_date")
    private LocalDateTime trialEndDate;

    @Column(name = "subscription_start_date")
    private LocalDateTime subscriptionStartDate;

    @Column(name = "subscription_end_date")
    private LocalDateTime subscriptionEndDate;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "enabled_features", columnDefinition = "jsonb")
    private Map<String, Boolean> enabledFeatures;

    // Configurações de Notificação
    @Column(name = "custom_smtp_host")
    private String customSmtpHost;

    @Column(name = "custom_smtp_port")
    private Integer customSmtpPort;

    @Column(name = "custom_smtp_user")
    private String customSmtpUser;

    @Column(name = "custom_smtp_password")
    private String customSmtpPassword;

    @Column(name = "custom_smtp_from_email")
    private String customSmtpFromEmail;

    @Column(name = "custom_smtp_from_name")
    private String customSmtpFromName;

    @Column(name = "sms_enabled")
    private Boolean smsEnabled = false;

    @Column(name = "webhook_url")
    private String webhookUrl;

    // Configurações Regionais e Localização
    @Column(name = "timezone", length = 50)
    private String timezone = "America/Sao_Paulo";

    @Column(name = "locale", length = 10)
    private String locale = "pt_BR";

    @Column(name = "currency_code", length = 3)
    private String currencyCode = "BRL";

    @Column(name = "date_format", length = 20)
    private String dateFormat = "dd/MM/yyyy";

    // Metadados Customizados
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "custom_metadata", columnDefinition = "jsonb")
    private Map<String, Object> customMetadata;

    // Enum para tipos de plano
    public enum SubscriptionPlan {
        FREE,
        BASIC,
        PREMIUM,
        ENTERPRISE
    }

}
