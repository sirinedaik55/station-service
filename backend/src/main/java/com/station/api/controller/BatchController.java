package com.station.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/batch")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Traitements par Lots (Spring Batch)", description = "Exécution des traitements batch de fin de journée : consolidation nocturne et génération automatique des factures B2B")
public class BatchController {

    private final JobLauncher jobLauncher;
    private final Job clotureNocturneJob;

    @PostMapping("/declencher-facturation-nocturne")
    @Operation(summary = "Lancer le batch de facturation nocturne", description = "Déclenche le job Spring Batch pour parcourir tous les clients B2B, agréger leurs bons non facturés et générer leurs factures avec timbre fiscal.")
    @ApiResponse(responseCode = "200", description = "Job Spring Batch exécuté avec succès")
    public ResponseEntity<String> lancerBatchFacturation() {
        try {
            JobParameters params = new JobParametersBuilder()
                .addLong("executionTime", System.currentTimeMillis())
                .toJobParameters();

            jobLauncher.run(clotureNocturneJob, params);
            return ResponseEntity.ok("Job Spring Batch de facturation nocturne exécuté avec succès !");
        } catch (Exception e) {
            log.error("Erreur exécution Spring Batch : ", e);
            return ResponseEntity.internalServerError().body("Erreur lors de l'exécution du batch : " + e.getMessage());
        }
    }
}
