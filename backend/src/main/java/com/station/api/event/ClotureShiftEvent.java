package com.station.api.event;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class ClotureShiftEvent {

    private final Long clotureId;
    private final LocalDate dateCloture;
    private final String nomEquipe;
    private final String chefEquipe;
    private final BigDecimal caTheoriqueTotal;
    private final BigDecimal totalReelEncaisse;
    private final BigDecimal ecartCaisse;
    private final LocalDateTime horodatage;
}
