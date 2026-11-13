package com.station.api.controller;

import com.station.api.model.FactureClient;
import com.station.api.service.StationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/factures")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Facturation Périodique Client B2B", description = "Consolidation des bons de carburant en factures officielles avec timbre fiscal tunisien (1.000 DT)")
public class FactureController {

    private final StationService stationService;

    @GetMapping
    @Operation(summary = "Lister toutes les factures clients", description = "Retourne la liste des factures périodiques B2B émises avec leurs statuts de paiement.")
    @ApiResponse(responseCode = "200", description = "Liste des factures récupérée avec succès")
    public ResponseEntity<List<FactureClient>> getAllFactures() {
        return ResponseEntity.ok(stationService.getAllFactures());
    }

    @PostMapping("/generer/{clientId}")
    @Operation(summary = "Générer une facture pour un client B2B", description = "Agrège tous les bons de carburant non facturés du client, applique le timbre fiscal obligatoire (1.000 DT) et génère la facture officielle.")
    @ApiResponse(responseCode = "200", description = "Facture client générée avec succès")
    public ResponseEntity<FactureClient> genererFacture(
            @Parameter(description = "Identifiant du client B2B") @PathVariable Long clientId) {
        return ResponseEntity.ok(stationService.genererFactureClient(clientId));
    }
}
