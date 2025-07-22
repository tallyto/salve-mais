package com.tallyto.gestorfinanceiro.api.controllers;

import com.tallyto.gestorfinanceiro.api.dto.UsuarioAtualizacaoDTO;
import com.tallyto.gestorfinanceiro.api.dto.UsuarioCadastroDTO;
import com.tallyto.gestorfinanceiro.api.dto.UsuarioSenhaDTO;
import com.tallyto.gestorfinanceiro.core.application.services.UsuarioService;
import com.tallyto.gestorfinanceiro.core.domain.entities.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

@RestController
@RequestMapping("api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @PostMapping
    public ResponseEntity<Usuario> cadastrar(@RequestBody UsuarioCadastroDTO dto) {
        Usuario usuario = usuarioService.cadastrar(dto);
        return new ResponseEntity<>(usuario, HttpStatus.CREATED);
    }

    @PutMapping("/atualizar-nome")
    public ResponseEntity<Usuario> atualizarNome(@RequestBody UsuarioAtualizacaoDTO dto) {
        Usuario usuario = usuarioService.atualizarNomePorEmail(dto.getEmail(), dto.getNome());
        return ResponseEntity.ok(usuario);
    }

    @PutMapping("/atualizar-senha")
    public ResponseEntity<?> atualizarSenha(@RequestBody UsuarioSenhaDTO dto) {
        try {
            usuarioService.atualizarSenhaComValidacao(dto.getEmail(), dto.getSenhaAtual(), dto.getNovaSenha());
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/me")
    public ResponseEntity<Usuario> getUsuarioLogado(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Usuario usuario = usuarioService.buscarPorEmail(userDetails.getUsername());
        return ResponseEntity.ok(usuario);
    }
}
