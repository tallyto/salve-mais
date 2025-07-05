package com.tallyto.gestorfinanceiro.mappers;



import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import com.tallyto.gestorfinanceiro.api.dto.TenantDTO;
import com.tallyto.gestorfinanceiro.api.dto.TenantResponseDTO;
import com.tallyto.gestorfinanceiro.core.domain.entities.Tenant;



@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TenantMapper {
    TenantMapper INSTANCE = Mappers.getMapper(TenantMapper.class);

    Tenant toEntity(TenantDTO tenantDTO);

    TenantResponseDTO toDTO(Tenant tenant);

    List<TenantResponseDTO> toListDTO(List<Tenant> tenants);
}
