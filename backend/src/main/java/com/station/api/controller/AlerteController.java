package com.station.api.controller;

import com.station.api.event.AlerteStockCuveEvent;
import com.station.api.event.StationEventListener;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/alertes")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Alertes & Télémétrie en Temps Réel", description = "Flux d'alertes automatiques émises par les événements système (stocks bas, risques de rupture, anomalies de jaugeage)")
public class AlerteController {

    private final StationEventListener eventListener;

    @GetMapping
    @Operation(summary = "Lister les alertes actives", description = "Retourne la liste des alertes opérationnelles récentes générées par l'architecture événementielle.")
    @ApiResponse(responseCode = "200", description = "Historique des alertes récupéré")
    public ResponseEntity<List<AlerteStockCuveEvent>> getAlertes() {
        return ResponseEntity.ok(eventListener.getAlertesRecentes());
    }
}
