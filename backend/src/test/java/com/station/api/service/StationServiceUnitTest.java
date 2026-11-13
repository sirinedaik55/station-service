package com.station.api.service;

import com.station.api.dto.ClotureDailyDto;
import com.station.api.dto.LivraisonInputDto;
import com.station.api.model.*;
import com.station.api.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Tests - StationService (Règles Métier Agil Tunisie)")
class StationServiceUnitTest {

    @Mock
    private ProduitRepository produitRepository;
    @Mock
    private CiterneRepository citerneRepository;
    @Mock
    private PistoletRepository pistoletRepository;
    @Mock
    private ReleveIndexRepository releveIndexRepository;
    @Mock
    private ClotureJourneeRepository clotureJourneeRepository;
    @Mock
    private ClientRepository clientRepository;
    @Mock
    private FactureClientRepository factureClientRepository;
    @Mock
    private BonCarburantRepository bonCarburantRepository;
    @Mock
    private FournisseurRepository fournisseurRepository;
    @Mock
    private BordereauLivraisonRepository bordereauLivraisonRepository;
    @Mock
    private FactureFournisseurRepository factureFournisseurRepository;
    @Mock
    private ReglementFournisseurRepository reglementFournisseurRepository;
    @Mock
    private org.springframework.context.ApplicationEventPublisher eventPublisher;
    @Mock
    private StationMetricsService metricsService;

    @InjectMocks
    private StationService stationService;

