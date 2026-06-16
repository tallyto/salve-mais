package br.com.salvemais.web.api.mappers;

import br.com.salvemais.web.api.dto.AnexoDTO;
import br.com.salvemais.web.api.dto.UrlDownloadDTO;
import br.com.salvemais.domain.entities.Anexo;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface AnexoMapper {

    @Mapping(target = "contaFixaId", source = ".", qualifiedByName = "getContaFixaId")
    AnexoDTO toDTO(Anexo anexo);

    @Named("getContaFixaId")
    default Long getContaFixaId(Anexo anexo) {
        return anexo.getContaFixa() != null ? anexo.getContaFixa().getId() : null;
    }

    default UrlDownloadDTO toUrlDownloadDTO(Anexo anexo, String url) {
        return new UrlDownloadDTO(url, anexo.getNome(), anexo.getTipo());
    }
}
