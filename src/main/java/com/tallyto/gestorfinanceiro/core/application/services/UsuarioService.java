package com.tallyto.gestorfinanceiro.core.application.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tallyto.gestorfinanceiro.api.dto.UsuarioCadastroDTO;
import com.tallyto.gestorfinanceiro.context.TenantContext;
import com.tallyto.gestorfinanceiro.core.domain.entities.Tenant;
import com.tallyto.gestorfinanceiro.core.domain.entities.Usuario;
import com.tallyto.gestorfinanceiro.core.infra.repositories.TenantRepository;
import com.tallyto.gestorfinanceiro.core.infra.repositories.UsuarioRepository;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TenantRepository tenantRepository;
    
    @Autowired
    private TenantService tenantService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Transactional
    public void atualizarUltimoAcesso(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        usuario.setUltimoAcesso(java.time.LocalDateTime.now());
        usuarioRepository.save(usuario);
    }
    
    @Transactional
    public Usuario cadastrar(UsuarioCadastroDTO dto) {
        if (usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("E-mail já cadastrado");
        }
        Usuario usuario = new Usuario();
        usuario.setNome(dto.getNome());
        usuario.setEmail(dto.getEmail());
        usuario.setSenha(passwordEncoder.encode(dto.getSenha()));
        usuario.setCriadoEm(java.time.LocalDateTime.now());
        
        // Obter o tenant do contexto e definir o tenantId
        String tenantDomain = TenantContext.getCurrentTenant();
        if (tenantDomain != null && !"public".equals(tenantDomain)) {
            Tenant tenant = tenantRepository.getTenantByDomain(tenantDomain);
            if (tenant != null) {
                usuario.setTenantId(tenant.getId());
                
                // Invalidar token de criação após criar o primeiro usuário
                tenantService.invalidarTokenCriarUsuario(tenant.getId());
            }
        }
        
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public void atualizarSenhaPorEmail(String email, String novaSenha) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        usuario.setSenha(passwordEncoder.encode(novaSenha));
        usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario atualizarNomePorEmail(String email, String novoNome) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        usuario.setNome(novoNome);
        return usuarioRepository.save(usuario);
    }

    @Transactional(readOnly = true)
    public Usuario buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }

    /**
     * Atualiza a senha do usuário, validando a senha atual.
     * 
     * @param email      email do usuário
     * @param senhaAtual senha atual informada
     * @param novaSenha  nova senha desejada
     * @throws RuntimeException se usuário não encontrado ou senha atual incorreta
     */
    @Transactional
    public void atualizarSenhaComValidacao(String email, String senhaAtual, String novaSenha) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        if (!encoder.matches(senhaAtual, usuario.getSenha())) {
            throw new RuntimeException("Senha atual incorreta");
        }
        usuario.setSenha(encoder.encode(novaSenha));
        usuarioRepository.save(usuario);
    }

    // Métodos de Administração
    @Transactional(readOnly = true)
    public java.util.List<Usuario> listarTodosUsuarios() {
        return usuarioRepository.findAll();
    }

    @Transactional
    public void deletarUsuario(Long id, String emailUsuarioLogado) {
        Usuario usuarioParaDeletar = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        
        // Impedir que o usuário delete a si mesmo
        if (usuarioParaDeletar.getEmail().equals(emailUsuarioLogado)) {
            throw new RuntimeException("Você não pode deletar sua própria conta");
        }
        
        usuarioRepository.deleteById(id);
    }
}
