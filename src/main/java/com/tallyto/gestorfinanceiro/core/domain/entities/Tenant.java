package com.tallyto.gestorfinanceiro.core.domain.entities;



import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "tenants", schema = "public")
@Getter
@Setter
public class Tenant extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "domain", unique = true, nullable = false)
    private String domain;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "address")
    private String address;
    
    @Column(name = "active", nullable = false)
    private Boolean active = false;
    
    @Column(name = "confirmation_token")
    private String confirmationToken;

}
