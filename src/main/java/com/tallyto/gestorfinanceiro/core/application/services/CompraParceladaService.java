package com.tallyto.gestorfinanceiro.core.application.services;

import com.tallyto.gestorfinanceiro.core.domain.entities.CartaoCredito;
import com.tallyto.gestorfinanceiro.core.domain.entities.Categoria;
import com.tallyto.gestorfinanceiro.core.domain.entities.CompraParcelada;
import com.tallyto.gestorfinanceiro.core.domain.entities.Parcela;
import com.tallyto.gestorfinanceiro.core.infra.repositories.CompraParceladaRepository;
import com.tallyto.gestorfinanceiro.core.infra.repositories.ParcelaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class CompraParceladaService {

    @Autowired
    private CompraParceladaRepository compraParceladaRepository;

    @Autowired
    private ParcelaRepository parcelaRepository;

    @Autowired
    private CartaoCreditoService cartaoCreditoService;

    @Autowired
    private CategoriaService categoriaService;

    /**
     * Cria uma compra parcelada e gera as parcelas automaticamente
     * @param compraParcelada dados da compra parcelada
     * @return compra parcelada salva com suas parcelas
     */
    @Transactional
    public CompraParcelada criarCompraParcelada(CompraParcelada compraParcelada) {
        // Valida valor total
        if (compraParcelada.getValorTotal() == null || compraParcelada.getValorTotal().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor total deve ser maior que zero");
        }

        // Valida cartão
        CartaoCredito cartao = cartaoCreditoService.findOrFail(compraParcelada.getCartaoCredito().getId());
        compraParcelada.setCartaoCredito(cartao);

        // Valida categoria se informada
        if (compraParcelada.getCategoria() != null && compraParcelada.getCategoria().getId() != null) {
            Categoria categoria = categoriaService.buscaCategoriaPorId(compraParcelada.getCategoria().getId());
            compraParcelada.setCategoria(categoria);
        }

        // Valida parcelas
        if (compraParcelada.getParcelaInicial() == null || compraParcelada.getParcelaInicial() < 1) {
            throw new IllegalArgumentException("Parcela inicial deve ser no mínimo 1");
        }
        if (compraParcelada.getTotalParcelas() == null || compraParcelada.getTotalParcelas() < 1) {
            throw new IllegalArgumentException("Total de parcelas deve ser no mínimo 1");
        }
        if (compraParcelada.getParcelaInicial() > compraParcelada.getTotalParcelas()) {
            throw new IllegalArgumentException(
                String.format("Parcela inicial (%d) não pode ser maior que o total de parcelas (%d)", 
                    compraParcelada.getParcelaInicial(), 
                    compraParcelada.getTotalParcelas())
            );
        }

        // Salva a compra parcelada
        CompraParcelada compraSalva = compraParceladaRepository.save(compraParcelada);

        // Gera as parcelas
        List<Parcela> parcelas = gerarParcelas(compraSalva);
        compraSalva.setParcelas(parcelas);

        return compraSalva;
    }

    /**
     * Gera as parcelas da compra parcelada
     */
    private List<Parcela> gerarParcelas(CompraParcelada compraParcelada) {
        List<Parcela> parcelas = new ArrayList<>();
        
        // Calcula quantas parcelas serão geradas (da inicial até a última)
        int parcelasRestantes = compraParcelada.getTotalParcelas() - compraParcelada.getParcelaInicial() + 1;
        
        // Divide o valor total pelo TOTAL de parcelas (não pelas restantes)
        // Exemplo: R$ 1.803,36 em 5x começando da 3ª = R$ 1.803,36 / 5 = R$ 360,67 por parcela
        BigDecimal valorParcela = compraParcelada.getValorTotal()
                .divide(BigDecimal.valueOf(compraParcelada.getTotalParcelas()), 2, RoundingMode.HALF_UP);

        // Ajuste para garantir que a soma de TODAS as parcelas (incluindo as não geradas) seja igual ao valor total
        // Calcula a diferença considerando o total de parcelas
        BigDecimal somaTotal = valorParcela.multiply(BigDecimal.valueOf(compraParcelada.getTotalParcelas()));
        BigDecimal diferenca = compraParcelada.getValorTotal().subtract(somaTotal);

        for (int i = compraParcelada.getParcelaInicial(); i <= compraParcelada.getTotalParcelas(); i++) {
            Parcela parcela = new Parcela();
            parcela.setNumeroParcela(i);
            parcela.setTotalParcelas(compraParcelada.getTotalParcelas());
            
            // Adiciona a diferença apenas na última parcela para garantir que a soma seja exata
            // Isso evita que a primeira parcela fique com valor diferente
            if (i == compraParcelada.getTotalParcelas()) {
                parcela.setValor(valorParcela.add(diferenca));
            } else {
                parcela.setValor(valorParcela);
            }

            // Calcula a data de vencimento (um mês após a data da compra para cada parcela)
            int mesesAFrente = i - compraParcelada.getParcelaInicial();
            LocalDate dataVencimento = compraParcelada.getDataCompra().plusMonths(mesesAFrente);
            parcela.setDataVencimento(dataVencimento);
            
            parcela.setPaga(false);
            parcela.setCompraParcelada(compraParcelada);
            
            parcelas.add(parcelaRepository.save(parcela));
        }

        return parcelas;
    }

    /**
     * Lista todas as compras parceladas com paginação
     */
    public Page<CompraParcelada> listarComprasParceladas(Pageable pageable) {
        return compraParceladaRepository.findAll(pageable);
    }

    /**
     * Busca compras parceladas por cartão
     */
    public Page<CompraParcelada> listarComprasParceladasPorCartao(Long cartaoId, Pageable pageable) {
        return compraParceladaRepository.findByCartaoCreditoId(cartaoId, pageable);
    }

    /**
     * Busca compra parcelada por ID
     */
    public CompraParcelada buscarPorId(Long id) {
        return compraParceladaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Compra parcelada não encontrada com ID: " + id));
    }

    /**
     * Busca parcelas de uma compra parcelada
     */
    public List<Parcela> listarParcelasPorCompra(Long compraParceladaId) {
        return parcelaRepository.findByCompraParceladaId(compraParceladaId);
    }

    /**
     * Busca parcelas por cartão e período
     */
    public List<Parcela> listarParcelasPorCartaoEPeriodo(Long cartaoId, LocalDate inicio, LocalDate fim) {
        return parcelaRepository.findByCartaoAndPeriodo(cartaoId, inicio, fim);
    }

    /**
     * Marca uma parcela como paga
     */
    @Transactional
    public Parcela marcarParcelaComoPaga(Long parcelaId) {
        Parcela parcela = parcelaRepository.findById(parcelaId)
                .orElseThrow(() -> new RuntimeException("Parcela não encontrada com ID: " + parcelaId));
        
        parcela.setPaga(true);
        return parcelaRepository.save(parcela);
    }

    /**
     * Desmarca uma parcela como paga
     */
    @Transactional
    public Parcela desmarcarParcelaComoPaga(Long parcelaId) {
        Parcela parcela = parcelaRepository.findById(parcelaId)
                .orElseThrow(() -> new RuntimeException("Parcela não encontrada com ID: " + parcelaId));
        
        parcela.setPaga(false);
        return parcelaRepository.save(parcela);
    }

    /**
     * Atualiza uma compra parcelada
     */
    @Transactional
    public CompraParcelada atualizarCompraParcelada(Long id, CompraParcelada compraAtualizada) {
        // Busca a compra existente
        CompraParcelada compraExistente = buscarPorId(id);

        // Valida valor total
        if (compraAtualizada.getValorTotal() == null || compraAtualizada.getValorTotal().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor total deve ser maior que zero");
        }

        // Valida cartão
        CartaoCredito cartao = cartaoCreditoService.findOrFail(compraAtualizada.getCartaoCredito().getId());
        
        // Valida categoria se informada
        if (compraAtualizada.getCategoria() != null && compraAtualizada.getCategoria().getId() != null) {
            Categoria categoria = categoriaService.buscaCategoriaPorId(compraAtualizada.getCategoria().getId());
            compraExistente.setCategoria(categoria);
        }

        // Valida parcelas
        if (compraAtualizada.getParcelaInicial() == null || compraAtualizada.getParcelaInicial() < 1) {
            throw new IllegalArgumentException("Parcela inicial deve ser no mínimo 1");
        }
        if (compraAtualizada.getTotalParcelas() == null || compraAtualizada.getTotalParcelas() < 1) {
            throw new IllegalArgumentException("Total de parcelas deve ser no mínimo 1");
        }
        if (compraAtualizada.getParcelaInicial() > compraAtualizada.getTotalParcelas()) {
            throw new IllegalArgumentException(
                String.format("Parcela inicial (%d) não pode ser maior que o total de parcelas (%d)", 
                    compraAtualizada.getParcelaInicial(), 
                    compraAtualizada.getTotalParcelas())
            );
        }

        // Atualiza os dados básicos
        compraExistente.setDescricao(compraAtualizada.getDescricao());
        compraExistente.setValorTotal(compraAtualizada.getValorTotal());
        compraExistente.setDataCompra(compraAtualizada.getDataCompra());
        compraExistente.setCartaoCredito(cartao);
        
        // Verifica se houve mudança nas parcelas
        boolean parcelasAlteradas = !compraExistente.getParcelaInicial().equals(compraAtualizada.getParcelaInicial()) ||
                                   !compraExistente.getTotalParcelas().equals(compraAtualizada.getTotalParcelas());

        if (parcelasAlteradas) {
            // Remove parcelas antigas
            parcelaRepository.deleteAll(compraExistente.getParcelas());
            
            // Atualiza os valores de parcela
            compraExistente.setParcelaInicial(compraAtualizada.getParcelaInicial());
            compraExistente.setTotalParcelas(compraAtualizada.getTotalParcelas());
            
            // Salva a compra atualizada
            CompraParcelada compraSalva = compraParceladaRepository.save(compraExistente);
            
            // Gera novas parcelas
            List<Parcela> parcelas = gerarParcelas(compraSalva);
            compraSalva.setParcelas(parcelas);
            
            return compraSalva;
        } else {
            // Se não houve mudança nas parcelas, apenas atualiza os valores das parcelas existentes
            compraExistente.setParcelaInicial(compraAtualizada.getParcelaInicial());
            compraExistente.setTotalParcelas(compraAtualizada.getTotalParcelas());
            
            CompraParcelada compraSalva = compraParceladaRepository.save(compraExistente);
            
            // Atualiza os valores das parcelas existentes
            atualizarValoresParcelas(compraSalva);
            
            return compraSalva;
        }
    }

    /**
     * Atualiza os valores das parcelas existentes quando o valor total muda
     */
    private void atualizarValoresParcelas(CompraParcelada compraParcelada) {
        List<Parcela> parcelas = parcelaRepository.findByCompraParceladaId(compraParcelada.getId());
        
        if (parcelas.isEmpty()) {
            return;
        }

        // Calcula o novo valor de cada parcela
        BigDecimal valorParcela = compraParcelada.getValorTotal()
                .divide(BigDecimal.valueOf(compraParcelada.getTotalParcelas()), 2, RoundingMode.HALF_UP);

        // Calcula a diferença para ajustar na última parcela
        BigDecimal somaTotal = valorParcela.multiply(BigDecimal.valueOf(compraParcelada.getTotalParcelas()));
        BigDecimal diferenca = compraParcelada.getValorTotal().subtract(somaTotal);

        for (int i = 0; i < parcelas.size(); i++) {
            Parcela parcela = parcelas.get(i);
            
            // Adiciona a diferença na última parcela
            if (i == parcelas.size() - 1) {
                parcela.setValor(valorParcela.add(diferenca));
            } else {
                parcela.setValor(valorParcela);
            }
            
            parcelaRepository.save(parcela);
        }
    }

    /**
     * Exclui uma compra parcelada e todas as suas parcelas
     */
    @Transactional
    public void excluirCompraParcelada(Long id) {
        CompraParcelada compra = buscarPorId(id);
        compraParceladaRepository.delete(compra);
    }

    /**
     * Busca parcelas não pagas de um cartão
     */
    public List<Parcela> listarParcelasNaoPagas(Long cartaoId) {
        return parcelaRepository.findParcelasNaoPagasByCartao(cartaoId);
    }

    /**
     * Busca parcelas vencidas
     */
    public List<Parcela> listarParcelasVencidas() {
        return parcelaRepository.findParcelasVencidas(LocalDate.now());
    }
}
