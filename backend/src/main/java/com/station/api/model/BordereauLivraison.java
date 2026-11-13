package com.station.api.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "bordereaux_livraison")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BordereauLivraison {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_bordereau", nullable = false, length = 50)
    private String numeroBordereau;

    @Column(name = "date_livraison", nullable = false)
    private LocalDate dateLivraison;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "fournisseur_id", nullable = false)
    private Fournisseur fournisseur;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "citerne_id", nullable = false)
    private Citerne citerne;

    @Column(name = "volume_livre", nullable = false, precision = 12, scale = 2)
    private BigDecimal volumeLivre;

    @Column(name = "prix_unitaire", nullable = false, precision = 12, scale = 3)
    private BigDecimal prixUnitaire;

    @Column(name = "montant_total", nullable = false, precision = 12, scale = 3)
    private BigDecimal montantTotal;

    @Column(columnDefinition = "TEXT")
    private String observations;
}
