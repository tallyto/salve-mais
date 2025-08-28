package com.tallyto.gestorfinanceiro.core.domain.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Categoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    
    @Enumerated(EnumType.STRING)
    private TipoCategoria tipo = TipoCategoria.NECESSIDADE;
    
    /**
     * Enum para representar os tipos de categoria do sistema 50/30/20
     * - NECESSIDADE: Representa os 50% do orçamento (despesas essenciais)
     * - DESEJO: Representa os 30% do orçamento (gastos com lazer e desejos)
     * - ECONOMIA: Representa os 20% do orçamento (poupança e investimentos)
     */
    public enum TipoCategoria {
        NECESSIDADE,  // 50%
        DESEJO,       // 30%
        ECONOMIA      // 20%
    }
}