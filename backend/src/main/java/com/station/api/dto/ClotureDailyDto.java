package com.station.api.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClotureDailyDto {
    private LocalDate dateCloture;
    private String nomEquipe;
    private String chefEquipe;
    private BigDecimal caKiosque;
    private BigDecimal caLavageVidange;
    private BigDecimal montantEspecesPhysique;
    private BigDecimal totalBonsAgil;
    private BigDecimal totalCartesAgilis;
    private BigDecimal totalCheques;
    private BigDecimal totalCreditsB2B;
    private BigDecimal totalDepensesPiste;
    private String observations;
}
