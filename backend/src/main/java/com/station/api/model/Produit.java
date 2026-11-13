package com.station.api.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "produits")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Produit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(name = "type_produit", nullable = false, length = 30)
    private String typeProduit; // CARBURANT, HUILLE, ACCESSOR, BOISSON

    @Column(name = "prix_unitaire", nullable = false, precision = 12, scale = 3)
    private BigDecimal prixUnitaire;

    @Column(name = "stock_kiosque", precision = 12, scale = 2)
    private BigDecimal stockKiosque;

    @Column(name = "seuil_alerte", precision = 12, scale = 2)
    private BigDecimal seuilAlerte;
}
