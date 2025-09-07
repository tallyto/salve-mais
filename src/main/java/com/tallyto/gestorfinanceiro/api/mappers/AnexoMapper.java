package com.tallyto.gestorfinanceiro.api.mappers;

import com.tallyto.gestorfinanceiro.api.dto.AnexoDTO;
import com.tallyto.gestorfinanceiro.api.dto.UrlDownloadDTO;
import com.tallyto.gestorfinanceiro.core.domain.entities.Anexo;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AnexoMapper {

    @Mapping(target = "contaFixaId", source = "contaFixa.id")
    AnexoDTO toDTO(Anexo anexo);
    
    default UrlDownloadDTO toUrlDownloadDTO(Anexo anexo, String url) {
        return new UrlDownloadDTO(url, anexo.getNome(), anexo.getTipo());
    }
}
