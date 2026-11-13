package com.station.api.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "releves_index")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReleveIndex {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date_releve", nullable = false)
    private LocalDate dateReleve;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "pistolet_id", nullable = false)
    private Pistolet pistolet;

    @Column(name = "index_depart", nullable = false, precision = 12, scale = 2)
    private BigDecimal indexDepart;

    @Column(name = "index_fin", nullable = false, precision = 12, scale = 2)
    private BigDecimal indexFin;

    @Column(name = "volume_vendu", nullable = false, precision = 12, scale = 2)
    private BigDecimal volumeVendu;

    @Column(name = "prix_unitaire", nullable = false, precision = 12, scale = 3)
    private BigDecimal prixUnitaire;

    @Column(name = "montant_total", nullable = false, precision = 12, scale = 3)
    private BigDecimal montantTotal;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}
