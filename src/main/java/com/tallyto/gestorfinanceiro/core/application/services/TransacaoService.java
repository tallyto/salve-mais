package com.tallyto.gestorfinanceiro.core.application.services;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tallyto.gestorfinanceiro.api.dto.TransacaoDTO;
import com.tallyto.gestorfinanceiro.api.dto.TransacaoFiltroDTO;
import com.tallyto.gestorfinanceiro.api.dto.TransacaoInputDTO;
import com.tallyto.gestorfinanceiro.core.domain.entities.Categoria;
import com.tallyto.gestorfinanceiro.core.domain.entities.Conta;
import com.tallyto.gestorfinanceiro.core.domain.entities.ContaFixa;
import com.tallyto.gestorfinanceiro.core.domain.entities.Fatura;
import com.tallyto.gestorfinanceiro.core.domain.entities.Provento;
import com.tallyto.gestorfinanceiro.core.domain.entities.Transacao;
import com.tallyto.gestorfinanceiro.core.domain.exceptions.ResourceNotFoundException;
import com.tallyto.gestorfinanceiro.core.infra.repositories.CategoriaRepository;
import com.tallyto.gestorfinanceiro.core.infra.repositories.ContaFixaRepository;
import com.tallyto.gestorfinanceiro.core.infra.repositories.ContaRepository;
import com.tallyto.gestorfinanceiro.core.infra.repositories.FaturaRepository;
import com.tallyto.gestorfinanceiro.core.infra.repositories.ProventoRepository;
import com.tallyto.gestorfinanceiro.core.infra.repositories.TransacaoRepository;

@Service
public class TransacaoService {
    /**
     * Remove uma transação pelo ID
     */
    @Transactional
    public void removerTransacao(Long id) {
        Transacao transacao = findById(id);
        transacaoRepository.delete(transacao);
    }

    @Autowired
    private TransacaoRepository transacaoRepository;

    @Autowired
    private ContaRepository contaRepository;
    
    @Autowired
    private CategoriaRepository categoriaRepository;
    
    @Autowired
    private ProventoRepository proventoRepository;
    
    @Autowired
    private ContaFixaRepository contaFixaRepository;
    
    @Autowired
    private FaturaRepository faturaRepository;

    /**
     * Busca uma transação por ID
     */
    public Transacao findById(Long id) {
        return transacaoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transação não encontrada com ID: " + id));
    }
    
    /**
     * Busca transações relacionadas a um provento específico
     */
    public List<Transacao> findByProventoId(Long proventoId) {
        return transacaoRepository.findByProvento_Id(proventoId);
    }

    /**
     * Lista transações com paginação e filtros
     */
    public Page<TransacaoDTO> listarTransacoes(TransacaoFiltroDTO filtro, Pageable pageable) {
        // Implementação básica para exemplo - em um cenário real, 
        // seria necessário uma consulta mais complexa com Specification ou QueryDSL
        if (filtro.contaId() != null) {
            return transacaoRepository.findByConta_IdOrderByDataDesc(filtro.contaId(), pageable)
                    .map(this::toDTO);
        }
        
        return transacaoRepository.findAllByOrderByDataDesc(pageable)
                    .map(this::toDTO);
    }

    /**
     * Cria uma nova transação e atualiza os saldos
     * Este método é usado pelo ContaService e mantido para compatibilidade com código existente
     */
    @Transactional
    public TransacaoDTO criarTransacao(TransacaoInputDTO inputDTO) {
        // Validação de negócios aqui - em um contexto real, você pode adicionar validações
        // específicas ou chamar o ContaService para gerenciar saldos
        
        // No momento, vamos apenas encaminhar para o método que não atualiza saldo
        // Nota: o saldo deve ser gerenciado pelo ContaService, não aqui
        return criarTransacaoSemAtualizarSaldo(inputDTO);
    }
    
