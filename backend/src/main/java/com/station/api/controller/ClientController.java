package com.station.api.controller;

import com.station.api.model.Client;
import com.station.api.service.StationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/clients")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Clients B2B & Comptes Crédit", description = "Gestion des entreprises clientes en compte, plafonds de crédit autorisés et soldes dus")
public class ClientController {

    private final StationService stationService;

    @GetMapping
    @Operation(summary = "Lister les clients B2B", description = "Retourne la liste des entreprises clientes avec leurs coordonnées, plafonds et solde débiteur actuel.")
    @ApiResponse(responseCode = "200", description = "Liste des clients récupérée avec succès")
    public ResponseEntity<List<Client>> getAllClients() {
        return ResponseEntity.ok(stationService.getAllClients());
    }

    @PostMapping
    @Operation(summary = "Créer ou mettre à jour un client B2B", description = "Enregistre une nouvelle entreprise cliente ou met à jour son plafond de crédit.")
    @ApiResponse(responseCode = "200", description = "Client enregistré avec succès")
    public ResponseEntity<Client> createOrUpdateClient(@RequestBody Client client) {
        return ResponseEntity.ok(stationService.saveClient(client));
    }
}
