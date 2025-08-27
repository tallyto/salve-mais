package com.tallyto.gestorfinanceiro.api.controllers;

import com.tallyto.gestorfinanceiro.api.dto.CategoriaDTO;
import com.tallyto.gestorfinanceiro.core.application.services.CategoriaService;
import com.tallyto.gestorfinanceiro.core.domain.entities.Categoria;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categorias")
@Validated
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @GetMapping
    public List<Categoria> listarCategorias() {
        return categoriaService.listarCategorias();
    }
    
    /**
     * Endpoint para listar categorias agrupadas por tipo (50/30/20)
     * @return Mapa com os tipos de categoria e suas respectivas listas
     */
    @GetMapping("/por-tipo")
    public Map<Categoria.TipoCategoria, List<Categoria>> listarCategoriasPorTipo() {
        return categoriaService.listarCategoriasPorTipo();
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

    @GetMapping("/id/{id}")
    public ResponseEntity<Categoria> buscarCategoriaPorId(@PathVariable Long id) {
        Categoria categoria = categoriaService.buscaCategoriaPorId(id);
        return ResponseEntity.ok(categoria);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Categoria> atualizarCategoria(@PathVariable Long id, @Valid @RequestBody CategoriaDTO categoriaDTO) {
        Categoria categoria = mapDTOToEntity(categoriaDTO);
        Categoria categoriaAtualizada = categoriaService.atualizarCategoria(id, categoria);
        return ResponseEntity.ok(categoriaAtualizada);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirCategoria(@PathVariable Long id) {
        categoriaService.excluirCategoria(id);
        return ResponseEntity.noContent().build();
    }

    // Outros métodos relacionados a categorias

    private Categoria mapDTOToEntity(CategoriaDTO categoriaDTO) {
        Categoria categoria = new Categoria();
        categoria.setId(categoriaDTO.id());
        categoria.setNome(categoriaDTO.nome());
        categoria.setTipo(categoriaDTO.tipo());
        return categoria;
    }
}