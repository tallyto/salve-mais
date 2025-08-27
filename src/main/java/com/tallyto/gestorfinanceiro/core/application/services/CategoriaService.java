package com.tallyto.gestorfinanceiro.core.application.services;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.tallyto.gestorfinanceiro.core.domain.entities.Categoria;
import com.tallyto.gestorfinanceiro.core.domain.exceptions.EntityInUseException;
import com.tallyto.gestorfinanceiro.core.domain.exceptions.ResourceNotFoundException;
import com.tallyto.gestorfinanceiro.core.infra.repositories.CategoriaRepository;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    public Categoria salvarCategoria(Categoria categoria) {
        return categoriaRepository.save(categoria);
    }

    public Categoria buscarCategoriaPorNome(String nome) {
        return categoriaRepository.findByNome(nome);
    }

    public Categoria buscaCategoriaPorId(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada com ID: " + id));
    }

    public List<Categoria> listarCategorias() {
        return categoriaRepository.findAll();
    }
    
    /**
     * Lista todas as categorias agrupadas por tipo (NECESSIDADE, DESEJO, ECONOMIA)
     * @return Mapa com os tipos e suas respectivas categorias
     */
    public Map<Categoria.TipoCategoria, List<Categoria>> listarCategoriasPorTipo() {
        List<Categoria> categorias = categoriaRepository.findAll();
        return categorias.stream()
                .collect(Collectors.groupingBy(Categoria::getTipo));
    }

    public void excluirCategoria(Long id) {
        // Verifica se a categoria existe antes de tentar excluir
        if (!categoriaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Categoria não encontrada com ID: " + id);
        }
        
        try {
            categoriaRepository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw new EntityInUseException("categoria", id, "contas fixas ou outras transações");
        }
    }

    public Categoria atualizarCategoria(Long id, Categoria categoria) {
        Categoria categoriaExistente = buscaCategoriaPorId(id);
        categoriaExistente.setNome(categoria.getNome());
        return categoriaRepository.save(categoriaExistente);
    }
}