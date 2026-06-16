package br.com.salvemais.infrastructure.config;


import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.stereotype.Component;

import br.com.salvemais.infrastructure.context.TenantContext;

import java.util.Objects;

@Component
public class CurrentTenantIdentifierResolverImpl implements CurrentTenantIdentifierResolver<String> {
    @Override
    public String resolveCurrentTenantIdentifier() {
        String tenant = TenantContext.getCurrentTenant();
        return Objects.requireNonNullElse(tenant, TenantContext.DEFAULT_TENANT);
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }
}
