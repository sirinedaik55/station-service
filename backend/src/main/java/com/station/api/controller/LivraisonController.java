package com.station.api.controller;

import com.station.api.dto.LivraisonInputDto;
import com.station.api.model.BordereauLivraison;
import com.station.api.service.StationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/livraisons")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Livraisons & Dépotages", description = "Bordereaux de dépotage camion-citerne, approvisionnement des stocks cuves et gestion des dettes fournisseurs")
public class LivraisonController {

    private final StationService stationService;

    @GetMapping
    @Operation(summary = "Lister les livraisons", description = "Retourne l'historique complet des bordereaux de livraison et réapprovisionnements en carburant.")
    @ApiResponse(responseCode = "200", description = "Historique des livraisons récupéré")
    public ResponseEntity<List<BordereauLivraison>> getAllLivraisons() {
        return ResponseEntity.ok(stationService.getAllLivraisons());
    }

    @PostMapping
    @Operation(summary = "Enregistrer un dépotage de camion-citerne", description = "Enregistre le bordereau de livraison, incrémente le stock de la citerne ciblée et met à jour le compte fournisseur.")
    @ApiResponse(responseCode = "200", description = "Livraison enregistrée et stock cuve réapprovisionné")
    public ResponseEntity<BordereauLivraison> enregistrerLivraison(@RequestBody LivraisonInputDto dto) {
        return ResponseEntity.ok(stationService.enregistrerLivraison(dto));
    }
}
