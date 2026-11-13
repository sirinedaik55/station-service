package com.station.api.event;

import com.station.api.service.StationMetricsService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Component
@RequiredArgsConstructor
public class StationEventListener {

    private final StationMetricsService metricsService;
    private final List<AlerteStockCuveEvent> alertesRecentes = new CopyOnWriteArrayList<>();

    @Async
    @EventListener
    public void onAlerteStockCuve(AlerteStockCuveEvent event) {
        log.warn("⚠️ [ALERTE STATION AGIL] Type: {} | Cuve: {} ({}) | Stock: {} L / {} L ({}%) | Message: {}",
                event.getTypeAlerte(),
                event.getNomCiterne(),
                event.getNomProduit(),
                event.getStockActuel(),
                event.getCapaciteNominale(),
                String.format("%.1f", event.getPourcentageRemplissage()),
                event.getMessage());

        metricsService.enregistrerAlerteCuve();

        // Maintien d'un historique glissant des 50 dernières alertes
        alertesRecentes.add(0, event);
        if (alertesRecentes.size() > 50) {
            alertesRecentes.remove(alertesRecentes.size() - 1);
        }
    }

    @Async
    @EventListener
    public void onClotureShift(ClotureShiftEvent event) {
        log.info("💰 [EVENT CLÔTURE SHIFT] Équipe: {} (Chef: {}) | Recettes: {} DT | Encaissé: {} DT | Écart: {} DT",
                event.getNomEquipe(),
                event.getChefEquipe(),
                event.getCaTheoriqueTotal(),
                event.getTotalReelEncaisse(),
                event.getEcartCaisse());

        metricsService.enregistrerEcartShift(event.getEcartCaisse());
    }

    public List<AlerteStockCuveEvent> getAlertesRecentes() {
        return Collections.unmodifiableList(alertesRecentes);
    }
}
