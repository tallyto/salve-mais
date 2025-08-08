package com.tallyto.gestorfinanceiro.api.controllers;

import com.tallyto.gestorfinanceiro.api.dto.AnexoDTO;
import com.tallyto.gestorfinanceiro.api.dto.UrlDownloadDTO;
import com.tallyto.gestorfinanceiro.api.mappers.AnexoMapper;
import com.tallyto.gestorfinanceiro.core.application.services.AnexoService;
import com.tallyto.gestorfinanceiro.core.application.services.ContaFixaService;
import com.tallyto.gestorfinanceiro.core.domain.entities.Anexo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/contas-fixas/{contaFixaId}/comprovantes")
@Tag(name = "Comprovantes", description = "Endpoints para gerenciamento de comprovantes de contas fixas")
public class ComprovanteController {

    @Autowired
    private ContaFixaService contaFixaService;
    
    @Autowired
    private AnexoService anexoService;
    
    @Autowired
    private AnexoMapper anexoMapper;
    
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Anexar comprovante a uma conta fixa")
    public ResponseEntity<AnexoDTO> anexarComprovante(
            @PathVariable Long contaFixaId,
            @RequestParam("arquivo") MultipartFile arquivo) throws IOException {
            
        Anexo anexo = contaFixaService.adicionarComprovante(contaFixaId, arquivo);
        return new ResponseEntity<>(anexoMapper.toDTO(anexo), HttpStatus.CREATED);
    }
    
    @GetMapping
    @Operation(summary = "Listar comprovantes de uma conta fixa")
    public ResponseEntity<List<AnexoDTO>> listarComprovantes(@PathVariable Long contaFixaId) {
        List<Anexo> anexos = contaFixaService.listarComprovantes(contaFixaId);
        List<AnexoDTO> anexoDTOs = anexos.stream()
                .map(anexoMapper::toDTO)
                .collect(Collectors.toList());
                
        return ResponseEntity.ok(anexoDTOs);
    }
    
    @GetMapping("/{anexoId}/download")
    @Operation(summary = "Gerar URL para download de comprovante")
    public ResponseEntity<UrlDownloadDTO> gerarUrlDownload(
            @PathVariable Long contaFixaId,
            @PathVariable Long anexoId) {
            
        // Verificar se o anexo pertence à conta fixa especificada
        List<Anexo> anexos = contaFixaService.listarComprovantes(contaFixaId);
        Anexo anexo = anexos.stream()
                .filter(a -> a.getId().equals(anexoId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Anexo não encontrado para esta conta fixa"));
                
        String url = contaFixaService.gerarUrlDownloadComprovante(anexoId);
        return ResponseEntity.ok(anexoMapper.toUrlDownloadDTO(anexo, url));
    }
    
    @DeleteMapping("/{anexoId}")
    @Operation(summary = "Remover comprovante")
    public ResponseEntity<Void> removerComprovante(
            @PathVariable Long contaFixaId,
            @PathVariable Long anexoId) {
            
        // Verificar se o anexo pertence à conta fixa especificada
        List<Anexo> anexos = contaFixaService.listarComprovantes(contaFixaId);
        boolean pertenceAContaFixa = anexos.stream()
                .anyMatch(a -> a.getId().equals(anexoId));
                
        if (!pertenceAContaFixa) {
            return ResponseEntity.notFound().build();
        }
        
        contaFixaService.removerComprovante(anexoId);
        return ResponseEntity.noContent().build();
    }
}
