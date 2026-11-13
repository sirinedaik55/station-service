package com.station.api.event;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class AlerteStockCuveEvent {

    public enum TypeAlerte {
        SEUIL_MINIMUM_ATTEINT,
        RUPTURE_IMMINENTE,
        COULAGE_ANORMAL
    }

    private final Long citerneId;
    private final String nomCiterne;
    private final String nomProduit;
    private final BigDecimal stockActuel;
    private final BigDecimal capaciteNominale;
    private final BigDecimal seuilAlerte;
    private final double pourcentageRemplissage;
    private final TypeAlerte typeAlerte;
    private final String message;
    private final LocalDateTime horodatage;
}
