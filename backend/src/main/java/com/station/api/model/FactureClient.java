package com.station.api.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "factures_client")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FactureClient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_facture", nullable = false, unique = true, length = 50)
    private String numeroFacture;

    @Column(name = "date_facture", nullable = false)
    private LocalDate dateFacture;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Column(name = "montant_ht", nullable = false, precision = 12, scale = 3)
    private BigDecimal montantHt;

    @Column(name = "timbre_fiscal", precision = 8, scale = 3)
    private BigDecimal timbreFiscal;

    @Column(name = "montant_ttc", nullable = false, precision = 12, scale = 3)
    private BigDecimal montantTtc;

    @Column(name = "montant_paye", precision = 12, scale = 3)
    private BigDecimal montantPaye;

    @Column(nullable = false, length = 30)
    private String statut; // IMPAYEE, PARTIELLE, PAYEE

    @Column(name = "date_echeance")
    private LocalDate dateEcheance;
}