    /**
     * Cria uma nova transação sem atualizar saldos.
     * A responsabilidade por atualizar os saldos é do ContaService.
     */
    @Transactional
    public TransacaoDTO criarTransacaoSemAtualizarSaldo(TransacaoInputDTO inputDTO) {
        // Valida os campos da transação
        inputDTO.validar();
        
        Transacao transacao = new Transacao();
        transacao.setTipo(inputDTO.tipo());
        transacao.setValor(inputDTO.valor());
        transacao.setDescricao(inputDTO.descricao());
        transacao.setData(LocalDateTime.now());
        transacao.setObservacoes(inputDTO.observacoes());
        
        // Busca e configura a conta principal
        Conta conta = contaRepository.findById(inputDTO.contaId())
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada com ID: " + inputDTO.contaId()));
        transacao.setConta(conta);
        
        // Configura campos específicos conforme o tipo de transação
        switch (inputDTO.tipo()) {
            case CREDITO:
                configurarCredito(transacao, inputDTO);
                break;
                
            case DEBITO:
                configurarDebito(transacao, inputDTO);
                break;
                
            case TRANSFERENCIA_SAIDA:
                configurarTransferenciaSaida(transacao, inputDTO);
                break;
                
            case TRANSFERENCIA_ENTRADA:
                configurarTransferenciaEntrada(transacao, inputDTO);
                break;
                
            case PAGAMENTO_FATURA:
                configurarPagamentoFatura(transacao, inputDTO);
                break;
        }
        
        // Configura categoria se informada
        if (inputDTO.categoriaId() != null) {
            Categoria categoria = categoriaRepository.findById(inputDTO.categoriaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada com ID: " + inputDTO.categoriaId()));
            transacao.setCategoria(categoria);
        }
        
        // Salva a transação
        Transacao transacaoSalva = transacaoRepository.save(transacao);
        
        return toDTO(transacaoSalva);
    }
    
    /**
     * Converte uma entidade Transacao para um TransacaoDTO
     */
    public TransacaoDTO toDTO(Transacao transacao) {
        if (transacao == null) {
            return null;
        }
        
        return new TransacaoDTO(
            transacao.getId(),
            transacao.getTipo(),
            transacao.getValor(),
            transacao.getData(),
            transacao.getDescricao(),
            mapContaResumo(transacao.getConta()),
            mapContaResumo(transacao.getContaDestino()),
            mapFaturaResumo(transacao.getFatura()),
            mapCategoriaResumo(transacao.getCategoria()),
            mapProventoResumo(transacao.getProvento()),
            mapContaFixaResumo(transacao.getContaFixa()),
            transacao.getObservacoes()
        );
    }
    
    /**
     * Mapeia uma Conta para ContaResumoDTO
     */
    private TransacaoDTO.ContaResumoDTO mapContaResumo(Conta conta) {
        if (conta == null) {
            return null;
        }
        return new TransacaoDTO.ContaResumoDTO(
                conta.getId(),
                conta.getTitular()
        );
    }
    
    /**
     * Mapeia uma Fatura para FaturaResumoDTO
     */
    private TransacaoDTO.FaturaResumoDTO mapFaturaResumo(Fatura fatura) {
        if (fatura == null) {
            return null;
        }
        return new TransacaoDTO.FaturaResumoDTO(
                fatura.getId(),
                fatura.getCartaoCredito() != null 
                        ? fatura.getCartaoCredito().getNome() 
                        : "Desconhecido",
                fatura.getDataVencimento() != null 
                        ? LocalDateTime.of(fatura.getDataVencimento(), LocalTime.MIDNIGHT) 
                        : null,
                fatura.getValorTotal()
        );
    }
    
    /**
     * Mapeia uma Categoria para CategoriaResumoDTO
     */
    private TransacaoDTO.CategoriaResumoDTO mapCategoriaResumo(Categoria categoria) {
        if (categoria == null) {
            return null;
        }
        return new TransacaoDTO.CategoriaResumoDTO(
                categoria.getId(),
                categoria.getNome()
        );
    }
    
    /**
     * Mapeia um Provento para ProventoResumoDTO
     */
    private TransacaoDTO.ProventoResumoDTO mapProventoResumo(Provento provento) {
        if (provento == null) {
            return null;
        }
        return new TransacaoDTO.ProventoResumoDTO(
                provento.getId(),
                provento.getDescricao(),
                provento.getValor()
        );
    }
    
    /**
     * Mapeia uma ContaFixa para ContaFixaResumoDTO
     */
    private TransacaoDTO.ContaFixaResumoDTO mapContaFixaResumo(ContaFixa contaFixa) {
        if (contaFixa == null) {
            return null;
        }
        return new TransacaoDTO.ContaFixaResumoDTO(
                contaFixa.getId(),
                contaFixa.getNome(),
                contaFixa.getValor()
        );
    }
    
    /**
     * Configura e processa uma transação de crédito
     */
    private void configurarCredito(Transacao transacao, TransacaoInputDTO inputDTO) {
        // Configura provento se informado
        if (inputDTO.proventoId() != null) {
            Provento provento = proventoRepository.findById(inputDTO.proventoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Provento não encontrado com ID: " + inputDTO.proventoId()));
            transacao.setProvento(provento);
        }
    }
    
    /**
     * Configura uma transação de débito
     */
    private void configurarDebito(Transacao transacao, TransacaoInputDTO inputDTO) {
        // Configura conta fixa se informada
        if (inputDTO.contaFixaId() != null) {
            ContaFixa contaFixa = contaFixaRepository.findById(inputDTO.contaFixaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Conta fixa não encontrada com ID: " + inputDTO.contaFixaId()));
            transacao.setContaFixa(contaFixa);
        }
    }
    
    /**
     * Configura uma transação de transferência de saída
     */
    private void configurarTransferenciaSaida(Transacao transacao, TransacaoInputDTO inputDTO) {
        // Busca a conta de destino
        Conta contaDestino = contaRepository.findById(inputDTO.contaDestinoId())
                .orElseThrow(() -> new ResourceNotFoundException("Conta de destino não encontrada com ID: " + inputDTO.contaDestinoId()));
        transacao.setContaDestino(contaDestino);
    }
    
    /**
     * Configura uma transação de transferência de entrada
     */
    private void configurarTransferenciaEntrada(Transacao transacao, TransacaoInputDTO inputDTO) {
        // Busca a conta de origem
        if (inputDTO.contaDestinoId() != null) {
            Conta contaOrigem = contaRepository.findById(inputDTO.contaDestinoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Conta de origem não encontrada com ID: " + inputDTO.contaDestinoId()));
            transacao.setContaDestino(contaOrigem);
        }
        
        // Esta transação é apenas para registro
        transacao.setSistema(true);
    }
    
    /**
     * Configura uma transação de pagamento de fatura
     */
    private void configurarPagamentoFatura(Transacao transacao, TransacaoInputDTO inputDTO) {
        // Busca a fatura
        Fatura fatura = faturaRepository.findById(inputDTO.faturaId())
                .orElseThrow(() -> new ResourceNotFoundException("Fatura não encontrada com ID: " + inputDTO.faturaId()));
        
        transacao.setFatura(fatura);
    }
    
    
    @Transactional
    public Transacao salvarTransacao(Transacao transacao) {
        return transacaoRepository.save(transacao);
    }

}