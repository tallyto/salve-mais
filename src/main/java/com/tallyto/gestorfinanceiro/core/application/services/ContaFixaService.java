package com.tallyto.gestorfinanceiro.core.application.services;

import com.tallyto.gestorfinanceiro.api.dto.ContaFixaRecorrenteDTO;
import com.tallyto.gestorfinanceiro.core.domain.entities.ContaFixa;
import com.tallyto.gestorfinanceiro.core.infra.repositories.ContaFixaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

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

    public ContaFixa salvarContaFixa(ContaFixa contaFixa) {
        var conta = contaService.getOne(contaFixa.getConta().getId());
        if (conta == null) {
            throw new IllegalArgumentException("Conta não encontrada");
        }

        if (contaFixa.isPago()) {
            conta.setSaldo(conta.getSaldo().subtract(contaFixa.getValor()));
        }

        return contaFixaRepository.save(contaFixa);
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

    // Outros métodos relacionados a contas fixas

    public Page<ContaFixa> listarTodasContasFixas(Pageable pageable) {
        return contaFixaRepository.findAll(pageable);
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

    // Outros métodos, se necessário
}
