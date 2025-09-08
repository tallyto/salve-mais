package com.tallyto.gestorfinanceiro.core.application.services;

import com.tallyto.gestorfinanceiro.core.domain.entities.Anexo;
import com.tallyto.gestorfinanceiro.core.domain.entities.ContaFixa;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * Interface para o serviço de anexos
 */
public interface AnexoServiceInterface {
    
    /**
     * Faz o upload de um arquivo e salva os metadados no banco de dados
     */
    Anexo uploadAnexo(MultipartFile file, ContaFixa contaFixa) throws IOException;
    
    /**
     * Gera uma URL para download do anexo
     */
    String gerarUrlDownload(Long anexoId);
    
    /**
     * Exclui um anexo
     */
    void excluirAnexo(Long anexoId);
    
    /**
     * Lista todos os anexos de uma conta fixa
     */
    List<Anexo> listarAnexosPorContaFixa(Long contaFixaId);
    
    /**
     * Lista todos os anexos (comprovantes) do sistema
     */
    List<Anexo> listarTodosAnexos();
}
