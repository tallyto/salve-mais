package com.tallyto.gestorfinanceiro.core.application.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.tallyto.gestorfinanceiro.core.domain.entities.Categoria;
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
        return categoriaRepository.findById(id).orElseThrow();
    }

    public List<Categoria> listarCategorias() {
        return categoriaRepository.findAll();
    }

}