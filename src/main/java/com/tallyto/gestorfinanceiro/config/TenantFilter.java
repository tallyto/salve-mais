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



@Order(Ordered.HIGHEST_PRECEDENCE)
@Component
@Slf4j
public class TenantFilter extends OncePerRequestFilter {

    @Autowired
    private HandlerExceptionResolver handlerExceptionResolver;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) {
       try {
           // O tenantDomain agora é definido pelo JwtAuthenticationFilter a partir do JWT
           // Este filter é mantido para compatibilidade com requisições sem JWT (públicas)
           filterChain.doFilter(request, response);
       } catch (Exception ex) {
           handlerExceptionResolver.resolveException(request, response, null, ex);
       } finally {
           TenantContext.clear();
       }
    }
}
