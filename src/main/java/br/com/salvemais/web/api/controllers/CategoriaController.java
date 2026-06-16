package br.com.salvemais.web.api.controllers;

import br.com.salvemais.web.api.dto.CategoriaDTO;
import br.com.salvemais.application.services.CategoriaService;
import br.com.salvemais.domain.entities.Categoria;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Categorias", description = "Gestão de categorias")
@RestController
@RequestMapping("/api/categorias")
@Validated
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @GetMapping
    @Operation(summary = "Listar categorias")
    public List<Categoria> listarCategorias() {
        return categoriaService.listarCategorias();
    }
    
    /**
     * Endpoint para listar categorias agrupadas por tipo (50/30/20)
     * @return Mapa com os tipos de categoria e suas respectivas listas
     */
    @GetMapping("/por-tipo")
    @Operation(summary = "Listar categorias agrupadas por tipo")
    public Map<Categoria.TipoCategoria, List<Categoria>> listarCategoriasPorTipo() {
        return categoriaService.listarCategoriasPorTipo();
    }

    @PostMapping
    @Operation(summary = "Criar categoria")
    public ResponseEntity<Categoria> criarCategoria(@Valid @RequestBody CategoriaDTO categoriaDTO) {
        Categoria categoria = mapDTOToEntity(categoriaDTO);
        Categoria categoriaSalva = categoriaService.salvarCategoria(categoria);
        return ResponseEntity.ok(categoriaSalva);
    }

    @GetMapping("/{nome}")
    @Operation(summary = "Buscar categoria por nome")
    public ResponseEntity<Categoria> buscarCategoriaPorNome(@PathVariable String nome) {
        Categoria categoria = categoriaService.buscarCategoriaPorNome(nome);
        return ResponseEntity.ok(categoria);
    }

    @GetMapping("/id/{id}")
    @Operation(summary = "Buscar categoria por ID")
    public ResponseEntity<Categoria> buscarCategoriaPorId(@PathVariable Long id) {
        Categoria categoria = categoriaService.buscaCategoriaPorId(id);
        return ResponseEntity.ok(categoria);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar categoria")
    public ResponseEntity<Categoria> atualizarCategoria(@PathVariable Long id, @Valid @RequestBody CategoriaDTO categoriaDTO) {
        Categoria categoria = mapDTOToEntity(categoriaDTO);
        Categoria categoriaAtualizada = categoriaService.atualizarCategoria(id, categoria);
        return ResponseEntity.ok(categoriaAtualizada);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir categoria")
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
