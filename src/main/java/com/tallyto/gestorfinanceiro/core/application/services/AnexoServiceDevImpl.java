package com.tallyto.gestorfinanceiro.core.application.services;

import com.tallyto.gestorfinanceiro.context.TenantContext;
import com.tallyto.gestorfinanceiro.core.domain.entities.Anexo;
import com.tallyto.gestorfinanceiro.core.domain.entities.ContaFixa;
import com.tallyto.gestorfinanceiro.core.infra.repositories.AnexoRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Implementação de desenvolvimento do serviço de anexos
 * Esta implementação armazena os arquivos diretamente no banco de dados
 * em vez de usar o AWS S3.
 */
@Service
@Profile("dev")
public class AnexoServiceDevImpl implements AnexoServiceInterface {

    @Autowired
    private AnexoRepository anexoRepository;
    
    @PersistenceContext
    private EntityManager entityManager;
    
    /**
     * Faz o upload de um arquivo para o banco de dados
     */
    @Override
    public Anexo uploadAnexo(MultipartFile file, ContaFixa contaFixa) throws IOException {
        // Validações
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Arquivo não pode estar vazio");
        }
        
        String originalFilename = file.getOriginalFilename();
        String contentType = file.getContentType();
        
        // Pegar o nome do tenant atual
        String tenantName = TenantContext.getCurrentTenant();
        
        // Gerar uma chave única 
        String chaveDb = "comprovantes/" + tenantName + "/" + UUID.randomUUID() + "-" + originalFilename;
        
        // Criar o anexo no banco de dados com o conteúdo
        Anexo anexo = new Anexo();
        anexo.setNome(originalFilename);
        anexo.setTipo(contentType);
        anexo.setChaveS3(chaveDb); // Usamos o mesmo campo para manter compatibilidade
        anexo.setContaFixa(contaFixa);
        anexo.setDados(file.getBytes()); // Campo para armazenar o conteúdo
        
        contaFixa.adicionarAnexo(anexo);
        
        return anexoRepository.save(anexo);
    }
    
    /**
     * Gera uma URL para download do anexo
     * No ambiente de desenvolvimento, isso retorna apenas um URL para o endpoint local
     */
    @Override
    public String gerarUrlDownload(Long anexoId) {
        Anexo anexo = anexoRepository.findById(anexoId)
                .orElseThrow(() -> new IllegalArgumentException("Anexo não encontrado"));
        
        return "/api/contas-fixas/" + anexo.getContaFixa().getId() + "/comprovantes/" + anexoId + "/conteudo";
    }
    
    /**
     * Exclui um anexo do banco de dados
     */
    @Override
    public void excluirAnexo(Long anexoId) {
        Anexo anexo = anexoRepository.findById(anexoId)
                .orElseThrow(() -> new IllegalArgumentException("Anexo não encontrado"));
        
        // Excluir do banco de dados
        anexoRepository.delete(anexo);
    }
    
    /**
     * Lista todos os anexos de uma conta fixa
     */
    @Override
    public List<Anexo> listarAnexosPorContaFixa(Long contaFixaId) {
        return anexoRepository.findByContaFixaId(contaFixaId);
    }
    
    /**
     * Recupera o conteúdo de um anexo diretamente do banco de dados
     * Este método é específico para a implementação de desenvolvimento
     */
    public byte[] getConteudoAnexo(Long anexoId) {
        Anexo anexo = anexoRepository.findById(anexoId)
                .orElseThrow(() -> new IllegalArgumentException("Anexo não encontrado"));
        
        return anexo.getDados();
    }
}
