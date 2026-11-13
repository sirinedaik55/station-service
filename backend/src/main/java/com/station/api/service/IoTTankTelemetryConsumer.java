package com.station.api.service;

import com.station.api.config.RabbitMQConfig;
import com.station.api.dto.IoTTankTelemetryMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class IoTTankTelemetryConsumer {

    private final StationMetricsService metricsService;

    @RabbitListener(queues = RabbitMQConfig.TELEMETRIE_QUEUE)
    public void consommerTelemetrieCuve(IoTTankTelemetryMessage msg) {
        log.info("📥 [RABBITMQ REÇU] Télémétrie Citerne {} | Produit: {} | Niveau: {} L | Temp: {}°C",
            msg.getCodeCiterne(), msg.getNomProduit(), msg.getStockLitres(), msg.getTemperatureCelsius());

        // Alerte sécurité incendie / surchauffe
        if (msg.getTemperatureCelsius() > 35.0) {
            log.error("🔥 [ALERTE SÉCURITÉ PÉTROLIÈRE] Surchauffe anormale détectée dans la citerne {} : {}°C",
                msg.getCodeCiterne(), msg.getTemperatureCelsius());
            metricsService.enregistrerAlerteCuve();
        }
    }
}
