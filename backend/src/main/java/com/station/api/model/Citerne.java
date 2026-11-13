package com.station.api.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "citernes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Citerne {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 100)
    private String nom;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "produit_id", nullable = false)
    private Produit produit;

    @Column(name = "capacite_nominale", nullable = false, precision = 12, scale = 2)
    private BigDecimal capaciteNominale;

    @Column(name = "stock_actuel", nullable = false, precision = 12, scale = 2)
    private BigDecimal stockActuel;

    @Column(name = "seuil_alerte", nullable = false, precision = 12, scale = 2)
    private BigDecimal seuilAlerte;

    @Column(name = "hauteur_pige_cm", precision = 8, scale = 2)
    private BigDecimal hauteurPigeCm;

    @Column(name = "stock_physique", precision = 12, scale = 2)
    private BigDecimal stockPhysique;

    @Column(name = "ecart_coulage", precision = 12, scale = 2)
    private BigDecimal ecartCoulage;
}
