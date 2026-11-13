package com.station.api.controller;

import com.station.api.model.ReglementFournisseur;
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
@RequestMapping("/api/v1/reglements-fournisseur")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Règlements Fournisseurs", description = "Gestion des paiements, chèques et virements émis en règlement des factures de carburant")
public class ReglementFournisseurController {

    private final StationService stationService;

    @PostMapping
    @Operation(summary = "Enregistrer un règlement fournisseur", description = "Enregistre un paiement effectué à un fournisseur pétrolier (virement, chèque ou espèces) et déduit le montant de la dette.")
    @ApiResponse(responseCode = "200", description = "Règlement enregistré et solde fournisseur mis à jour")
    public ResponseEntity<ReglementFournisseur> enregistrerReglement(@RequestBody ReglementFournisseur reglement) {
        return ResponseEntity.ok(stationService.enregistrerReglementFournisseur(reglement));
    }

    @GetMapping("/fournisseur/{fournisseurId}")
    @Operation(summary = "Lister les règlements d'un fournisseur", description = "Retourne l'historique de tous les paiements effectués à un fournisseur pétrolier.")
    @ApiResponse(responseCode = "200", description = "Historique des règlements récupéré")
    public ResponseEntity<List<ReglementFournisseur>> getReglementsParFournisseur(
            @Parameter(description = "Identifiant unique du fournisseur") @PathVariable Long fournisseurId) {
        return ResponseEntity.ok(stationService.getReglementsFournisseur(fournisseurId));
    }
}