    @Test
    @DisplayName("Devrait calculer avec exactitude l'écart de caisse shift multi-règlements (Espèces, Bons Agil, Cartes Agilis)")
    void testCloturerJourneeMultiReglement() {
        // GIVEN - Shift Équipe 1 avec encaissements variés
        LocalDate date = LocalDate.of(2026, 11, 2);
        ClotureDailyDto dto = ClotureDailyDto.builder()
                .dateCloture(date)
                .nomEquipe("Équipe 1 (Matin 06h - 14h)")
                .chefEquipe("Mohamed Ben Ali")
                .caKiosque(new BigDecimal("150.000"))
                .caLavageVidange(new BigDecimal("80.000"))
                .montantEspecesPhysique(new BigDecimal("1200.000"))
                .totalBonsAgil(new BigDecimal("450.000"))
                .totalCartesAgilis(new BigDecimal("600.000"))
                .totalCheques(new BigDecimal("300.000"))
                .totalCreditsB2B(new BigDecimal("250.000"))
                .totalDepensesPiste(new BigDecimal("50.000"))
                .observations("Bordereau shift OK")
                .build();

        // Pistolet avec volume débité = 1000L à 2.250 DT/L = 2250.000 DT
        Produit g050 = Produit.builder().id(1L).nom("Gazole 50").prixUnitaire(new BigDecimal("2.250")).build();
        Citerne cuve = Citerne.builder().id(1L).produit(g050).build();
        Pistolet pistolet = Pistolet.builder().id(1L).citerne(cuve).dernierIndex(new BigDecimal("10000.00")).build();
        ReleveIndex releve = ReleveIndex.builder()
                .dateReleve(date)
                .pistolet(pistolet)
                .indexDepart(new BigDecimal("10000.00"))
                .indexFin(new BigDecimal("11000.00"))
                .volumeVendu(new BigDecimal("1000.000"))
                .montantTotal(new BigDecimal("2250.000"))
                .build();

        when(releveIndexRepository.findByDateReleve(date)).thenReturn(List.of(releve));
        when(clotureJourneeRepository.save(any(ClotureJournee.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // WHEN
        ClotureJournee cloture = stationService.cloturerJournee(dto);

        // THEN
        // CA Théorique = 2250 (Piste) + 150 (Kiosque) + 80 (Lavage) = 2480.000 DT
        assertThat(cloture.getCaTheoriquePiste()).isEqualByComparingTo("2250.000");
        assertThat(cloture.getCaTotalTheorique()).isEqualByComparingTo("2480.000");

        // Total Réel Encaissé = 1200 + 450 + 600 + 300 + 250 - 50 = 2750.000 DT
        assertThat(cloture.getTotalReelEncaisse()).isEqualByComparingTo("2750.000");

        // Écart de caisse = 2750 - 2480 = +270.000 DT (Surplus)
        assertThat(cloture.getEcartCaisse()).isEqualByComparingTo("270.000");
        assertThat(cloture.getNomEquipe()).isEqualTo("Équipe 1 (Matin 06h - 14h)");
        assertThat(cloture.getChefEquipe()).isEqualTo("Mohamed Ben Ali");
    }

    @Test
    @DisplayName("Devrait calculer le coulage lors du jaugeage physique à la pige")
    void testEnregistrerJaugeagePhysiqueEtCoulage() {
        // GIVEN - Citerne avec 20 000 L de stock théorique
        Citerne citerne = Citerne.builder()
                .id(1L)
                .nom("Citerne Gazole 50")
                .capaciteNominale(new BigDecimal("30000"))
                .stockActuel(new BigDecimal("20000.0"))
                .build();

        when(citerneRepository.findById(1L)).thenReturn(Optional.of(citerne));
        when(citerneRepository.save(any(Citerne.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // WHEN - Jaugeage à la pige mesuré à 19 950 L (perte admissible de 50L)
        BigDecimal hauteurPige = new BigDecimal("175.5");
        BigDecimal volumeMesure = new BigDecimal("19950.0");
        Citerne resultat = stationService.enregistrerJaugeage(1L, hauteurPige, volumeMesure);

        // THEN
        assertThat(resultat.getHauteurPigeCm()).isEqualByComparingTo("175.5");
        assertThat(resultat.getStockPhysique()).isEqualByComparingTo("19950.0");
        // Coulage = 19950 - 20000 = -50.0 L
        assertThat(resultat.getEcartCoulage()).isEqualByComparingTo("-50.0");
    }

    @Test
    @DisplayName("Devrait appliquer le timbre fiscal tunisien (1.000 DT) lors de la génération d'une facture B2B")
    void testGenererFactureClientAvecTimbreFiscal() {
        // GIVEN
        Client client = Client.builder().id(10L).raisonSociale("Société Transport Sfaxienne").soldeDu(new BigDecimal("500.000")).build();
        Produit ssp = Produit.builder().id(2L).nom("Sans Plomb").prixUnitaire(new BigDecimal("2.450")).build();

        BonCarburant bon1 = BonCarburant.builder()
                .id(1L)
                .numeroBon("BON-001")
                .client(client)
                .produit(ssp)
                .volume(new BigDecimal("100"))
                .montant(new BigDecimal("245.000"))
                .statut("NON_FACTURE")
                .build();

        BonCarburant bon2 = BonCarburant.builder()
                .id(2L)
                .numeroBon("BON-002")
                .client(client)
                .produit(ssp)
                .volume(new BigDecimal("50"))
                .montant(new BigDecimal("122.500"))
                .statut("NON_FACTURE")
                .build();

        when(clientRepository.findById(10L)).thenReturn(Optional.of(client));
        when(bonCarburantRepository.findByClientIdAndStatut(10L, "NON_FACTURE")).thenReturn(List.of(bon1, bon2));
        when(factureClientRepository.save(any(FactureClient.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // WHEN
        FactureClient facture = stationService.genererFactureClient(10L);

        // THEN
        // Montant HT = 245.000 + 122.500 = 367.500 DT
        assertThat(facture.getMontantHt()).isEqualByComparingTo("367.500");
        // Timbre Fiscal obligatoire en Tunisie = 1.000 DT
        assertThat(facture.getTimbreFiscal()).isEqualByComparingTo("1.000");
        // Montant TTC = 367.500 + 1.000 = 368.500 DT
        assertThat(facture.getMontantTtc()).isEqualByComparingTo("368.500");
    }

    @Test
    @DisplayName("Devrait augmenter le stock cuve lors d'un dépotage sans dépasser la capacité nominale")
    void testEnregistrerLivraisonDepotage() {
        // GIVEN
        Citerne cuve = Citerne.builder()
                .id(1L)
                .nom("Citerne GO50")
                .capaciteNominale(new BigDecimal("30000.000"))
                .stockActuel(new BigDecimal("25000.000"))
                .build();
        Fournisseur sndp = Fournisseur.builder().id(1L).nomSociete("SNDP Agil").soldeDu(BigDecimal.ZERO).build();

        LivraisonInputDto dto = LivraisonInputDto.builder()
                .numeroBordereau("BL-AGIL-2026-99")
                .citerneId(1L)
                .fournisseurId(1L)
                .volumeLivre(new BigDecimal("8000.000")) // 25000 + 8000 = 33000 > 30000 -> Plafonné à 30000
                .prixUnitaire(new BigDecimal("2.100"))
                .build();

        when(citerneRepository.findById(1L)).thenReturn(Optional.of(cuve));
        when(fournisseurRepository.findById(1L)).thenReturn(Optional.of(sndp));
        when(bordereauLivraisonRepository.save(any(BordereauLivraison.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // WHEN
        BordereauLivraison bl = stationService.enregistrerLivraison(dto);

        // THEN
        assertThat(cuve.getStockActuel()).isEqualByComparingTo("30000.000"); // Plafonné
        assertThat(sndp.getSoldeDu()).isEqualByComparingTo("16800.000"); // 8000 * 2.100
        assertThat(bl.getNumeroBordereau()).isEqualTo("BL-AGIL-2026-99");
    }
}
