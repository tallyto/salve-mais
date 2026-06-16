package br.com.salvemais.testsupport;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestSecurityMocks {

    // Mock de JwtService para evitar dependência de tokens reais nos testes MVC.
    @MockBean
    private br.com.salvemais.application.services.JwtService jwtService;

    // Mock de UsuarioDetailsService para não carregar usuários reais.
    @MockBean
    private br.com.salvemais.application.services.UsuarioDetailsService usuarioDetailsService;
}
