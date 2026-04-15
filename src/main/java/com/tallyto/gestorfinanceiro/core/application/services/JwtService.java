package com.tallyto.gestorfinanceiro.core.application.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class JwtService {
    private static final String SECRET_KEY = "minha-chave-secreta-para-jwt-gestor-financeiro-2025";
    private static final long EXPIRATION = 86400000; // 1 dia em ms

    private final Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

    /**
     * Gera um token JWT com as informações do usuário e tenant
     * 
     * @param email email do usuário
     * @param tenantId ID do tenant
     * @param tenantDomain domain do tenant
     * @return token JWT
     */
    public String gerarToken(String email, UUID tenantId, String tenantDomain) {
        Map<String, Object> claims = new HashMap<>();
        if (tenantId != null) {
            claims.put("tenantId", tenantId.toString());
        }
        if (tenantDomain != null) {
            claims.put("tenantDomain", tenantDomain);
        }
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Método sobrescrito para compatibilidade com código antigo
     * Gera token sem incluir tenantDomain
     */
    public String gerarToken(String email, UUID tenantId) {
        return gerarToken(email, tenantId, null);
    }

    public String getEmailFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public UUID getTenantIdFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        
        String tenantIdStr = claims.get("tenantId", String.class);
        return tenantIdStr != null ? UUID.fromString(tenantIdStr) : null;
    }

    /**
     * Extrai o domain do tenant a partir do token JWT
     * 
     * @param token token JWT
     * @return domain do tenant ou null se não incluído no token
     */
    public String getTenantDomainFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        
        return claims.get("tenantDomain", String.class);
    }
}
