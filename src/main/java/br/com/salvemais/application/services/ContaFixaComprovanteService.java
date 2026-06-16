package br.com.salvemais.application.services;

import br.com.salvemais.domain.entities.Anexo;
import br.com.salvemais.domain.entities.ContaFixa;
import br.com.salvemais.infrastructure.repositories.ContaFixaRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class ContaFixaComprovanteService {

    private final ContaFixaRepository contaFixaRepository;
    private final AnexoServiceInterface anexoService;

    public ContaFixaComprovanteService(ContaFixaRepository contaFixaRepository,
                                       AnexoServiceInterface anexoService) {
        this.contaFixaRepository = contaFixaRepository;
        this.anexoService = anexoService;
    }

    public Anexo adicionarComprovante(Long contaFixaId, MultipartFile arquivo) throws IOException {
        ContaFixa contaFixa = buscarContaFixaPorId(contaFixaId);
        if (contaFixa == null) {
            throw new IllegalArgumentException("Conta fixa não encontrada");
        }

        return anexoService.uploadAnexo(arquivo, contaFixa);
    }

    public List<Anexo> listarComprovantes(Long contaFixaId) {
        ContaFixa contaFixa = buscarContaFixaPorId(contaFixaId);
        if (contaFixa == null) {
            throw new IllegalArgumentException("Conta fixa não encontrada");
        }

        return anexoService.listarAnexosPorContaFixa(contaFixaId);
    }

    public String gerarUrlDownloadComprovante(Long anexoId) {
        return anexoService.gerarUrlDownload(anexoId);
    }

    public void removerComprovante(Long anexoId) {
        anexoService.excluirAnexo(anexoId);
    }

    private ContaFixa buscarContaFixaPorId(Long id) {
        return contaFixaRepository.findById(id).orElse(null);
    }
}
