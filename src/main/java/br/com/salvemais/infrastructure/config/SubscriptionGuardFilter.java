package br.com.salvemais.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import br.com.salvemais.infrastructure.context.TenantContext;
import br.com.salvemais.domain.entities.Tenant;
import br.com.salvemais.domain.enums.SubscriptionStatus;
import br.com.salvemais.infrastructure.repositories.TenantRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Component
public class SubscriptionGuardFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionGuardFilter.class);

    private static final Set<String> PUBLIC_PREFIXES = Set.of(
            "/api/auth/",
            "/api/tenants/cadastro",
            "/api/tenants/verificar",
            "/api/tenants/confirmar",
            "/api/tenants/verificar-dominio",
            "/swagger-ui",
            "/v3/api-docs"
    );

    private static final Set<String> PUBLIC_EXACT = Set.of("/", "/health", "/actuator/health", "/api/usuarios");

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        if (HttpMethod.OPTIONS.matches(request.getMethod()) || isPublicPath(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        String tenantDomain = TenantContext.getCurrentTenant();
        if (tenantDomain == null || tenantDomain.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        Optional<Tenant> tenantOpt = tenantRepository.findByDomain(tenantDomain);
        if (tenantOpt.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        Tenant tenant = tenantOpt.get();
        SubscriptionStatus status = tenant.getSubscriptionStatus();

        if (status == SubscriptionStatus.INADIMPLENTE || status == SubscriptionStatus.CANCELADO) {
            log.warn("Acesso bloqueado para tenant '{}' com status {}", tenantDomain, status);
            bloquearAcesso(response, status);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isPublicPath(String uri) {
        if (PUBLIC_EXACT.contains(uri)) return true;
        return PUBLIC_PREFIXES.stream().anyMatch(uri::startsWith);
    }

    private void bloquearAcesso(HttpServletResponse response, SubscriptionStatus status) throws IOException {
        String mensagem = status == SubscriptionStatus.INADIMPLENTE
                ? "Sua assinatura está com pagamento pendente. Acesse o painel de cobrança para regularizar."
                : "Sua assinatura foi cancelada. Entre em contato com o suporte.";

        response.setStatus(HttpStatus.PAYMENT_REQUIRED.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> body = Map.of(
                "timestamp", OffsetDateTime.now().toString(),
                "status", HttpStatus.PAYMENT_REQUIRED.value(),
                "title", "Assinatura bloqueada",
                "message", mensagem,
                "subscriptionStatus", status.name()
        );

        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
