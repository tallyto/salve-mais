package com.tallyto.gestorfinanceiro.api.controllers;

import com.tallyto.gestorfinanceiro.api.dto.UsuarioAtualizacaoDTO;
import com.tallyto.gestorfinanceiro.api.dto.UsuarioCadastroDTO;
import com.tallyto.gestorfinanceiro.api.dto.UsuarioResponseDTO;
import com.tallyto.gestorfinanceiro.api.dto.UsuarioSenhaDTO;
import com.tallyto.gestorfinanceiro.config.openapi.OpenApiPublic;
import com.tallyto.gestorfinanceiro.core.application.services.UsuarioService;
import com.tallyto.gestorfinanceiro.core.domain.entities.Usuario;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Usuários", description = "Cadastro e administração de usuários")
@RestController
@RequestMapping("api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @PostMapping
    @OpenApiPublic
    @Operation(summary = "Cadastrar novo usuário")
    public ResponseEntity<Usuario> cadastrar(@RequestBody UsuarioCadastroDTO dto) {
        Usuario usuario = usuarioService.cadastrar(dto);
        return new ResponseEntity<>(usuario, HttpStatus.CREATED);
    }

    @PutMapping("/atualizar-nome")
    @Operation(summary = "Atualizar nome do usuário")
    public ResponseEntity<Usuario> atualizarNome(@RequestBody UsuarioAtualizacaoDTO dto) {
        Usuario usuario = usuarioService.atualizarNomePorEmail(dto.getEmail(), dto.getNome());
        return ResponseEntity.ok(usuario);
    }

    @PutMapping("/atualizar-senha")
    @Operation(summary = "Atualizar senha do usuário")
    public ResponseEntity<?> atualizarSenha(@RequestBody UsuarioSenhaDTO dto) {
        try {
            usuarioService.atualizarSenhaComValidacao(dto.getEmail(), dto.getSenhaAtual(), dto.getNovaSenha());
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/me")
    @Operation(summary = "Obter usuário autenticado")
    public ResponseEntity<UsuarioResponseDTO> getUsuarioLogado(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Usuario usuario = usuarioService.buscarPorEmail(userDetails.getUsername());
        return ResponseEntity.ok(new UsuarioResponseDTO(usuario));
    }

    // Endpoints de Administração
    @GetMapping("/admin/listar")
    @Operation(summary = "Listar usuários cadastrados")
    public ResponseEntity<List<UsuarioResponseDTO>> listarUsuarios() {
        List<Usuario> usuarios = usuarioService.listarTodosUsuarios();
        List<UsuarioResponseDTO> usuariosDTO = usuarios.stream()
                .map(UsuarioResponseDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(usuariosDTO);
    }

    @PostMapping("/admin/criar")
    @Operation(summary = "Criar usuário via administração")
    public ResponseEntity<UsuarioResponseDTO> criarUsuario(@RequestBody UsuarioCadastroDTO dto) {
        Usuario usuario = usuarioService.cadastrar(dto);
        return new ResponseEntity<>(new UsuarioResponseDTO(usuario), HttpStatus.CREATED);
    }

    @DeleteMapping("/admin/{id}")
    @Operation(summary = "Excluir usuário")
    public ResponseEntity<?> deletarUsuario(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            if (userDetails == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            usuarioService.deletarUsuario(id, userDetails.getUsername());
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
