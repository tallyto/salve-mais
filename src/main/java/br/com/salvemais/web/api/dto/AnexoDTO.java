package br.com.salvemais.web.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Anexo de comprovante")
public record AnexoDTO(
    @Schema(description = "ID do anexo")
    Long id,
    @Schema(description = "Nome do arquivo")
    String nome,
    @Schema(description = "Tipo MIME do arquivo")
    String tipo,
    @Schema(description = "Data do upload")
    LocalDateTime dataUpload,
    @Schema(description = "ID da conta fixa associada")
    Long contaFixaId
) {}
