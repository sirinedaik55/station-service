package com.station.api.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "reglements_fournisseur")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReglementFournisseur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date_reglement", nullable = false)
    private LocalDate dateReglement;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "fournisseur_id", nullable = false)
    private Fournisseur fournisseur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facture_id")
    private FactureFournisseur facture;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal montant;

    @Column(name = "mode_reglement", nullable = false, length = 50)
    private String modeReglement; // VIREMENT, CHÈQUE, ESPÈCES

    @Column(length = 100)
    private String reference;
}
