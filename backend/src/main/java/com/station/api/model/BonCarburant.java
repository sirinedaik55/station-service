package com.station.api.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "bons_carburant")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BonCarburant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_bon", nullable = false, length = 50)
    private String numeroBon;

    @Column(name = "date_emission", nullable = false)
    private LocalDate dateEmission;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "produit_id", nullable = false)
    private Produit produit;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal volume;

    @Column(name = "prix_unitaire", nullable = false, precision = 12, scale = 3)
    private BigDecimal prixUnitaire;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal montant;

    @Column(name = "mode_reglement", length = 50)
    private String modeReglement; // BON_VALEUR, CARTE_AGELIS, ESPECE, CHÈQUE

    @Column(nullable = false, length = 30)
    private String statut; // NON_FACTURE, FACTURE

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facture_id")
    private FactureClient facture;
}
