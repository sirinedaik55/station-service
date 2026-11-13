package com.station.api.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LivraisonInputDto {
    private String numeroBordereau;
    private LocalDate dateLivraison;
    private Long fournisseurId;
    private Long citerneId;
    private BigDecimal volumeLivre;
    private BigDecimal prixUnitaire;
    private String observations;
}
