package com.tallyto.gestorfinanceiro.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.tallyto.gestorfinanceiro.api.dto.TenantDTO;
import com.tallyto.gestorfinanceiro.api.dto.TenantResponseDTO;
import com.tallyto.gestorfinanceiro.core.domain.entities.Tenant;

@Mapper(componentModel = "spring")
public interface TenantMapper {

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "confirmationToken", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "customSmtpPassword", ignore = true) // Não permitir atualização via DTO simples
    @Mapping(target = "subscriptionStartDate", ignore = true) // Gerenciado internamente
    @Mapping(target = "subscriptionEndDate", ignore = true) // Gerenciado internamente
    @Mapping(target = "createUserToken", ignore = true) // Gerenciado internamente
    @Mapping(target = "createUserTokenExpiry", ignore = true) // Gerenciado internamente
    Tenant toEntity(TenantDTO tenantDTO);

    TenantResponseDTO toDTO(Tenant tenant);

    List<TenantResponseDTO> toListDTO(List<Tenant> tenants);
}
