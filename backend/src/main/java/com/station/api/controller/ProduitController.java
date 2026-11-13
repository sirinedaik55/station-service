package com.station.api.controller;

import com.station.api.model.Produit;
import com.station.api.service.StationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/produits")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Produits & Lubrifiants Agil", description = "Catalogue des carburants (G050, Sans Plomb), huiles Tanix, bouteilles de gaz et services de lavage")
public class ProduitController {

    private final StationService stationService;

    @GetMapping
    @Operation(summary = "Lister les produits", description = "Retourne le catalogue complet des produits et services avec leurs prix unitaires en Dinars Tunisiens (DT).")
    @ApiResponse(responseCode = "200", description = "Catalogue des produits récupéré avec succès")
    public ResponseEntity<List<Produit>> getAllProduits() {
        return ResponseEntity.ok(stationService.getAllProduits());
    }

    @PostMapping
    @Operation(summary = "Ajouter ou modifier un produit", description = "Crée un nouveau produit ou met à jour le prix / stock kiosque (invalide le cache Redis).")
    @ApiResponse(responseCode = "200", description = "Produit enregistré avec succès")
    public ResponseEntity<Produit> createOrUpdateProduit(@RequestBody Produit produit) {
        return ResponseEntity.ok(stationService.saveProduit(produit));
    }
}
