package com.station.api.batch;

import com.station.api.model.Client;
import com.station.api.model.FactureClient;
import com.station.api.repository.ClientRepository;
import com.station.api.service.StationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class BatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final ClientRepository clientRepository;
    private final StationService stationService;

    @Bean
    public ItemReader<Client> clientB2BItemReader() {
        List<Client> clients = clientRepository.findAll();
        return new ListItemReader<>(clients);
    }

    @Bean
    public ItemProcessor<Client, FactureClient> facturationItemProcessor() {
        return client -> {
            try {
                log.info("📋 [SPRING BATCH] Traitement automatique facturation périodique pour client: {}", client.getRaisonSociale());
                return stationService.genererFactureClient(client.getId());
            } catch (IllegalStateException e) {
                // Aucun bon non facturé pour ce client, passage au suivant
                return null;
            }
        };
    }

    @Bean
    public ItemWriter<FactureClient> facturationItemWriter() {
        return chunk -> {
            for (FactureClient facture : chunk) {
                log.info("✅ [SPRING BATCH FACTURE GÉNÉRÉE] Facture N°: {} | Client: {} | Total TTC: {} DT (incluant timbre fiscal)",
                    facture.getNumeroFacture(), facture.getClient().getRaisonSociale(), facture.getMontantTtc());
            }
        };
    }

    @Bean
    public Step clotureNocturneStep(ItemReader<Client> reader,
                                   ItemProcessor<Client, FactureClient> processor,
                                   ItemWriter<FactureClient> writer) {
        return new StepBuilder("clotureNocturneStep", jobRepository)
            .<Client, FactureClient>chunk(10, transactionManager)
            .reader(reader)
            .processor(processor)
            .writer(writer)
            .build();
    }

    @Bean
    public Job clotureNocturneJob(Step clotureNocturneStep) {
        return new JobBuilder("clotureNocturneJob", jobRepository)
            .start(clotureNocturneStep)
            .build();
    }
}
