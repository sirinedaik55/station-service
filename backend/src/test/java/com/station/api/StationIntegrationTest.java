package com.station.api;

import com.station.api.model.Produit;
import com.station.api.repository.ProduitRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@Tag("integration")
@DisplayName("Integration Tests - Testcontainers PostgreSQL 17")
public class StationIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired
    private ProduitRepository produitRepository;

    @Test
    @DisplayName("Devrait exécuter les migrations Flyway V1, V2, V3 et persister les produits réels Agil")
    void testFlywayMigrationEtProduitsAgilDansVraiPostgres() {
        assertThat(postgres.isRunning()).isTrue();

        List<Produit> produits = produitRepository.findAll();
        assertThat(produits).isNotEmpty();

        // Vérification de la présence des lubrifiants officiels Agil Tanix insérés par Flyway V3
        boolean aTanixSuper = produits.stream()
                .anyMatch(p -> p.getNom() != null && p.getNom().contains("TANIX"));
        assertThat(aTanixSuper).isTrue();
    }
}
