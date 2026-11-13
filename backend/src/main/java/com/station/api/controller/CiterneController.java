package com.station.api.controller;

import com.station.api.model.Citerne;
import com.station.api.service.StationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/cuves")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Citernes & Jaugeage", description = "Gestion des réservoirs de carburant, niveaux de stocks et jaugeage physique (pige & coulage)")
public class CiterneController {

    private final StationService stationService;

    @GetMapping
    @Operation(summary = "Lister toutes les citernes", description = "Retourne la liste des cuves avec leurs stocks actuels, capacités et seuils d'alerte (mise en cache Redis).")
    @ApiResponse(responseCode = "200", description = "Liste des citernes récupérée avec succès")
    public ResponseEntity<List<Citerne>> getAllCuves() {
        return ResponseEntity.ok(stationService.getAllCiternes());
    }

    @PostMapping
    @Operation(summary = "Créer ou modifier une citerne", description = "Ajoute une nouvelle citerne ou met à jour sa capacité et son seuil min d'alerte.")
    @ApiResponse(responseCode = "200", description = "Citerne enregistrée avec succès")
    public ResponseEntity<Citerne> createOrUpdateCuve(@RequestBody Citerne citerne) {
        return ResponseEntity.ok(stationService.saveCiterne(citerne));
    }

    @PostMapping("/{id}/jaugeage")
    @Operation(summary = "Enregistrer un jaugeage physique", description = "Enregistre la hauteur pige (cm) et le volume physique selon barème, calcule automatiquement l'écart de coulage admissible.")
    @ApiResponse(responseCode = "200", description = "Jaugeage enregistré et écart de coulage calculé")
    public ResponseEntity<Citerne> enregistrerJaugeage(
            @Parameter(description = "Identifiant unique de la citerne") @PathVariable Long id,
            @Parameter(description = "Hauteur relevée à la règle pige (cm)") @RequestParam BigDecimal hauteurPigeCm,
            @Parameter(description = "Stock physique mesuré en litres (selon barème citerne)") @RequestParam BigDecimal stockPhysique) {
        return ResponseEntity.ok(stationService.enregistrerJaugeage(id, hauteurPigeCm, stockPhysique));
    }
}
