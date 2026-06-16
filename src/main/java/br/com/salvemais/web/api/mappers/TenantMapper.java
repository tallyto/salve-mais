package br.com.salvemais.web.api.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import br.com.salvemais.web.api.dto.TenantDTO;
import br.com.salvemais.web.api.dto.TenantResponseDTO;
import br.com.salvemais.domain.entities.Tenant;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TenantMapper {

    Tenant toEntity(TenantDTO tenantDTO);

    TenantResponseDTO toDTO(Tenant tenant);

    List<TenantResponseDTO> toListDTO(List<Tenant> tenants);
}
