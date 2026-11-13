package com.station.api.controller;

import com.station.api.model.Pistolet;
import com.station.api.service.StationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pistolets")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Pistolets & Volucompteurs", description = "Gestion des pistolets de distribution sur piste et rattachement aux citernes")
public class PistoletController {

    private final StationService stationService;

    @GetMapping
    @Operation(summary = "Lister les pistolets", description = "Retourne la liste des pistolets de distribution sur piste avec leur citerne associée et leur dernier index connu.")
    @ApiResponse(responseCode = "200", description = "Liste des pistolets récupérée")
    public ResponseEntity<List<Pistolet>> getAllPistolets() {
        return ResponseEntity.ok(stationService.getAllPistolets());
    }

    @PostMapping
    @Operation(summary = "Créer ou modifier un pistolet", description = "Ajoute un nouveau pistolet sur un îlot de distribution ou met à jour son étalonnage d'index.")
    @ApiResponse(responseCode = "200", description = "Pistolet enregistré avec succès")
    public ResponseEntity<Pistolet> createOrUpdatePistolet(@RequestBody Pistolet pistolet) {
        return ResponseEntity.ok(stationService.savePistolet(pistolet));
    }
}
