package com.station.api.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "fournisseurs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Fournisseur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nom_societe", nullable = false, length = 150)
    private String nomSociete;

    @Column(length = 50)
    private String telephone;

    @Column(columnDefinition = "TEXT")
    private String adresse;

    @Column(name = "solde_du", precision = 12, scale = 3)
    private java.math.BigDecimal soldeDu;
}
