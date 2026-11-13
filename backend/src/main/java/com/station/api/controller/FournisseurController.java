package com.station.api.controller;

import com.station.api.model.Fournisseur;
import com.station.api.service.StationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/fournisseurs")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Fournisseurs Pétroliers", description = "Gestion des partenaires d'approvisionnement en hydrocarbures (SNDP Agil, etc.) et solde créditeur")
public class FournisseurController {

    private final StationService stationService;

    @GetMapping
    @Operation(summary = "Lister les fournisseurs", description = "Retourne la liste des fournisseurs pétroliers enregistrés avec leurs soldes dus (mise en cache Redis).")
    @ApiResponse(responseCode = "200", description = "Liste des fournisseurs récupérée")
    public ResponseEntity<List<Fournisseur>> getAllFournisseurs() {
        return ResponseEntity.ok(stationService.getAllFournisseurs());
    }

    @PostMapping
    @Operation(summary = "Créer ou mettre à jour un fournisseur", description = "Enregistre un nouveau fournisseur ou met à jour ses coordonnées (invalide le cache Redis).")
    @ApiResponse(responseCode = "200", description = "Fournisseur enregistré avec succès")
    public ResponseEntity<Fournisseur> createOrUpdateFournisseur(@RequestBody Fournisseur fournisseur) {
        return ResponseEntity.ok(stationService.saveFournisseur(fournisseur));
    }
}
