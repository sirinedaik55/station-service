package com.station.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IoTTankTelemetryMessage implements Serializable {

    private Long citerneId;
    private String codeCiterne;
    private String nomProduit;
    private BigDecimal stockLitres;
    private BigDecimal hauteurPigeCm;
    private double temperatureCelsius;
    private double densiteHydrocarbure;
    private String statutSonde; // OK, ALERTE_NIVEAU_BAS, VARIATION_BRUSQUE
    private LocalDateTime horodatage;
}
