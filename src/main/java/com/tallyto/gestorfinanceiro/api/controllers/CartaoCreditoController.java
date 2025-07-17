package com.tallyto.gestorfinanceiro.api.controllers;

import com.tallyto.gestorfinanceiro.api.dto.CartaoLimiteDTO;
import com.tallyto.gestorfinanceiro.api.dto.CartaoLimiteStatusDTO;
import com.tallyto.gestorfinanceiro.core.application.services.CartaoCreditoService;
import com.tallyto.gestorfinanceiro.core.domain.entities.CartaoCredito;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cartao-credito")
public class CartaoCreditoController {

    @Autowired
    private CartaoCreditoService cartaoCreditoService;

    @GetMapping
    public List<CartaoCredito> listarCartaoCredito() {
        return cartaoCreditoService.listarCartoesCredito();
    }
    
    @GetMapping("/{id}")
    public CartaoCredito buscarCartaoCredito(@PathVariable Long id) {
        return cartaoCreditoService.findOrFail(id);
    }

    @PostMapping
    public CartaoCredito salvarCartaoCredito(@RequestBody CartaoCredito cartaoCredito) {
        return cartaoCreditoService.salvarCartaoCredito(cartaoCredito);
    }
    
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluirCartaoCredito(@PathVariable Long id) {
        cartaoCreditoService.excluirCartaoCredito(id);
    }
    
    // ===== ENDPOINTS PARA SISTEMA DE LIMITES =====
    
    /**
     * Configura o limite do cartão de crédito
     */
    @PutMapping("/{id}/limite")
    public ResponseEntity<CartaoCredito> configurarLimite(
            @PathVariable Long id,
            @Valid @RequestBody CartaoLimiteDTO limiteDTO) {
        
        // Valida se o ID do path é o mesmo do DTO
        if (!id.equals(limiteDTO.cartaoId())) {
            return ResponseEntity.badRequest().build();
        }
        
        CartaoCredito cartaoAtualizado = cartaoCreditoService.configurarLimite(limiteDTO);
        return ResponseEntity.ok(cartaoAtualizado);
    }
    
    /**
     * Verifica o status atual do limite do cartão
     */
    @GetMapping("/{id}/limite/status")
    public ResponseEntity<CartaoLimiteStatusDTO> verificarStatusLimite(@PathVariable Long id) {
        try {
            CartaoLimiteStatusDTO status = cartaoCreditoService.verificarStatusLimite(id);
            return ResponseEntity.ok(status);
        } catch (IllegalStateException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Lista status de limite de todos os cartões ativos
     */
    @GetMapping("/limite/status")
    public List<CartaoLimiteStatusDTO> listarStatusLimiteTodos() {
        return cartaoCreditoService.listarStatusLimiteTodos();
    }
    
    /**
     * Verifica cartões com alertas de limite
     */
    @GetMapping("/limite/alertas")
    public List<CartaoLimiteStatusDTO> verificarAlertas() {
        return cartaoCreditoService.verificarAlertas();
    }
    
    /**
     * Calcula limite disponível para uso
     */
    @GetMapping("/{id}/limite/disponivel")
    public ResponseEntity<Map<String, BigDecimal>> calcularLimiteDisponivel(@PathVariable Long id) {
        try {
            BigDecimal limiteDisponivel = cartaoCreditoService.calcularLimiteDisponivel(id);
            return ResponseEntity.ok(Map.of("limiteDisponivel", limiteDisponivel));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Verifica se uma compra pode ser realizada
     */
    @PostMapping("/{id}/limite/verificar-compra")
    public ResponseEntity<Map<String, Object>> verificarCompra(
            @PathVariable Long id,
            @RequestBody Map<String, BigDecimal> request) {
        
        BigDecimal valorCompra = request.get("valor");
        if (valorCompra == null) {
            return ResponseEntity.badRequest().build();
        }
        
        boolean podeRealizar = cartaoCreditoService.podeRealizarCompra(id, valorCompra);
        BigDecimal limiteDisponivel = null;
        
        try {
            limiteDisponivel = cartaoCreditoService.calcularLimiteDisponivel(id);
        } catch (Exception e) {
            // Ignora erro se não há limite configurado
        }
        
        Map<String, Object> response = Map.of(
            "podeRealizar", podeRealizar,
            "valorCompra", valorCompra,
            "limiteDisponivel", limiteDisponivel != null ? limiteDisponivel : BigDecimal.ZERO
        );
        
        return ResponseEntity.ok(response);
    }
}
