package com.tallyto.gestorfinanceiro.core.application.services;

import com.tallyto.gestorfinanceiro.core.domain.entities.Categoria;
import com.tallyto.gestorfinanceiro.core.infra.repositories.CategoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    @Autowired
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