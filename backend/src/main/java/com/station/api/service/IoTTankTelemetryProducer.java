package com.station.api.service;

import com.station.api.config.RabbitMQConfig;
import com.station.api.dto.IoTTankTelemetryMessage;
import com.station.api.model.Citerne;
import com.station.api.repository.CiterneRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "station.iot.simulator.enabled", havingValue = "true", matchIfMissing = true)
public class IoTTankTelemetryProducer {

    private final RabbitTemplate rabbitTemplate;
    private final CiterneRepository citerneRepository;
    private final Random random = new Random();

    @Scheduled(initialDelay = 15000, fixedRate = 30000)
    public void emettreTelemetrieCapteursCuves() {
        try {
            List<Citerne> citernes = citerneRepository.findAll();
            if (citernes.isEmpty()) return;

            // Sélection d'une citerne aléatoire pour la télémétrie IoT
            Citerne cuve = citernes.get(random.nextInt(citernes.size()));
            double temp = 18.0 + (random.nextDouble() * 5.0); // 18°C à 23°C
            double densite = 0.835 + (random.nextDouble() * 0.01);

            IoTTankTelemetryMessage message = IoTTankTelemetryMessage.builder()
                .citerneId(cuve.getId())
                .codeCiterne(cuve.getCode())
                .nomProduit(cuve.getProduit().getNom())
                .stockLitres(cuve.getStockActuel())
                .hauteurPigeCm(cuve.getHauteurPigeCm() != null ? cuve.getHauteurPigeCm() : new BigDecimal("180.0"))
                .temperatureCelsius(Math.round(temp * 10.0) / 10.0)
                .densiteHydrocarbure(Math.round(densite * 1000.0) / 1000.0)
                .statutSonde("OK")
                .horodatage(LocalDateTime.now())
                .build();

            rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.TELEMETRIE_ROUTING_KEY,
                message
            );

            log.info("📡 [IoT SONDE CITERNE] Télémétrie émise vers RabbitMQ -> Cuve: {} ({}) | Temp: {}°C | Stock: {} L",
                cuve.getCode(), cuve.getProduit().getNom(), message.getTemperatureCelsius(), cuve.getStockActuel());

        } catch (Exception e) {
            // Tolérance : Si le broker RabbitMQ n'est pas encore démarré, le simulateur n'interrompt pas l'appli
            log.debug("RabbitMQ en attente de connexion pour la télémétrie IoT : {}", e.getMessage());
        }
    }
}
