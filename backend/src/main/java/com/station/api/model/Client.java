package com.station.api.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "clients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "raison_sociale", nullable = false, length = 150)
    private String raisonSociale;

    @Column(length = 50)
    private String telephone;

    @Column(length = 100)
    private String email;

    @Column(name = "plafond_credit", precision = 12, scale = 3)
    private BigDecimal plafondCredit;

    @Column(name = "solde_du", precision = 12, scale = 3)
    private BigDecimal soldeDu;
}
