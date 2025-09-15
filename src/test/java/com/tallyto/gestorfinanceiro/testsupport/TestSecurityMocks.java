package com.tallyto.gestorfinanceiro.testsupport;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestSecurityMocks {

    // Mock de JwtService para evitar dependência de tokens reais nos testes MVC.
    @MockBean
    private com.tallyto.gestorfinanceiro.core.application.services.JwtService jwtService;

    // Mock de UsuarioDetailsService para não carregar usuários reais.
    @MockBean
    private com.tallyto.gestorfinanceiro.core.application.services.UsuarioDetailsService usuarioDetailsService;
}
