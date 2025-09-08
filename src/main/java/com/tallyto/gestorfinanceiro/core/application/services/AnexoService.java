package com.tallyto.gestorfinanceiro.core.application.services;

import com.tallyto.gestorfinanceiro.context.TenantContext;
import com.tallyto.gestorfinanceiro.core.domain.entities.Anexo;
import com.tallyto.gestorfinanceiro.core.domain.entities.ContaFixa;
import com.tallyto.gestorfinanceiro.core.infra.repositories.AnexoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Service
public class AnexoService implements AnexoServiceInterface {

    @Autowired
    private AnexoRepository anexoRepository;

    @Autowired
    private S3Client s3Client;
    
    @Autowired
    private S3Presigner s3Presigner;
    
    @Value("${aws.s3.bucket}")
    private String bucketName;
    
    @Value("${aws.s3.presignedUrlExpirationMinutes}")
    private int presignedUrlExpirationMinutes;

    /**
     * Faz o upload de um arquivo para o S3 e salva os metadados no banco de dados
     */
    public Anexo uploadAnexo(MultipartFile file, ContaFixa contaFixa) throws IOException {
        // Validações
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Arquivo não pode estar vazio");
        }
        
        String originalFilename = file.getOriginalFilename();
        String contentType = file.getContentType();
        
        // Pegar o nome do tenant atual a partir do contexto
        String tenantName = TenantContext.getCurrentTenant();
        
        // Gerar uma chave única para o S3
        String chaveS3 = "comprovantes/" + tenantName + "/" + UUID.randomUUID() + "-" + originalFilename;
        
        // Fazer upload para o S3
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(chaveS3)
                .contentType(contentType)
                .build();
                
        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));
        
        // Criar o anexo no banco de dados (apenas os metadados)
        Anexo anexo = new Anexo();
        anexo.setNome(originalFilename);
        anexo.setTipo(contentType);
        anexo.setChaveS3(chaveS3);
        anexo.setContaFixa(contaFixa);
        
        contaFixa.adicionarAnexo(anexo);
        
        return anexoRepository.save(anexo);
    }
    
    /**
     * Gera uma URL pré-assinada para download do anexo
     */
    public String gerarUrlDownload(Long anexoId) {
        Anexo anexo = anexoRepository.findById(anexoId)
                .orElseThrow(() -> new IllegalArgumentException("Anexo não encontrado"));
        
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(anexo.getChaveS3())
                .build();
                
        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(presignedUrlExpirationMinutes))
                .getObjectRequest(getObjectRequest)
                .build();
                
        PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
        
        return presignedRequest.url().toString();
    }
    
    /**
     * Exclui um anexo do S3 e do banco de dados
     */
    public void excluirAnexo(Long anexoId) {
        Anexo anexo = anexoRepository.findById(anexoId)
                .orElseThrow(() -> new IllegalArgumentException("Anexo não encontrado"));
        
        // Excluir do S3
        s3Client.deleteObject(builder -> builder
                .bucket(bucketName)
                .key(anexo.getChaveS3())
                .build());
        
        // Excluir do banco de dados
        anexoRepository.delete(anexo);
    }
    
    /**
     * Lista todos os anexos de uma conta fixa
     */
    public List<Anexo> listarAnexosPorContaFixa(Long contaFixaId) {
        return anexoRepository.findByContaFixaId(contaFixaId);
    }
    
    /**
     * Lista todos os anexos (comprovantes) do sistema
     */
    @Override
    public List<Anexo> listarTodosAnexos() {
        return anexoRepository.findAll();
    }
}
