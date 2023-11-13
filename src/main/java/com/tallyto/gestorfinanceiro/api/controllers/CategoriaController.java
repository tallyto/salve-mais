package com.tallyto.gestorfinanceiro.api.controllers;

import com.tallyto.gestorfinanceiro.api.dto.CategoriaDTO;
import com.tallyto.gestorfinanceiro.core.application.services.CategoriaService;
import com.tallyto.gestorfinanceiro.core.domain.entities.Categoria;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/categorias")
@Validated
public class CategoriaController {

    private final CategoriaService categoriaService;

    @Autowired
    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @PostMapping
    public ResponseEntity<Categoria> criarCategoria(@Valid @RequestBody CategoriaDTO categoriaDTO) {
        Categoria categoria = mapDTOToEntity(categoriaDTO);
        Categoria categoriaSalva = categoriaService.salvarCategoria(categoria);
        return ResponseEntity.ok(categoriaSalva);
    }

    @GetMapping("/{nome}")
    public ResponseEntity<Categoria> buscarCategoriaPorNome(@PathVariable String nome) {
        Categoria categoria = categoriaService.buscarCategoriaPorNome(nome);
        return ResponseEntity.ok(categoria);
    }

    // Outros métodos relacionados a categorias

    private Categoria mapDTOToEntity(CategoriaDTO categoriaDTO) {
        Categoria categoria = new Categoria();
        categoria.setNome(categoriaDTO.nome());
        return categoria;
    }
}