package com.station.api.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BonCarburantInputDto {
    private String numeroBon;
    private LocalDate dateEmission;
    private Long clientId;
    private Long produitId;
    private BigDecimal volume;
    private String modeReglement;
}
