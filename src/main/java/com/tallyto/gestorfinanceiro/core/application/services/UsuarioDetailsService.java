package com.tallyto.gestorfinanceiro.core.application.services;

import com.tallyto.gestorfinanceiro.core.domain.entities.UsuarioGlobal;
import com.tallyto.gestorfinanceiro.core.infra.repositories.UsuarioGlobalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Serviço de detalhes de usuário para autenticação.
 * Agora utiliza a tabela usuario_global centralizada no schema public.
 */
@Service
public class UsuarioDetailsService implements UserDetailsService {
    @Autowired
    private UsuarioGlobalRepository usuarioGlobalRepository;

    /**
     * Carrega os detalhes do usuário pelo email.
     * Este método busca na tabela usuario_global (centralizada no schema public)
     * 
     * @param email email do usuário
     * @return UserDetails com as credenciais do usuário
     * @throws UsernameNotFoundException se o usuário não for encontrado
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UsuarioGlobal usuarioGlobal = usuarioGlobalRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
        
        // Verificar se o usuário está ativo
        if (!usuarioGlobal.getAtivo()) {
            throw new UsernameNotFoundException("Usuário inativo");
        }
        
        return org.springframework.security.core.userdetails.User
                .withUsername(usuarioGlobal.getEmail())
                .password(usuarioGlobal.getSenha())
                .authorities("USER")
                .build();
    }
}
