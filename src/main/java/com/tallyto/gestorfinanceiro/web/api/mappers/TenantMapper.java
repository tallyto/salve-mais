package com.tallyto.gestorfinanceiro.web.api.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import com.tallyto.gestorfinanceiro.web.api.dto.TenantDTO;
import com.tallyto.gestorfinanceiro.web.api.dto.TenantResponseDTO;
import com.tallyto.gestorfinanceiro.domain.entities.Tenant;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TenantMapper {

    Tenant toEntity(TenantDTO tenantDTO);

    TenantResponseDTO toDTO(Tenant tenant);

    List<TenantResponseDTO> toListDTO(List<Tenant> tenants);
}
