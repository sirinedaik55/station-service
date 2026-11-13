package com.station.api.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "clotures_journee")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClotureJournee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date_cloture", nullable = false)
    private LocalDate dateCloture;

    @Column(name = "nom_equipe", length = 50)
    private String nomEquipe;

    @Column(name = "chef_equipe", length = 100)
    private String chefEquipe;

    @Column(name = "ca_theorique_piste", nullable = false, precision = 12, scale = 3)
    private BigDecimal caTheoriquePiste;

    @Column(name = "ca_kiosque", nullable = false, precision = 12, scale = 3)
    private BigDecimal caKiosque;

    @Column(name = "ca_lavage_vidange", precision = 12, scale = 3)
    private BigDecimal caLavageVidange;

    @Column(name = "ca_total_theorique", nullable = false, precision = 12, scale = 3)
    private BigDecimal caTotalTheorique;

    @Column(name = "montant_especes_physique", nullable = false, precision = 12, scale = 3)
    private BigDecimal montantEspecesPhysique;

    @Column(name = "total_bons_agil", precision = 12, scale = 3)
    private BigDecimal totalBonsAgil;

    @Column(name = "total_cartes_agilis", precision = 12, scale = 3)
    private BigDecimal totalCartesAgilis;

    @Column(name = "total_cheques", precision = 12, scale = 3)
    private BigDecimal totalCheques;

    @Column(name = "total_credits_b2b", precision = 12, scale = 3)
    private BigDecimal totalCreditsB2B;

    @Column(name = "total_depenses_piste", precision = 12, scale = 3)
    private BigDecimal totalDepensesPiste;

    @Column(name = "total_reel_encaisse", precision = 12, scale = 3)
    private BigDecimal totalReelEncaisse;

    @Column(name = "ecart_caisse", nullable = false, precision = 12, scale = 3)
    private BigDecimal ecartCaisse;

    @Column(nullable = false, length = 30)
    private String statut; // BROUILLON, CLOTURE

    @Column(columnDefinition = "TEXT")
    private String observations;
}
