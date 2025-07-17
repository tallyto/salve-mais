package com.tallyto.gestorfinanceiro.core.application.services;

import com.tallyto.gestorfinanceiro.api.dto.CartaoLimiteDTO;
import com.tallyto.gestorfinanceiro.api.dto.CartaoLimiteStatusDTO;
import com.tallyto.gestorfinanceiro.core.domain.entities.CartaoCredito;
import com.tallyto.gestorfinanceiro.core.domain.exceptions.CartaoCreditoException;
import com.tallyto.gestorfinanceiro.core.infra.repositories.CartaoCreditoRepository;
import com.tallyto.gestorfinanceiro.core.infra.repositories.CompraRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
public class CartaoCreditoService {

    @Autowired
    private CartaoCreditoRepository cartaoCreditoRepository;
    
    @Autowired
    private CompraRepository compraRepository;

    public CartaoCredito findOrFail(Long cartaoId){
        return cartaoCreditoRepository.findById(cartaoId).orElseThrow(() -> new CartaoCreditoException(cartaoId));
    }

    public CartaoCredito salvarCartaoCredito(CartaoCredito  cartaoCredito) {
        return cartaoCreditoRepository.save(cartaoCredito);
    }

    public List<CartaoCredito> listarCartoesCredito() {
        return cartaoCreditoRepository.findAll();
    }
    
    public void excluirCartaoCredito(Long id) {
        CartaoCredito cartaoCredito = findOrFail(id);
        cartaoCreditoRepository.delete(cartaoCredito);
    }
    
    /**
     * Configura o limite do cartão de crédito
     */
    public CartaoCredito configurarLimite(CartaoLimiteDTO limiteDTO) {
        CartaoCredito cartao = findOrFail(limiteDTO.cartaoId());
        cartao.setLimiteTotal(limiteDTO.limiteTotal());
        cartao.setLimiteAlertaPercentual(limiteDTO.limiteAlertaPercentual());
        return cartaoCreditoRepository.save(cartao);
    }
    
    /**
     * Verifica o status atual do limite do cartão
     */
    public CartaoLimiteStatusDTO verificarStatusLimite(Long cartaoId) {
        CartaoCredito cartao = findOrFail(cartaoId);
        
        if (cartao.getLimiteTotal() == null) {
            throw new IllegalStateException("Cartão não possui limite configurado");
        }
        
        // Calcula período atual (ciclo do cartão)
        LocalDate hoje = LocalDate.now();
        YearMonth mesAtual = YearMonth.from(hoje);
        LocalDate inicioMes = mesAtual.atDay(1);
        LocalDate fimMes = mesAtual.atEndOfMonth();
        
        // Busca valor utilizado no mês
        BigDecimal valorUtilizado = compraRepository.calcularValorUtilizadoPeriodo(cartaoId, inicioMes, fimMes);
        
        // Calcula valores
        BigDecimal limiteDisponivel = cartao.getLimiteTotal().subtract(valorUtilizado);
        BigDecimal percentualUtilizado = calcularPercentualUtilizado(valorUtilizado, cartao.getLimiteTotal());
        
        // Verifica alertas
        boolean limiteExcedido = valorUtilizado.compareTo(cartao.getLimiteTotal()) > 0;
        boolean alertaAtivado = percentualUtilizado.compareTo(BigDecimal.valueOf(cartao.getLimiteAlertaPercentual())) >= 0;
        
        return new CartaoLimiteStatusDTO(
            cartao.getId(),
            cartao.getNome(),
            cartao.getLimiteTotal(),
            valorUtilizado,
            limiteDisponivel,
            percentualUtilizado,
            limiteExcedido,
            alertaAtivado,
            cartao.getLimiteAlertaPercentual()
        );
    }
    
    /**
     * Lista status de limite de todos os cartões ativos
     */
    public List<CartaoLimiteStatusDTO> listarStatusLimiteTodos() {
        return cartaoCreditoRepository.findAll().stream()
            .filter(cartao -> cartao.getAtivo() && cartao.getLimiteTotal() != null)
            .map(cartao -> verificarStatusLimite(cartao.getId()))
            .toList();
    }
    
    /**
     * Verifica se algum cartão excedeu o limite ou está em alerta
     */
    public List<CartaoLimiteStatusDTO> verificarAlertas() {
        return listarStatusLimiteTodos().stream()
            .filter(status -> status.alertaAtivado() || status.limiteExcedido())
            .toList();
    }
    
    /**
     * Calcula valor disponível para uso no cartão
     */
    public BigDecimal calcularLimiteDisponivel(Long cartaoId) {
        CartaoLimiteStatusDTO status = verificarStatusLimite(cartaoId);
        return status.limiteDisponivel();
    }
    
    /**
     * Verifica se uma compra pode ser realizada sem exceder o limite
     */
    public boolean podeRealizarCompra(Long cartaoId, BigDecimal valorCompra) {
        try {
            BigDecimal limiteDisponivel = calcularLimiteDisponivel(cartaoId);
            return limiteDisponivel.compareTo(valorCompra) >= 0;
        } catch (Exception e) {
            // Se não tem limite configurado, permite a compra
            return true;
        }
    }
    
    private BigDecimal calcularPercentualUtilizado(BigDecimal valorUtilizado, BigDecimal limiteTotal) {
        if (limiteTotal.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return valorUtilizado.divide(limiteTotal, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }
}
