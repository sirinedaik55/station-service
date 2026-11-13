package com.station.api.controller;

import com.station.api.dto.BonCarburantInputDto;
import com.station.api.model.BonCarburant;
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
@RequestMapping("/api/v1/bons")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Bons Carburant B2B", description = "Saisie des bons de prise de carburant remis à la piste et imputation sur le compte crédit client")
public class BonCarburantController {

    private final StationService stationService;

    @PostMapping
    @Operation(summary = "Enregistrer un bon de carburant", description = "Enregistre un bon émis à la piste pour un véhicule client, calcule le montant et met à jour le solde dû.")
    @ApiResponse(responseCode = "200", description = "Bon carburant enregistré")
    public ResponseEntity<BonCarburant> enregistrerBon(@RequestBody BonCarburantInputDto dto) {
        return ResponseEntity.ok(stationService.enregistrerBonCarburant(dto));
    }

    @GetMapping("/client/{clientId}")
    @Operation(summary = "Lister les bons d'un client", description = "Retourne l'historique de tous les bons de carburant enregistrés pour une entreprise cliente.")
    @ApiResponse(responseCode = "200", description = "Liste des bons récupérée")
    public ResponseEntity<List<BonCarburant>> getBonsParClient(
            @Parameter(description = "Identifiant du client B2B") @PathVariable Long clientId) {
        return ResponseEntity.ok(stationService.getBonsParClient(clientId));
    }
}
