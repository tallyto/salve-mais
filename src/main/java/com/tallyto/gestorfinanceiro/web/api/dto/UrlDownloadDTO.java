package com.tallyto.gestorfinanceiro.web.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "URL assinada para download de arquivo")
public record UrlDownloadDTO(
    @Schema(description = "URL de download")
    String url,
    @Schema(description = "Nome do arquivo")
    String nome,
    @Schema(description = "Tipo MIME do arquivo")
    String tipo
) {}
