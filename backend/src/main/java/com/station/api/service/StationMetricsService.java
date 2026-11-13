package com.station.api.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class StationMetricsService {

    private final Counter volumeVenduTotalCounter;
    private final Counter caTotalCounter;
    private final Counter alertesCuvesCounter;
    private final Counter ecartsCaisseCounter;

    public StationMetricsService(MeterRegistry registry) {
        this.volumeVenduTotalCounter = Counter.builder("station.ventes.litres.total")
            .description("Volume total de carburant distribué à la piste (Litres)")
            .tag("reseau", "agil_tunisie")
            .register(registry);

        this.caTotalCounter = Counter.builder("station.ca.recettes.total")
            .description("Chiffre d'affaires total consolidé (Dinars Tunisiens DT)")
            .tag("reseau", "agil_tunisie")
            .register(registry);

        this.alertesCuvesCounter = Counter.builder("station.alertes.cuves.total")
            .description("Nombre total d'alertes de stock bas ou de coulage anormal déclenchées")
            .tag("reseau", "agil_tunisie")
            .register(registry);

        this.ecartsCaisseCounter = Counter.builder("station.shifts.ecarts.total")
            .description("Nombre total de clôtures de shift présentant un écart financier")
            .tag("reseau", "agil_tunisie")
            .register(registry);
    }

    public void enregistrerDebitCarburant(double litres, double montantDt) {
        volumeVenduTotalCounter.increment(litres);
        caTotalCounter.increment(montantDt);
    }

    public void enregistrerAlerteCuve() {
        alertesCuvesCounter.increment();
    }

    public void enregistrerEcartShift(BigDecimal ecart) {
        if (ecart != null && ecart.compareTo(BigDecimal.ZERO) != 0) {
            ecartsCaisseCounter.increment();
        }
    }
}
