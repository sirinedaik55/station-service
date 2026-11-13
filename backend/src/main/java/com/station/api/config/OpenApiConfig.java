package com.station.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI stationOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Station-Service Agil - Enterprise Core API")
                .description("API REST d'entreprise pour la gestion opérationnelle et financière des stations-service (Réseau SNDP Agil Tunisie). " +
                    "Modules : Téléjaugeage des citernes, Saisie quotidienne des index pistolets, Facturation périodique B2B avec timbre fiscal tunisien, " +
                    "Feuille de quart & Clôture de caisse multi-règlements (Espèces, Bons Agil, Cartes Agilis), Baie de lavage et lubrifiants Tanix.")
                .version("v1.0.0")
                .contact(new Contact()
                    .name("Direction des Systèmes d'Information - Station Agil")
                    .email("contact@station-agil.tn")
                    .url("https://github.com/"))
                .license(new License()
                    .name("Apache 2.0")
                    .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
            .servers(List.of(
                new Server().url("http://localhost:8080").description("Serveur Local / Développement"),
                new Server().url("https://station.agil.tn").description("Serveur de Production (SNDP Agil)")
            ));
    }
}
