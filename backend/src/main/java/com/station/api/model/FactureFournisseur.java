package com.station.api.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "factures_fournisseur")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FactureFournisseur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_facture", nullable = false, unique = true, length = 50)
    private String numeroFacture;

    @Column(name = "date_facture", nullable = false)
    private LocalDate dateFacture;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "fournisseur_id", nullable = false)
    private Fournisseur fournisseur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bordereau_id")
    private BordereauLivraison bordereau;

    @Column(name = "montant_total", nullable = false, precision = 12, scale = 3)
    private BigDecimal montantTotal;

    @Column(name = "montant_paye", precision = 12, scale = 3)
    private BigDecimal montantPaye;

    @Column(nullable = false, length = 30)
    private String statut; // IMPAYEE, PARTIELLE, PAYEE

    @Column(name = "date_echeance")
    private LocalDate dateEcheance;
}
