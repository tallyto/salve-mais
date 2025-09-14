package com.tallyto.gestorfinanceiro.core.application.services;

import com.tallyto.gestorfinanceiro.api.dto.ContaFixaRecorrenteDTO;
import com.tallyto.gestorfinanceiro.core.domain.entities.Anexo;
import com.tallyto.gestorfinanceiro.core.domain.entities.ContaFixa;
import com.tallyto.gestorfinanceiro.core.infra.repositories.ContaFixaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class ContaFixaService {

    @Autowired
    private ContaFixaRepository contaFixaRepository;

    @Autowired
    private ContaService contaService;

    @Autowired
    private CategoriaService categoriaService;
    
    @Autowired
    private AnexoServiceInterface anexoService;
    
    @Autowired
    private TransacaoService transacaoService;

    @org.springframework.transaction.annotation.Transactional
    public ContaFixa salvarContaFixa(ContaFixa contaFixa) {
        var conta = contaService.getOne(contaFixa.getConta().getId());
        if (conta == null) {
            throw new IllegalArgumentException("Conta não encontrada");
        }
        
        // Verifica se é uma conta nova ou atualização
        boolean isNew = contaFixa.getId() == null;
        ContaFixa contaFixaExistente = null;
        boolean mudouParaPago = false;
        
        // Se for uma atualização, verifica se mudou de não pago para pago
        if (!isNew) {
            contaFixaExistente = contaFixaRepository.findById(contaFixa.getId()).orElse(null);
            if (contaFixaExistente != null) {
                mudouParaPago = !contaFixaExistente.isPago() && contaFixa.isPago();
            }
        } else {
            // Conta nova sendo marcada como paga
            mudouParaPago = contaFixa.isPago();
        }

        // Se a conta fixa foi marcada como paga, debita o valor da conta e cria a transação
        if (mudouParaPago) {
            // Verifica se a conta tem saldo suficiente
            if (conta.getSaldo().compareTo(contaFixa.getValor()) < 0) {
                throw new IllegalArgumentException("Saldo insuficiente na conta para pagar esta conta fixa");
            }
            
            // Atualiza o saldo da conta
            conta.setSaldo(conta.getSaldo().subtract(contaFixa.getValor()));
            
            // Salva a conta fixa primeiro para obter o ID (se for nova)
            ContaFixa contaFixaSalva = contaFixaRepository.save(contaFixa);
            
            // Cria a transação associada ao pagamento
            var transacaoDTO = new com.tallyto.gestorfinanceiro.api.dto.TransacaoInputDTO(
                com.tallyto.gestorfinanceiro.core.domain.enums.TipoTransacao.DEBITO,
                contaFixa.getValor(),
                "Pagamento: " + contaFixa.getNome(),
                contaFixa.getConta().getId(),
                null, // contaDestinoId
                null, // faturaId
                contaFixa.getCategoria() != null ? contaFixa.getCategoria().getId() : null,
                null, // proventoId
                contaFixaSalva.getId(), // contaFixaId
                "Transação gerada automaticamente para o pagamento da conta fixa #" + contaFixaSalva.getId()
            );
            
            // Cria a transação sem atualizar o saldo (já atualizamos acima)
            transacaoService.criarTransacaoSemAtualizarSaldo(transacaoDTO);
            
            return contaFixaSalva;
        } else {
            // Se não mudou para pago, apenas salva a conta fixa
            return contaFixaRepository.save(contaFixa);
        }
    }

    public BigDecimal calcularTotalContasFixasNaoPagas() {
        LocalDate hoje = LocalDate.now();
        return contaFixaRepository.calcularTotalContasFixasNaoPagas(hoje);
    }

    public List<ContaFixa> listarContaFixaPorCategoria(Long categoriaId) {
        return contaFixaRepository.findByCategoria_Id(categoriaId);
    }

    public List<ContaFixa> listarContasFixasVencidasNaoPagas() {
        LocalDate hoje = LocalDate.now();
        return contaFixaRepository.findByVencimentoBeforeAndPagoIsFalse(hoje);
    }

    public List<ContaFixa> listarContaFixaPorPeriodo(LocalDate dataInicio, LocalDate dataFim) {
        return contaFixaRepository.findByVencimentoBetween(dataInicio, dataFim);
    }

    // Outros métodos relacionados a contas fixas

    public Page<ContaFixa> listarTodasContasFixas(Pageable pageable) {
        return contaFixaRepository.findAll(pageable);
    }

    public Page<ContaFixa> listarContasFixasPorMesEAno(Pageable pageable, Integer mes, Integer ano) {
        return contaFixaRepository.findByVencimentoMesEAno(pageable, mes, ano);
    }

    public ContaFixa buscarContaFixaPorId(Long id) {
        return contaFixaRepository.findById(id).orElse(null);
    }

    public void deletarContaFixa(Long id) {
        contaFixaRepository.deleteById(id);
    }

    /**
     * Cria múltiplas contas fixas com base em uma recorrência
     * @param dto Dados da conta fixa recorrente
     * @return Lista das contas fixas criadas
     */
    public List<ContaFixa> criarContasFixasRecorrentes(ContaFixaRecorrenteDTO dto) {
        // Validar se a conta existe
        var conta = contaService.getOne(dto.contaId());
        if (conta == null) {
            throw new IllegalArgumentException("Conta não encontrada");
        }

        // Validar se a categoria existe
        var categoria = categoriaService.buscaCategoriaPorId(dto.categoriaId());
        if (categoria == null) {
            throw new IllegalArgumentException("Categoria não encontrada");
        }

        List<ContaFixa> contasFixasCriadas = new ArrayList<>();
        LocalDate dataVencimento = dto.dataInicio();

        for (int i = 1; i <= dto.numeroParcelas(); i++) {
            ContaFixa contaFixa = new ContaFixa();
            
            // Define o nome com numeração das parcelas
            String nomeComParcela = String.format("%s (%d/%d)", 
                dto.nome(), i, dto.numeroParcelas());
            
            contaFixa.setNome(nomeComParcela);
            contaFixa.setConta(conta);
            contaFixa.setCategoria(categoria);
            contaFixa.setVencimento(dataVencimento);
            contaFixa.setValor(dto.valor());
            contaFixa.setPago(false);

            ContaFixa contaSalva = contaFixaRepository.save(contaFixa);
            contasFixasCriadas.add(contaSalva);

            // Calcular próxima data de vencimento baseada no tipo de recorrência
            dataVencimento = dataVencimento.plusMonths(dto.tipoRecorrencia().getMeses());
        }

        return contasFixasCriadas;
    }
    
    /**
     * Adiciona um comprovante à conta fixa
     * @param contaFixaId ID da conta fixa
     * @param arquivo Arquivo do comprovante
     * @return Objeto Anexo criado
     * @throws IOException em caso de erro ao processar o arquivo
     */
    public Anexo adicionarComprovante(Long contaFixaId, MultipartFile arquivo) throws IOException {
        ContaFixa contaFixa = buscarContaFixaPorId(contaFixaId);
        if (contaFixa == null) {
            throw new IllegalArgumentException("Conta fixa não encontrada");
        }
        
        return anexoService.uploadAnexo(arquivo, contaFixa);
    }
    
    /**
     * Lista todos os comprovantes de uma conta fixa
     * @param contaFixaId ID da conta fixa
     * @return Lista de anexos
     */
    public List<Anexo> listarComprovantes(Long contaFixaId) {
        ContaFixa contaFixa = buscarContaFixaPorId(contaFixaId);
        if (contaFixa == null) {
            throw new IllegalArgumentException("Conta fixa não encontrada");
        }
        
        return anexoService.listarAnexosPorContaFixa(contaFixaId);
    }
    
    /**
     * Marca uma conta fixa como paga e cria a transação correspondente
     * @param contaFixaId ID da conta fixa a ser paga
     * @param observacoes Observações opcionais sobre o pagamento
     * @return A conta fixa atualizada
     */
    @org.springframework.transaction.annotation.Transactional
    public ContaFixa pagarContaFixa(Long contaFixaId, String observacoes) {
        ContaFixa contaFixa = buscarContaFixaPorId(contaFixaId);
        if (contaFixa == null) {
            throw new IllegalArgumentException("Conta fixa não encontrada");
        }
        
        if (contaFixa.isPago()) {
            throw new IllegalArgumentException("Esta conta fixa já está paga");
        }
        
        // Busca a conta
        var conta = contaService.getOne(contaFixa.getConta().getId());
        if (conta == null) {
            throw new IllegalArgumentException("Conta não encontrada");
        }
        
        // Verifica saldo
        if (conta.getSaldo().compareTo(contaFixa.getValor()) < 0) {
            throw new IllegalArgumentException("Saldo insuficiente na conta para pagar esta conta fixa");
        }
        
        // Atualiza o saldo da conta
        conta.setSaldo(conta.getSaldo().subtract(contaFixa.getValor()));
        
        // Marca a conta fixa como paga
        contaFixa.setPago(true);
        ContaFixa contaFixaSalva = contaFixaRepository.save(contaFixa);
        
        // Cria a transação associada ao pagamento
        var transacaoDTO = new com.tallyto.gestorfinanceiro.api.dto.TransacaoInputDTO(
            com.tallyto.gestorfinanceiro.core.domain.enums.TipoTransacao.DEBITO,
            contaFixa.getValor(),
            "Pagamento: " + contaFixa.getNome(),
            contaFixa.getConta().getId(),
            null, // contaDestinoId
            null, // faturaId
            contaFixa.getCategoria() != null ? contaFixa.getCategoria().getId() : null,
            null, // proventoId
            contaFixaSalva.getId(), // contaFixaId
            observacoes != null ? observacoes : "Pagamento da conta fixa #" + contaFixaSalva.getId()
        );
        
        // Cria a transação sem atualizar o saldo (já atualizamos acima)
        transacaoService.criarTransacaoSemAtualizarSaldo(transacaoDTO);
        
        return contaFixaSalva;
    }
    
    /**
     * Gera URL para download do comprovante
     * @param anexoId ID do anexo
     * @return URL assinada para download
     */
    public String gerarUrlDownloadComprovante(Long anexoId) {
        return anexoService.gerarUrlDownload(anexoId);
    }
    
    /**
     * Remove um comprovante
     * @param anexoId ID do anexo
     */
    public void removerComprovante(Long anexoId) {
        anexoService.excluirAnexo(anexoId);
    }
}
