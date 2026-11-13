package com.station.api.controller;

import com.station.api.dto.ClotureDailyDto;
import com.station.api.model.ClotureJournee;
import com.station.api.service.StationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/clotures")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Clôtures de Quart & Caisse", description = "Feuille de quart officielle Agil, rapprochement multi-règlements (Espèces, Bons Agil, Cartes Agilis, Chèques, Crédits B2B) et calcul d'écart de caisse")
public class ClotureController {

    private final StationService stationService;

    @PostMapping
    @Operation(summary = "Valider la clôture d'un quart / shift", description = "Enregistre la feuille de quart avec le détail des encaissements réels, compare avec les recettes théoriques (piste + kiosque + lavage) et calcule l'écart de caisse.")
    @ApiResponse(responseCode = "200", description = "Clôture d'équipe enregistrée avec calcul d'écart")
    public ResponseEntity<ClotureJournee> cloturerJournee(@RequestBody ClotureDailyDto dto) {
        return ResponseEntity.ok(stationService.cloturerJournee(dto));
    }
}
