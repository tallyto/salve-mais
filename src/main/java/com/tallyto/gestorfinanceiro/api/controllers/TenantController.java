package com.tallyto.gestorfinanceiro.api.controllers;


import java.util.List;
import java.util.UUID;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tallyto.gestorfinanceiro.api.dto.TenantDTO;
import com.tallyto.gestorfinanceiro.api.dto.TenantResponseDTO;
import com.tallyto.gestorfinanceiro.core.domain.entities.Tenant;
import com.tallyto.gestorfinanceiro.core.domain.exceptions.ResourceNotFoundException;
import com.tallyto.gestorfinanceiro.core.infra.repositories.TenantRepository;
import com.tallyto.gestorfinanceiro.mappers.TenantMapper;
import com.tallyto.gestorfinanceiro.util.Utils;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Tenants", description = "API de Tenants")
@RestController
@RequestMapping("/api/tenants")
public class TenantController {

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private TenantMapper tenantMapper;

    @GetMapping
    public List<TenantResponseDTO> getAllTenants() {
        var tenants = tenantRepository.findAll();
        return tenantMapper.toListDTO(tenants);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TenantResponseDTO> getTenantById(@PathVariable UUID id) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with id " + id));
        return ResponseEntity.ok(tenantMapper.toDTO(tenant));
    }

    @PostMapping
    public TenantResponseDTO createTenant(@Valid @RequestBody TenantDTO tenantDTO) {
        var tenant = tenantMapper.toEntity(tenantDTO);


        tenant = tenantRepository.save(tenant);

        return tenantMapper.toDTO(tenant);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TenantResponseDTO> updateTenant(@PathVariable UUID id, @RequestBody TenantDTO tenantDTO) {
        var tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with id " + id));

        BeanUtils.copyProperties(tenantDTO, tenant, Utils.getNullPropertyNames(tenantDTO));


        var updatedTenant = tenantRepository.save(tenant);

        return ResponseEntity.ok(tenantMapper.toDTO(updatedTenant));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTenant(@PathVariable UUID id) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with id " + id));
        tenantRepository.delete(tenant);
        return ResponseEntity.noContent().build();
    }
}
