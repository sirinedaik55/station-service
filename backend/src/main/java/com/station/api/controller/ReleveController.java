package com.station.api.controller;

import com.station.api.dto.ReleveDailyBatchDto;
import com.station.api.model.ReleveIndex;
import com.station.api.service.StationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/releves")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Relevés d'Index Journaliers", description = "Saisie quotidienne des index compteurs (volucompteurs) par pistolet et déduction automatique des stocks")
public class ReleveController {

    private final StationService stationService;

    @PostMapping
    @Operation(summary = "Enregistrer un lot de relevés d'index", description = "Enregistre les index de fin de service de tous les pistolets, calcule le volume débité, le chiffre d'affaires généré et décrémente les citernes associées.")
    @ApiResponse(responseCode = "200", description = "Relevés enregistrés avec succès")
    public ResponseEntity<List<ReleveIndex>> enregistrerReleves(@RequestBody ReleveDailyBatchDto dto) {
        return ResponseEntity.ok(stationService.enregistrerRelevesQuotidien(dto));
    }

    @GetMapping
    @Operation(summary = "Consulter les relevés par date", description = "Retourne l'historique des relevés d'index et volumes distribués pour une date donnée (par défaut aujourd'hui).")
    @ApiResponse(responseCode = "200", description = "Historique des relevés récupéré avec succès")
    public ResponseEntity<List<ReleveIndex>> getRelevesParDate(
            @Parameter(description = "Date du relevé (format YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate searchDate = date != null ? date : LocalDate.now();
        return ResponseEntity.ok(stationService.getRelevesParDate(searchDate));
    }
}
