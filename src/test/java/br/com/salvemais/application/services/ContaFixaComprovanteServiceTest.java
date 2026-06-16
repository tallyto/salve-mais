package br.com.salvemais.application.services;

import br.com.salvemais.domain.entities.Anexo;
import br.com.salvemais.domain.entities.ContaFixa;
import br.com.salvemais.infrastructure.repositories.ContaFixaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContaFixaComprovanteServiceTest {

    @Mock
    private ContaFixaRepository contaFixaRepository;

    @Mock
    private AnexoServiceInterface anexoService;

    @InjectMocks
    private ContaFixaComprovanteService contaFixaComprovanteService;

    @Test
    void deveAdicionarComprovanteQuandoContaFixaExiste() throws IOException {
        ContaFixa contaFixa = new ContaFixa();
        contaFixa.setId(1L);
        MultipartFile arquivo = new MockMultipartFile("arquivo", "comprovante.pdf", "application/pdf", "conteudo".getBytes());
        Anexo anexo = new Anexo();
        anexo.setId(10L);

        when(contaFixaRepository.findById(1L)).thenReturn(java.util.Optional.of(contaFixa));
        when(anexoService.uploadAnexo(arquivo, contaFixa)).thenReturn(anexo);

        Anexo resultado = contaFixaComprovanteService.adicionarComprovante(1L, arquivo);

        assertSame(anexo, resultado);
        verify(anexoService).uploadAnexo(arquivo, contaFixa);
    }

    @Test
    void deveLancarExcecaoQuandoContaFixaNaoExiste() {
        MultipartFile arquivo = new MockMultipartFile("arquivo", "comprovante.pdf", "application/pdf", "conteudo".getBytes());
        when(contaFixaRepository.findById(1L)).thenReturn(java.util.Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> contaFixaComprovanteService.adicionarComprovante(1L, arquivo)
        );

        assertEquals("Conta fixa não encontrada", exception.getMessage());
        verifyNoInteractions(anexoService);
    }

    @Test
    void deveListarComprovantesQuandoContaFixaExiste() {
        ContaFixa contaFixa = new ContaFixa();
        contaFixa.setId(1L);
        Anexo anexo = new Anexo();
        anexo.setId(10L);

        when(contaFixaRepository.findById(1L)).thenReturn(java.util.Optional.of(contaFixa));
        when(anexoService.listarAnexosPorContaFixa(1L)).thenReturn(List.of(anexo));

        List<Anexo> resultado = contaFixaComprovanteService.listarComprovantes(1L);

        assertEquals(1, resultado.size());
        assertSame(anexo, resultado.getFirst());
    }

    @Test
    void deveGerarUrlDownloadComprovante() {
        when(anexoService.gerarUrlDownload(5L)).thenReturn("https://download");

        assertEquals("https://download", contaFixaComprovanteService.gerarUrlDownloadComprovante(5L));
    }

    @Test
    void deveRemoverComprovante() {
        doNothing().when(anexoService).excluirAnexo(7L);

        contaFixaComprovanteService.removerComprovante(7L);

        verify(anexoService).excluirAnexo(7L);
    }
}
