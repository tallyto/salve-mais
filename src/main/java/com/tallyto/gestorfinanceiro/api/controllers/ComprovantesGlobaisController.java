package com.tallyto.gestorfinanceiro.api.controllers;

import com.tallyto.gestorfinanceiro.api.dto.AnexoDTO;
import com.tallyto.gestorfinanceiro.api.dto.UrlDownloadDTO;
import com.tallyto.gestorfinanceiro.api.mappers.AnexoMapper;
import com.tallyto.gestorfinanceiro.core.application.services.AnexoService;
import com.tallyto.gestorfinanceiro.core.domain.entities.Anexo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/comprovantes")
@Tag(name = "Comprovantes Globais", description = "Endpoints para gerenciamento global de comprovantes")
public class ComprovantesGlobaisController {

    @Autowired
    private AnexoService anexoService;
    
    @Autowired
    private AnexoMapper anexoMapper;
    
    @GetMapping
    @Operation(summary = "Listar todos os comprovantes do sistema")
    public ResponseEntity<List<AnexoDTO>> listarTodosComprovantes() {
        List<Anexo> anexos = anexoService.listarTodosAnexos();
        List<AnexoDTO> anexoDTOs = anexos.stream()
                .map(anexoMapper::toDTO)
                .collect(Collectors.toList());
                
        return ResponseEntity.ok(anexoDTOs);
    }
    
    @GetMapping("/{anexoId}/download")
    @Operation(summary = "Gerar URL para download de comprovante")
    public ResponseEntity<UrlDownloadDTO> gerarUrlDownload(@PathVariable Long anexoId) {
        try {
            String url = anexoService.gerarUrlDownload(anexoId);
            
            // Vamos obter o anexo do repositório para criar o DTO
            Anexo anexo = anexoService.listarTodosAnexos().stream()
                    .filter(a -> a.getId().equals(anexoId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Anexo não encontrado"));
            
            return ResponseEntity.ok(anexoMapper.toUrlDownloadDTO(anexo, url));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/{anexoId}")
    @Operation(summary = "Remover comprovante")
    public ResponseEntity<Void> removerComprovante(@PathVariable Long anexoId) {
        try {
            anexoService.excluirAnexo(anexoId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
