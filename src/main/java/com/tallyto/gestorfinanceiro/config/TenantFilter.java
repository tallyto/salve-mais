package com.tallyto.gestorfinanceiro.config;


import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import com.tallyto.gestorfinanceiro.context.TenantContext;
import com.tallyto.gestorfinanceiro.core.database.FlywayMigrationService;
import com.tallyto.gestorfinanceiro.core.infra.repositories.TenantRepository;

import static com.tallyto.gestorfinanceiro.context.TenantContext.PRIVATE_TENANT_HEADER;



@Order(Ordered.HIGHEST_PRECEDENCE)
@Component
@Slf4j
public class TenantFilter extends OncePerRequestFilter {

    @Autowired
    private FlywayMigrationService flywayMigrationService;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private HandlerExceptionResolver handlerExceptionResolver;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) {
       try {
           String privateTenant = request.getHeader(PRIVATE_TENANT_HEADER);

           if (privateTenant != null && !privateTenant.isEmpty()) {
               var tenant = tenantRepository.getTenantByDomain(privateTenant);
               if (tenant != null) {
                   TenantContext.setCurrentTenant(privateTenant);
                   flywayMigrationService.migrateTenantSchema(privateTenant);
               } else {
                   log.error("Tenant not found for domain: {}", privateTenant);
               }
           }

           filterChain.doFilter(request, response);
       } catch (Exception ex) {
           handlerExceptionResolver.resolveException(request, response, null, ex);
       } finally {
           TenantContext.clear();
       }
    }
}
