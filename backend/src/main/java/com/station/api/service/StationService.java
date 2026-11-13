package com.station.api.service;

import com.station.api.config.CacheConfig;
import com.station.api.dto.*;
import com.station.api.event.AlerteStockCuveEvent;
import com.station.api.event.ClotureShiftEvent;
import com.station.api.model.*;
import com.station.api.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StationService {

    private final ProduitRepository produitRepository;
    private final CiterneRepository citerneRepository;
    private final PistoletRepository pistoletRepository;
    private final ReleveIndexRepository releveIndexRepository;
    private final ClotureJourneeRepository clotureJourneeRepository;
    private final ClientRepository clientRepository;
    private final FactureClientRepository factureClientRepository;
    private final BonCarburantRepository bonCarburantRepository;
    private final FournisseurRepository fournisseurRepository;
    private final BordereauLivraisonRepository bordereauLivraisonRepository;
    private final FactureFournisseurRepository factureFournisseurRepository;
    private final ReglementFournisseurRepository reglementFournisseurRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final StationMetricsService metricsService;

    // --- Dynamic Tanks & Nozzles Management ---

    @Cacheable(value = CacheConfig.CACHE_CUVES)
    public List<Citerne> getAllCiternes() {
        return citerneRepository.findAll();
    }

    @CacheEvict(value = CacheConfig.CACHE_CUVES, allEntries = true)
    public Citerne saveCiterne(Citerne citerne) {
        if (citerne.getStockActuel() == null) {
            citerne.setStockActuel(citerne.getCapaciteNominale());
        }
        return citerneRepository.save(citerne);
    }

    public List<Pistolet> getAllPistolets() {
        return pistoletRepository.findAll();
    }

    public Pistolet savePistolet(Pistolet pistolet) {
        return pistoletRepository.save(pistolet);
    }

    @Cacheable(value = CacheConfig.CACHE_PRODUITS)
    public List<Produit> getAllProduits() {
        return produitRepository.findAll();
    }

    @CacheEvict(value = CacheConfig.CACHE_PRODUITS, allEntries = true)
    public Produit saveProduit(Produit produit) {
        return produitRepository.save(produit);
    }

    // --- Daily Meter Readings Processing ---

    @Transactional
    public List<ReleveIndex> enregistrerRelevesQuotidien(ReleveDailyBatchDto batchDto) {
        LocalDate date = batchDto.getDateReleve() != null ? batchDto.getDateReleve() : LocalDate.now();
        List<ReleveIndex> resultats = new ArrayList<>();

        for (ReleveInputDto input : batchDto.getReleves()) {
            Pistolet pistolet = pistoletRepository.findById(input.getPistoletId())
                    .orElseThrow(() -> new IllegalArgumentException("Pistolet introuvable ID: " + input.getPistoletId()));

            BigDecimal indexDepart = pistolet.getDernierIndex();
            BigDecimal indexFin = input.getIndexFin();

            if (indexFin.compareTo(indexDepart) < 0) {
                throw new IllegalArgumentException("L'index de fin (" + indexFin + ") pour " + pistolet.getNom() +
                        " ne peut pas être inférieur à l'index de départ (" + indexDepart + ")");
            }

            BigDecimal volumeVendu = indexFin.subtract(indexDepart);
            BigDecimal prixUnitaire = pistolet.getCiterne().getProduit().getPrixUnitaire();
            BigDecimal montantTotal = volumeVendu.multiply(prixUnitaire).setScale(3, RoundingMode.HALF_UP);

            // Mise à jour du stock de la citerne
            Citerne citerne = pistolet.getCiterne();
            BigDecimal nouveauStock = citerne.getStockActuel().subtract(volumeVendu);
            if (nouveauStock.compareTo(BigDecimal.ZERO) < 0) {
                nouveauStock = BigDecimal.ZERO;
            }
            citerne.setStockActuel(nouveauStock);
            citerneRepository.save(citerne);

            // Télémétrie et Métriques Prometheus
            metricsService.enregistrerDebitCarburant(volumeVendu.doubleValue(), montantTotal.doubleValue());

            // Détection automatique événementielle de seuil critique / rupture
            if (nouveauStock.compareTo(citerne.getSeuilAlerte()) <= 0) {
                double pct = citerne.getCapaciteNominale().compareTo(BigDecimal.ZERO) > 0
                        ? nouveauStock.divide(citerne.getCapaciteNominale(), 4, RoundingMode.HALF_UP).doubleValue() * 100.0
                        : 0.0;
                eventPublisher.publishEvent(AlerteStockCuveEvent.builder()
                        .citerneId(citerne.getId())
                        .nomCiterne(citerne.getNom())
                        .nomProduit(citerne.getProduit().getNom())
                        .stockActuel(nouveauStock)
                        .capaciteNominale(citerne.getCapaciteNominale())
                        .seuilAlerte(citerne.getSeuilAlerte())
                        .pourcentageRemplissage(pct)
                        .typeAlerte(pct < 10.0 ? AlerteStockCuveEvent.TypeAlerte.RUPTURE_IMMINENTE : AlerteStockCuveEvent.TypeAlerte.SEUIL_MINIMUM_ATTEINT)
                        .message("Stock bas sur " + citerne.getNom() + " (" + citerne.getProduit().getNom() + ") : " + nouveauStock + " L restants.")
                        .horodatage(LocalDateTime.now())
                        .build());
            }

            // Mise à jour du dernier index du pistolet
            pistolet.setDernierIndex(indexFin);
            pistoletRepository.save(pistolet);

            // Enregistrement de la ligne de relevé
            ReleveIndex releve = ReleveIndex.builder()
                    .dateReleve(date)
                    .pistolet(pistolet)
                    .indexDepart(indexDepart)
                    .indexFin(indexFin)
                    .volumeVendu(volumeVendu)
                    .prixUnitaire(prixUnitaire)
                    .montantTotal(montantTotal)
                    .build();

            resultats.add(releveIndexRepository.save(releve));
        }

        return resultats;
    }

    public List<ReleveIndex> getRelevesParDate(LocalDate date) {
        return releveIndexRepository.findByDateReleve(date);
    }

    // --- Tankers Delivery & Purchase Management ---

    @Transactional
    @CacheEvict(value = CacheConfig.CACHE_CUVES, allEntries = true)
    public BordereauLivraison enregistrerLivraison(LivraisonInputDto dto) {
        Citerne citerne = citerneRepository.findById(dto.getCiterneId())
                .orElseThrow(() -> new IllegalArgumentException("Citerne introuvable ID: " + dto.getCiterneId()));

        Fournisseur fournisseur = fournisseurRepository.findById(dto.getFournisseurId())
                .orElseThrow(() -> new IllegalArgumentException("Fournisseur introuvable ID: " + dto.getFournisseurId()));

        BigDecimal volumeLivre = dto.getVolumeLivre();
        BigDecimal prixUnitaire = dto.getPrixUnitaire();
        BigDecimal montantTotal = volumeLivre.multiply(prixUnitaire).setScale(3, RoundingMode.HALF_UP);

        // Augmentation automatique du stock de la cuve
        BigDecimal nouveauStock = citerne.getStockActuel().add(volumeLivre);
        if (nouveauStock.compareTo(citerne.getCapaciteNominale()) > 0) {
            nouveauStock = citerne.getCapaciteNominale();
        }
        citerne.setStockActuel(nouveauStock);
        citerneRepository.save(citerne);

        // Mise à jour de la dette fournisseur (Achats)
        BigDecimal soldeActuel = fournisseur.getSoldeDu() != null ? fournisseur.getSoldeDu() : BigDecimal.ZERO;
        fournisseur.setSoldeDu(soldeActuel.add(montantTotal));
        fournisseurRepository.save(fournisseur);

        BordereauLivraison bl = BordereauLivraison.builder()
                .numeroBordereau(dto.getNumeroBordereau())
                .dateLivraison(dto.getDateLivraison() != null ? dto.getDateLivraison() : LocalDate.now())
                .fournisseur(fournisseur)
                .citerne(citerne)
                .volumeLivre(volumeLivre)
                .prixUnitaire(prixUnitaire)
                .montantTotal(montantTotal)
                .observations(dto.getObservations())
                .build();

        return bordereauLivraisonRepository.save(bl);
    }

    public List<BordereauLivraison> getAllLivraisons() {
        return bordereauLivraisonRepository.findAll();
    }

    @Cacheable(value = CacheConfig.CACHE_FOURNISSEURS)
    public List<Fournisseur> getAllFournisseurs() {
        return fournisseurRepository.findAll();
    }

    @CacheEvict(value = CacheConfig.CACHE_FOURNISSEURS, allEntries = true)
    public Fournisseur saveFournisseur(Fournisseur fournisseur) {
        if (fournisseur.getSoldeDu() == null) {
            fournisseur.setSoldeDu(BigDecimal.ZERO);
        }
        return fournisseurRepository.save(fournisseur);
    }

    @Transactional
    @CacheEvict(value = CacheConfig.CACHE_FOURNISSEURS, allEntries = true)
    public ReglementFournisseur enregistrerReglementFournisseur(ReglementFournisseur reglement) {
        Fournisseur fournisseur = fournisseurRepository.findById(reglement.getFournisseur().getId())
                .orElseThrow(() -> new IllegalArgumentException("Fournisseur introuvable"));

        BigDecimal montant = reglement.getMontant();
        BigDecimal soldeActuel = fournisseur.getSoldeDu() != null ? fournisseur.getSoldeDu() : BigDecimal.ZERO;
        BigDecimal nouveauSolde = soldeActuel.subtract(montant);
        if (nouveauSolde.compareTo(BigDecimal.ZERO) < 0) {
            nouveauSolde = BigDecimal.ZERO;
        }
        fournisseur.setSoldeDu(nouveauSolde);
        fournisseurRepository.save(fournisseur);

        if (reglement.getDateReglement() == null) {
            reglement.setDateReglement(LocalDate.now());
        }
        return reglementFournisseurRepository.save(reglement);
    }

    public List<ReglementFournisseur> getReglementsFournisseur(Long fournisseurId) {
        return reglementFournisseurRepository.findByFournisseurId(fournisseurId);
    }

    // --- B2B Clients & Fuel Vouchers ---

    public List<Client> getAllClients() {
        return clientRepository.findAll();
    }

    public Client saveClient(Client client) {
        if (client.getSoldeDu() == null) {
            client.setSoldeDu(BigDecimal.ZERO);
        }
        return clientRepository.save(client);
    }

    @Transactional
    public BonCarburant enregistrerBonCarburant(BonCarburantInputDto dto) {
        Client client = clientRepository.findById(dto.getClientId())
                .orElseThrow(() -> new IllegalArgumentException("Client introuvable ID: " + dto.getClientId()));

        Produit produit = produitRepository.findById(dto.getProduitId())
                .orElseThrow(() -> new IllegalArgumentException("Produit introuvable ID: " + dto.getProduitId()));

        BigDecimal volume = dto.getVolume();
        BigDecimal prixUnitaire = produit.getPrixUnitaire();
        BigDecimal montant = volume.multiply(prixUnitaire).setScale(3, RoundingMode.HALF_UP);

        // Mise à jour du solde du client
        BigDecimal nouveauSolde = client.getSoldeDu().add(montant);
        client.setSoldeDu(nouveauSolde);
        clientRepository.save(client);

        BonCarburant bon = BonCarburant.builder()
                .numeroBon(dto.getNumeroBon())
                .dateEmission(dto.getDateEmission() != null ? dto.getDateEmission() : LocalDate.now())
                .client(client)
                .produit(produit)
                .volume(volume)
                .prixUnitaire(prixUnitaire)
                .montant(montant)
                .modeReglement(dto.getModeReglement() != null ? dto.getModeReglement() : "CARTE_BON")
                .statut("NON_FACTURE")
                .build();

        return bonCarburantRepository.save(bon);
    }

    public List<BonCarburant> getBonsParClient(Long clientId) {
        return bonCarburantRepository.findByClientId(clientId);
    }

    @Transactional
    public FactureClient genererFactureClient(Long clientId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new IllegalArgumentException("Client introuvable ID: " + clientId));

        List<BonCarburant> bonsNonFactures = bonCarburantRepository.findByClientIdAndStatut(clientId, "NON_FACTURE");
        if (bonsNonFactures.isEmpty()) {
            throw new IllegalStateException("Aucun bon de carburant non facturé pour le client: " + client.getRaisonSociale());
        }

        BigDecimal montantHt = bonsNonFactures.stream()
                .map(BonCarburant::getMontant)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal timbreFiscal = new BigDecimal("1.000");
        BigDecimal montantTtc = montantHt.add(timbreFiscal);

        String numFacture = "FAC-" + LocalDate.now().getYear() + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        FactureClient facture = FactureClient.builder()
                .numeroFacture(numFacture)
                .dateFacture(LocalDate.now())
                .client(client)
                .montantHt(montantHt)
                .timbreFiscal(timbreFiscal)
                .montantTtc(montantTtc)
                .montantPaye(BigDecimal.ZERO)
                .statut("IMPAYEE")
                .dateEcheance(LocalDate.now().plusDays(30))
                .build();

        FactureClient savedFacture = factureClientRepository.save(facture);

        // Association des bons à la facture
        for (BonCarburant bon : bonsNonFactures) {
            bon.setStatut("FACTURE");
            bon.setFacture(savedFacture);
            bonCarburantRepository.save(bon);
        }

        return savedFacture;
    }

    public List<FactureClient> getAllFactures() {
        return factureClientRepository.findAll();
    }

    // --- Shift Closing & Cash Count ---

    @Transactional
    public ClotureJournee cloturerJournee(ClotureDailyDto dto) {
        LocalDate date = dto.getDateCloture() != null ? dto.getDateCloture() : LocalDate.now();

        List<ReleveIndex> releves = releveIndexRepository.findByDateReleve(date);
        BigDecimal caTheoriquePiste = releves.stream()
                .map(ReleveIndex::getMontantTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal caKiosque = dto.getCaKiosque() != null ? dto.getCaKiosque() : BigDecimal.ZERO;
        BigDecimal caLavageVidange = dto.getCaLavageVidange() != null ? dto.getCaLavageVidange() : BigDecimal.ZERO;
        BigDecimal caTotalTheorique = caTheoriquePiste.add(caKiosque).add(caLavageVidange);

        BigDecimal especesPhysiques = dto.getMontantEspecesPhysique() != null ? dto.getMontantEspecesPhysique() : BigDecimal.ZERO;
        BigDecimal totalBonsAgil = dto.getTotalBonsAgil() != null ? dto.getTotalBonsAgil() : BigDecimal.ZERO;
        BigDecimal totalCartesAgilis = dto.getTotalCartesAgilis() != null ? dto.getTotalCartesAgilis() : BigDecimal.ZERO;
        BigDecimal totalCheques = dto.getTotalCheques() != null ? dto.getTotalCheques() : BigDecimal.ZERO;
        BigDecimal totalCreditsB2B = dto.getTotalCreditsB2B() != null ? dto.getTotalCreditsB2B() : BigDecimal.ZERO;
        BigDecimal totalDepensesPiste = dto.getTotalDepensesPiste() != null ? dto.getTotalDepensesPiste() : BigDecimal.ZERO;

        BigDecimal totalReelEncaisse = especesPhysiques
                .add(totalBonsAgil)
                .add(totalCartesAgilis)
                .add(totalCheques)
                .add(totalCreditsB2B)
                .subtract(totalDepensesPiste);

        BigDecimal ecartCaisse = totalReelEncaisse.subtract(caTotalTheorique);

        ClotureJournee cloture = ClotureJournee.builder()
                .dateCloture(date)
                .nomEquipe(dto.getNomEquipe() != null ? dto.getNomEquipe() : "Équipe 1 (Matin)")
                .chefEquipe(dto.getChefEquipe() != null ? dto.getChefEquipe() : "Chef de Piste")
                .caTheoriquePiste(caTheoriquePiste)
                .caKiosque(caKiosque)
                .caLavageVidange(caLavageVidange)
                .caTotalTheorique(caTotalTheorique)
                .montantEspecesPhysique(especesPhysiques)
                .totalBonsAgil(totalBonsAgil)
                .totalCartesAgilis(totalCartesAgilis)
                .totalCheques(totalCheques)
                .totalCreditsB2B(totalCreditsB2B)
                .totalDepensesPiste(totalDepensesPiste)
                .totalReelEncaisse(totalReelEncaisse)
                .ecartCaisse(ecartCaisse)
                .statut("CLOTURE")
                .observations(dto.getObservations())
                .build();

        ClotureJournee saved = clotureJourneeRepository.save(cloture);

        // Publication de l'événement de fin de quart (Event-Driven)
        eventPublisher.publishEvent(ClotureShiftEvent.builder()
                .clotureId(saved.getId())
                .dateCloture(date)
                .nomEquipe(saved.getNomEquipe())
                .chefEquipe(saved.getChefEquipe())
                .caTheoriqueTotal(caTotalTheorique)
                .totalReelEncaisse(totalReelEncaisse)
                .ecartCaisse(ecartCaisse)
                .horodatage(LocalDateTime.now())
                .build());

        return saved;
    }

    @Transactional
    @CacheEvict(value = CacheConfig.CACHE_CUVES, allEntries = true)
    public Citerne enregistrerJaugeage(Long citerneId, BigDecimal hauteurPigeCm, BigDecimal stockPhysique) {
        Citerne citerne = citerneRepository.findById(citerneId)
                .orElseThrow(() -> new IllegalArgumentException("Citerne introuvable ID: " + citerneId));

        citerne.setHauteurPigeCm(hauteurPigeCm);
        citerne.setStockPhysique(stockPhysique);
        BigDecimal ecart = stockPhysique.subtract(citerne.getStockActuel());
        citerne.setEcartCoulage(ecart);
        Citerne saved = citerneRepository.save(citerne);

        // Détection d'anomalie de jaugeage / fuite si coulage excessif (> 100L en valeur absolue)
        if (ecart.abs().compareTo(new BigDecimal("100")) > 0) {
            double pct = saved.getCapaciteNominale().compareTo(BigDecimal.ZERO) > 0
                    ? stockPhysique.divide(saved.getCapaciteNominale(), 4, RoundingMode.HALF_UP).doubleValue() * 100.0
                    : 0.0;
            eventPublisher.publishEvent(AlerteStockCuveEvent.builder()
                    .citerneId(saved.getId())
                    .nomCiterne(saved.getNom())
                    .nomProduit(saved.getProduit().getNom())
                    .stockActuel(stockPhysique)
                    .capaciteNominale(saved.getCapaciteNominale())
                    .seuilAlerte(saved.getSeuilAlerte())
                    .pourcentageRemplissage(pct)
                    .typeAlerte(AlerteStockCuveEvent.TypeAlerte.COULAGE_ANORMAL)
                    .message("Coulage anormal mesuré à la pige : " + ecart + " L ! Vérifier l'étanchéité de la citerne.")
                    .horodatage(LocalDateTime.now())
                    .build());
        }

        return saved;
    }
}
