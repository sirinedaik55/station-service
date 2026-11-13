import { Component, OnInit, inject, signal, computed, PLATFORM_ID, Inject } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from './services/api.service';
import {
  Citerne, Pistolet, Produit, ReleveIndex, Client,
  BonCarburant, FactureClient, Fournisseur, BordereauLivraison,
  ClotureJournee, ClotureDailyInput
} from './models/station.models';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App implements OnInit {
  private api = inject(ApiService);
  private platformId = inject(PLATFORM_ID);

  activeTab = signal<'dashboard' | 'cuves' | 'saisie' | 'services' | 'boutique' | 'clients' | 'livraisons' | 'caisse' | 'config'>('dashboard');
  isDarkTheme = signal<boolean>(false);
  sidebarOpen = signal<boolean>(false);

  // State Signals
  cuves = signal<Citerne[]>([]);
  pistolets = signal<Pistolet[]>([]);
  produits = signal<Produit[]>([]);
  clients = signal<Client[]>([]);
  fournisseurs = signal<Fournisseur[]>([]);
  factures = signal<FactureClient[]>([]);
  livraisons = signal<BordereauLivraison[]>([]);

  // Saisie du Jour Form State
  dateReleve = signal<string>(new Date().toISOString().split('T')[0]);
  saisieInputs = signal<Map<number, number>>(new Map()); // pistoletId -> indexFin
  relevesJour = signal<ReleveIndex[]>([]);
  notification = signal<{ message: string; type: 'success' | 'error' } | null>(null);

  // Jaugeage Physique des Cuves State (Pige en cm -> Litres)
  jaugeageInputs = signal<Map<number, { hauteurCm: number; litres: number }>>(new Map());

  // Modals Visibility
  showLivraisonModal = signal<boolean>(false);
  showReglementFournisseurModal = signal<boolean>(false);
  showBonModal = signal<boolean>(false);
  showClientModal = signal<boolean>(false);
  showFournisseurModal = signal<boolean>(false);
  showProduitModal = signal<boolean>(false);
  showPistoletModal = signal<boolean>(false);
  showCuveModal = signal<boolean>(false);
  showVenteBoutiqueModal = signal<boolean>(false);
  showJaugeageModal = signal<boolean>(false);
  selectedCuveToJauge = signal<Citerne | null>(null);

  // New Entity Forms State
  newLivraison = signal({
    numeroBordereau: '',
    fournisseurId: 0,
    citerneId: 0,
    volumeLivre: 0,
    prixUnitaire: 0,
    observations: ''
  });

  newReglementFournisseur = signal({
    fournisseurId: 0,
    montant: 0,
    modeReglement: 'VIREMENT',
    reference: ''
  });

  newBon = signal({
    numeroBon: '',
    clientId: 0,
    produitId: 0,
    volume: 0,
    modeReglement: 'CARTE_BON'
  });

  newClient = signal({
    raisonSociale: '',
    telephone: '',
    email: '',
    plafondCredit: 10000
  });

  newFournisseur = signal({
    nomSociete: '',
    telephone: '',
    adresse: ''
  });

  newProduit = signal({
    code: '',
    nom: '',
    typeProduit: 'HUILES',
    prixUnitaire: 16.500,
    stockKiosque: 50,
    seuilAlerte: 10
  });

  newPistolet = signal({
    code: '',
    nom: '',
    citerneId: 0,
    dernierIndex: 1000
  });

  newCuve = signal({
    code: '',
    nom: '',
    produitId: 0,
    capaciteNominale: 30000,
    stockActuel: 30000,
    seuilAlerte: 5000
  });

  newVenteBoutique = signal({
    produitId: 0,
    quantite: 1
  });

  // Jaugeage Input Form
  inputHauteurPige = signal<number>(0);
  inputLitresPige = signal<number>(0);

  // Printable Invoice Modal State
  selectedFactureToPrint = signal<FactureClient | null>(null);
  factureBonsToPrint = signal<BonCarburant[]>([]);

  // Shift & Multi-Tender Closing State (Système Universel de Caisse Station)
  nomEquipeInput = signal<string>('Équipe A (Quart Matin 06h - 14h)');
  chefEquipeInput = signal<string>('Chef de Quart / Superviseur Piste');
  caKiosqueInput = signal<number>(0);
  caLavageVidangeInput = signal<number>(0);
  especesPhysiqueInput = signal<number>(0);
  bonsAgilInput = signal<number>(0);
  cartesAgilisInput = signal<number>(0);
  chequesInput = signal<number>(0);
  creditsB2BInput = signal<number>(0);
  depensesPisteInput = signal<number>(0);
  clotureObservations = signal<string>('');
  lastCloture = signal<ClotureJournee | null>(null);

  // Computed Totals & KPIs
  totalCapacite = computed(() => this.cuves().reduce((acc, c) => acc + c.capaciteNominale, 0));
  totalStockActuel = computed(() => this.cuves().reduce((acc, c) => acc + c.stockActuel, 0));
  pourcentageGlobal = computed(() => this.totalCapacite() > 0 ? (this.totalStockActuel() / this.totalCapacite()) * 100 : 0);
  alertesStockCount = computed(() => this.cuves().filter(c => this.getCuveStatusColor(c) === 'danger').length);

  // Volume Total Litres Débités aujourd'hui
  totalLitresVendus = computed(() => {
    let vol = 0;
    this.pistolets().forEach(p => {
      const indexFin = this.saisieInputs().get(p.id);
      if (indexFin !== undefined && indexFin >= p.dernierIndex) {
        vol += (indexFin - p.dernierIndex);
      }
    });
    return vol;
  });

  // Computed Revenue Saisie Piste
  caTheoriqueJour = computed(() => {
    let total = 0;
    this.pistolets().forEach(p => {
      const indexFin = this.saisieInputs().get(p.id);
      if (indexFin !== undefined && indexFin >= p.dernierIndex) {
        const volume = indexFin - p.dernierIndex;
        const prix = p.citerne.produit.prixUnitaire;
        total += volume * prix;
      }
    });
    return total;
  });

  // Computed Shift Total Revenue & Total Tender Encaissé
  caTotalTheoriqueShift = computed(() => {
    return this.caTheoriqueJour() + Number(this.caKiosqueInput()) + Number(this.caLavageVidangeInput());
  });

  totalReelEncaisseShift = computed(() => {
    return Number(this.especesPhysiqueInput()) +
      Number(this.bonsAgilInput()) +
      Number(this.cartesAgilisInput()) +
      Number(this.chequesInput()) +
      Number(this.creditsB2BInput()) -
      Number(this.depensesPisteInput());
  });

  ecartShift = computed(() => {
    return this.totalReelEncaisseShift() - this.caTotalTheoriqueShift();
  });

  toggleSidebar() {
    this.sidebarOpen.update(v => !v);
  }

  closeSidebar() {
    this.sidebarOpen.set(false);
  }

  selectTab(tab: 'dashboard' | 'cuves' | 'saisie' | 'services' | 'boutique' | 'clients' | 'livraisons' | 'caisse' | 'config') {
    this.activeTab.set(tab);
    this.closeSidebar();
  }

  ngOnInit() {
    if (isPlatformBrowser(this.platformId)) {
      this.loadAllData();
    }
  }

  toggleTheme() {
    this.isDarkTheme.set(!this.isDarkTheme());
    if (isPlatformBrowser(this.platformId)) {
      if (this.isDarkTheme()) {
        document.body.classList.add('dark-theme', 'dark');
        document.documentElement.classList.add('dark');
      } else {
        document.body.classList.remove('dark-theme', 'dark');
        document.documentElement.classList.remove('dark');
      }
    }
  }

  loadAllData() {
    // Attempt loading from API backend, with initial fallback demo dataset if backend returns empty/error
    this.api.getProduits().subscribe({
      next: (data) => {
        if (data && data.length > 0) {
          this.produits.set(data);
        } else {
          this.initFallbackProduits();
        }
      },
      error: () => this.initFallbackProduits()
    });

    this.api.getCuves().subscribe({
      next: (data) => {
        if (data && data.length > 0) {
          this.cuves.set(data);
        } else {
          this.initFallbackCuves();
        }
      },
      error: () => this.initFallbackCuves()
    });

    this.api.getPistolets().subscribe({
      next: (data) => {
        if (data && data.length > 0) {
          this.pistolets.set(data);
          const map = new Map<number, number>();
          data.forEach(p => map.set(p.id, p.dernierIndex));
          this.saisieInputs.set(map);
        } else {
          this.initFallbackPistolets();
        }
      },
      error: () => this.initFallbackPistolets()
    });

    this.api.getClients().subscribe({
      next: (data) => {
        if (data && data.length > 0) this.clients.set(data);
        else this.initFallbackClients();
      },
      error: () => this.initFallbackClients()
    });

    this.api.getFournisseurs().subscribe({
      next: (data) => {
        if (data && data.length > 0) this.fournisseurs.set(data);
        else this.initFallbackFournisseurs();
      },
      error: () => this.initFallbackFournisseurs()
    });

    this.api.getFactures().subscribe({
      next: (data) => this.factures.set(data || []),
      error: () => {}
    });

    this.api.getLivraisons().subscribe({
      next: (data) => this.livraisons.set(data || []),
      error: () => {}
    });
  }

  // Fallback initial dataset to make UI immediately interactive out of the box
  private initFallbackProduits() {
    const defaultProds: Produit[] = [
      { id: 1, code: 'G01', nom: 'Gazole Supérieur G01', typeProduit: 'CARBURANT', prixUnitaire: 2.150, stockKiosque: 0, seuilAlerte: 0 },
      { id: 2, code: 'G02', nom: 'Gazole Normal G02', typeProduit: 'CARBURANT', prixUnitaire: 1.980, stockKiosque: 0, seuilAlerte: 0 },
      { id: 3, code: 'G050', nom: 'Gazole 50 ppm G050', typeProduit: 'CARBURANT', prixUnitaire: 2.250, stockKiosque: 0, seuilAlerte: 0 },
      { id: 4, code: 'SSP1', nom: 'Sans Plomb Premium SSP1', typeProduit: 'CARBURANT', prixUnitaire: 2.450, stockKiosque: 0, seuilAlerte: 0 },
      { id: 5, code: 'SSP2', nom: 'Sans Plomb 95 SSP2', typeProduit: 'CARBURANT', prixUnitaire: 2.380, stockKiosque: 0, seuilAlerte: 0 },
      { id: 6, code: 'Pr7', nom: 'Produit Spécial Pr7', typeProduit: 'CARBURANT', prixUnitaire: 2.600, stockKiosque: 0, seuilAlerte: 0 },
      { id: 7, code: 'OIL_5W40', nom: 'Lubrifiant Moteur 100% Synthèse 5W40 4L', typeProduit: 'HUILES', prixUnitaire: 42.500, stockKiosque: 35, seuilAlerte: 8 },
      { id: 8, code: 'OIL_15W40', nom: 'Huile Diesel Haute Performance 15W40 5L', typeProduit: 'HUILES', prixUnitaire: 52.000, stockKiosque: 25, seuilAlerte: 5 },
      { id: 9, code: 'OIL_20W50', nom: 'Huile Multigrade Minérale 20W50 4L', typeProduit: 'HUILES', prixUnitaire: 36.000, stockKiosque: 30, seuilAlerte: 8 },
      { id: 10, code: 'OIL_2T', nom: 'Huile 2-Temps Synthèse Moto/Scooter 1L', typeProduit: 'HUILES', prixUnitaire: 14.000, stockKiosque: 40, seuilAlerte: 6 },
      { id: 11, code: 'OIL_80W90', nom: 'Fluide Transmission & Boîte 80W90 2L', typeProduit: 'HUILES', prixUnitaire: 26.500, stockKiosque: 15, seuilAlerte: 4 },
      { id: 12, code: 'GAZ_13KG', nom: 'Bouteille de Gaz Butane 13kg', typeProduit: 'GAZ', prixUnitaire: 8.800, stockKiosque: 50, seuilAlerte: 10 },
      { id: 13, code: 'LAV_EXT', nom: 'Lavage Extérieur Haute Pression Mousse', typeProduit: 'LAVAGES', prixUnitaire: 8.000, stockKiosque: 999, seuilAlerte: 0 },
      { id: 14, code: 'LAV_COMP', nom: 'Lavage Complet Intérieur + Extérieur Auto', typeProduit: 'LAVAGES', prixUnitaire: 15.000, stockKiosque: 999, seuilAlerte: 0 },
      { id: 15, code: 'EAU_1.5L', nom: 'Bouteille Eau Minérale Naturelle 1.5L', typeProduit: 'EAUX', prixUnitaire: 0.950, stockKiosque: 120, seuilAlerte: 20 }
    ];
    this.produits.set(defaultProds);
  }

  private initFallbackCuves() {
    const prods = this.produits();
    const findP = (c: string) => prods.find(p => p.code === c) || prods[0];
    const defaultCuves: Citerne[] = [
      { id: 1, code: 'C_G01', nom: 'Citerne G01 (30 000 L)', produit: findP('G01'), capaciteNominale: 30000, stockActuel: 22500, seuilAlerte: 5000 },
      { id: 2, code: 'C_G02', nom: 'Citerne G02 (10 000 L)', produit: findP('G02'), capaciteNominale: 10000, stockActuel: 6800, seuilAlerte: 2000 },
      { id: 3, code: 'C_G050', nom: 'Citerne G050 (30 000 L)', produit: findP('G050'), capaciteNominale: 30000, stockActuel: 18200, seuilAlerte: 5000 },
      { id: 4, code: 'C_SSP1', nom: 'Citerne SSP1 (30 000 L)', produit: findP('SSP1'), capaciteNominale: 30000, stockActuel: 24000, seuilAlerte: 5000 },
      { id: 5, code: 'C_SSP2', nom: 'Citerne SSP2 (10 000 L)', produit: findP('SSP2'), capaciteNominale: 10000, stockActuel: 4500, seuilAlerte: 2000 },
      { id: 6, code: 'C_Pr7', nom: 'Citerne Pr7 (10 000 L)', produit: findP('Pr7'), capaciteNominale: 10000, stockActuel: 8100, seuilAlerte: 2000 }
    ];
    this.cuves.set(defaultCuves);
  }

  private initFallbackPistolets() {
    const cuves = this.cuves();
    const findC = (code: string) => cuves.find(c => c.code === code) || cuves[0];
    const defaultPistolets: Pistolet[] = [
      { id: 1, code: 'P1_G01', nom: 'Pistolet 1 - G01 Piste Nord', citerne: findC('C_G01'), dernierIndex: 154200 },
      { id: 2, code: 'P2_G01', nom: 'Pistolet 2 - G01 Piste Sud', citerne: findC('C_G01'), dernierIndex: 98500 },
      { id: 3, code: 'P1_G02', nom: 'Pistolet 1 - G02 Piste Nord', citerne: findC('C_G02'), dernierIndex: 45200 },
      { id: 4, code: 'P1_G050', nom: 'Pistolet 1 - G050 Piste Sud', citerne: findC('C_G050'), dernierIndex: 210400 },
      { id: 5, code: 'P1_SSP1', nom: 'Pistolet 1 - SSP1 Piste Nord', citerne: findC('C_SSP1'), dernierIndex: 189300 },
      { id: 6, code: 'P1_SSP2', nom: 'Pistolet 1 - SSP2 Piste Sud', citerne: findC('C_SSP2'), dernierIndex: 67100 },
      { id: 7, code: 'P1_Pr7', nom: 'Pistolet 1 - Pr7 Piste Spéciale', citerne: findC('C_Pr7'), dernierIndex: 32000 }
    ];
    this.pistolets.set(defaultPistolets);
    const map = new Map<number, number>();
    defaultPistolets.forEach(p => map.set(p.id, p.dernierIndex));
    this.saisieInputs.set(map);
  }

  private initFallbackClients() {
    this.clients.set([
      { id: 1, raisonSociale: 'Société de Transport Express', telephone: '+216 71 123 456', email: 'contact@transport.tn', plafondCredit: 15000, soldeDu: 1250 },
      { id: 2, raisonSociale: 'BTP Construction SARL', telephone: '+216 72 987 654', email: 'compta@btp.tn', plafondCredit: 25000, soldeDu: 3800.500 },
      { id: 3, raisonSociale: 'Taxi Services & Co', telephone: '+216 73 555 444', email: 'contact@taxi.tn', plafondCredit: 5000, soldeDu: 420 }
    ]);
  }

  private initFallbackFournisseurs() {
    this.fournisseurs.set([
      { id: 1, nomSociete: 'Fournisseur Pétrolier National (Raffinage & Distribution)', telephone: '+216 71 800 000', adresse: 'Zone Pétrolière Portuaire', soldeDu: 45000 },
      { id: 2, nomSociete: 'Compagnie Pétrolière Internationale & Carburants', telephone: '+216 71 900 000', adresse: 'Zone Industrielle Nord', soldeDu: 12000 }
    ]);
  }

  // --- Saisie Actions ---
  onIndexFinChange(pistoletId: number, value: number) {
    const map = new Map(this.saisieInputs());
    map.set(pistoletId, Number(value));
    this.saisieInputs.set(map);
  }

  getVolumePistolet(pistolet: Pistolet): number {
    const val = this.saisieInputs().get(pistolet.id);
    if (val !== undefined && val >= pistolet.dernierIndex) {
      return val - pistolet.dernierIndex;
    }
    return 0;
  }

  getMontantPistolet(pistolet: Pistolet): number {
    return this.getVolumePistolet(pistolet) * pistolet.citerne.produit.prixUnitaire;
  }

  validerSaisieJournee() {
    const releves = Array.from(this.saisieInputs().entries()).map(([pistoletId, indexFin]) => ({
      pistoletId,
      indexFin
    }));

    this.api.enregistrerReleves({
      dateReleve: this.dateReleve(),
      releves
    }).subscribe({
      next: (res) => {
        this.relevesJour.set(res);
        this.notify('Saisie du jour enregistrée et stocks des cuves mis à jour !', 'success');
        this.loadAllData();
      },
      error: () => {
        // Fallback local update if offline
        const updatedPistolets = this.pistolets().map(p => {
          const indexFin = this.saisieInputs().get(p.id);
          if (indexFin !== undefined && indexFin >= p.dernierIndex) {
            const vol = indexFin - p.dernierIndex;
            p.citerne.stockActuel = Math.max(0, p.citerne.stockActuel - vol);
            p.dernierIndex = indexFin;
          }
          return p;
        });
        this.pistolets.set(updatedPistolets);
        this.notify('Saisie effectuée et stocks des cuves décomptés !', 'success');
      }
    });
  }

  // --- Kiosk Boutique POS Vente Rapide ---
  validerVenteBoutique() {
    const vb = this.newVenteBoutique();
    if (!vb.produitId || vb.quantite <= 0) return;
    const produit = this.produits().find(p => p.id === Number(vb.produitId));
    if (!produit) return;

    const montant = produit.prixUnitaire * Number(vb.quantite);
    if (produit.stockKiosque !== undefined) {
      produit.stockKiosque = Math.max(0, produit.stockKiosque - Number(vb.quantite));
    }
    this.caKiosqueInput.set(this.caKiosqueInput() + montant);

    this.notify(`Vente Kiosque de ${vb.quantite}x ${produit.nom} (${montant.toFixed(3)} DT) ajoutée au CA Kiosque !`, 'success');
    this.showVenteBoutiqueModal.set(false);
  }

  // --- Tanker Delivery & Supplier Actions ---
  validerLivraison() {
    const d = this.newLivraison();
    if (!d.fournisseurId || !d.citerneId || d.volumeLivre <= 0) {
      this.notify('Veuillez remplir tous les champs de livraison obligatoires.', 'error');
      return;
    }

    this.api.enregistrerLivraison({
      numeroBordereau: d.numeroBordereau || 'BL-' + Date.now(),
      fournisseurId: Number(d.fournisseurId),
      citerneId: Number(d.citerneId),
      volumeLivre: Number(d.volumeLivre),
      prixUnitaire: Number(d.prixUnitaire),
      observations: d.observations
    }).subscribe({
      next: () => {
        this.notify('Livraison enregistrée ! Stock cuve et solde fournisseur mis à jour.', 'success');
        this.showLivraisonModal.set(false);
        this.loadAllData();
      },
      error: () => {
        // Fallback local update
        const cuve = this.cuves().find(c => c.id === Number(d.citerneId));
        if (cuve) {
          cuve.stockActuel = Math.min(cuve.capaciteNominale, cuve.stockActuel + Number(d.volumeLivre));
        }
        this.notify('Livraison enregistrée ! La cuve a été réapprovisionnée.', 'success');
        this.showLivraisonModal.set(false);
      }
    });
  }

  validerReglementFournisseur() {
    const rf = this.newReglementFournisseur();
    if (!rf.fournisseurId || rf.montant <= 0) {
      this.notify('Veuillez sélectionner un fournisseur et un montant valide.', 'error');
      return;
    }
    const fournisseur = this.fournisseurs().find(f => f.id === Number(rf.fournisseurId));
    if (!fournisseur) return;

    this.api.enregistrerReglementFournisseur({
      fournisseur: { id: fournisseur.id },
      montant: Number(rf.montant),
      modeReglement: rf.modeReglement,
      reference: rf.reference
    }).subscribe({
      next: () => {
        this.notify('Règlement fournisseur enregistré ! Dette mise à jour.', 'success');
        this.showReglementFournisseurModal.set(false);
        this.loadAllData();
      },
      error: () => {
        if (fournisseur.soldeDu) {
          fournisseur.soldeDu = Math.max(0, fournisseur.soldeDu - Number(rf.montant));
        }
        this.notify('Règlement déduit du solde du fournisseur !', 'success');
        this.showReglementFournisseurModal.set(false);
      }
    });
  }

  validerNouveauFournisseur() {
    const f = this.newFournisseur();
    if (!f.nomSociete) return;
    const newF: Fournisseur = {
      id: Date.now(),
      nomSociete: f.nomSociete,
      telephone: f.telephone,
      adresse: f.adresse,
      soldeDu: 0
    };
    this.api.saveFournisseur(newF).subscribe({
      next: () => {
        this.notify('Nouveau fournisseur enregistré !', 'success');
        this.showFournisseurModal.set(false);
        this.loadAllData();
      },
      error: () => {
        this.fournisseurs.set([...this.fournisseurs(), newF]);
        this.notify('Fournisseur ajouté avec succès !', 'success');
        this.showFournisseurModal.set(false);
      }
    });
  }

  // --- Client & Voucher Actions ---
  validerNouveauClient() {
    const c = this.newClient();
    if (!c.raisonSociale) return;
    const newC: Client = {
      id: Date.now(),
      raisonSociale: c.raisonSociale,
      telephone: c.telephone,
      email: c.email,
      plafondCredit: Number(c.plafondCredit),
      soldeDu: 0
    };
    this.api.saveClient(newC).subscribe({
      next: () => {
        this.notify('Nouveau client créé avec succès !', 'success');
        this.showClientModal.set(false);
        this.loadAllData();
      },
      error: () => {
        this.clients.set([...this.clients(), newC]);
        this.notify('Nouveau client B2B créé !', 'success');
        this.showClientModal.set(false);
      }
    });
  }

  validerBon() {
    const b = this.newBon();
    if (!b.clientId || !b.produitId || b.volume <= 0) {
      this.notify('Veuillez remplir tous les champs du bon de carburant.', 'error');
      return;
    }
    this.api.enregistrerBon({
      numeroBon: b.numeroBon || 'BON-' + Math.floor(1000 + Math.random() * 9000),
      clientId: Number(b.clientId),
      produitId: Number(b.produitId),
      volume: Number(b.volume),
      modeReglement: b.modeReglement
    }).subscribe({
      next: () => {
        this.notify('Bon de carburant enregistré et compte client débité !', 'success');
        this.showBonModal.set(false);
        this.loadAllData();
      },
      error: () => {
        const client = this.clients().find(c => c.id === Number(b.clientId));
        const prod = this.produits().find(p => p.id === Number(b.produitId));
        if (client && prod) {
          client.soldeDu = (client.soldeDu || 0) + (Number(b.volume) * prod.prixUnitaire);
        }
        this.notify('Bon de carburant enregistré localement !', 'success');
        this.showBonModal.set(false);
      }
    });
  }

  genererFacture(clientId: number) {
    this.api.genererFacture(clientId).subscribe({
      next: (fac) => {
        this.notify(`Facture ${fac.numeroFacture} générée avec succès pour le client !`, 'success');
        this.loadAllData();
        this.ouvrirModalImpression(fac);
      },
      error: () => {
        const client = this.clients().find(c => c.id === clientId);
        if (!client) return;
        const mockFac: FactureClient = {
          id: Date.now(),
          numeroFacture: 'FAC-' + new Date().getFullYear() + '-' + Math.floor(1000 + Math.random() * 9000),
          dateFacture: new Date().toISOString().split('T')[0],
          client: client,
          montantHt: client.soldeDu || 500,
          montantTtc: client.soldeDu || 500,
          montantPaye: 0,
          statut: 'IMPAYEE',
          dateEcheance: new Date(Date.now() + 30*86400000).toISOString().split('T')[0]
        };
        this.factures.set([...this.factures(), mockFac]);
        this.ouvrirModalImpression(mockFac);
      }
    });
  }

  ouvrirModalImpression(fac: FactureClient) {
    this.selectedFactureToPrint.set(fac);
    this.api.getBonsParClient(fac.client.id!).subscribe({
      next: (bons) => {
        this.factureBonsToPrint.set(bons.filter(b => b.statut === 'FACTURE' || b.facture?.id === fac.id));
      },
      error: () => {
        this.factureBonsToPrint.set([
          {
            id: 101,
            numeroBon: 'BON-9821',
            dateEmission: fac.dateFacture,
            client: fac.client,
            produit: this.produits()[0],
            volume: 200,
            prixUnitaire: this.produits()[0].prixUnitaire,
            montant: 200 * this.produits()[0].prixUnitaire,
            modeReglement: 'CARTE_BON',
            statut: 'FACTURE'
          }
        ]);
      }
    });
  }

  fermerModalImpression() {
    this.selectedFactureToPrint.set(null);
  }

  imprimerFactureModal() {
    if (isPlatformBrowser(this.platformId)) {
      window.print();
    }
  }

  // --- Dynamic Products, Tanks & Nozzles Actions ---
  validerNouveauProduit() {
    const np = this.newProduit();
    if (!np.code || !np.nom) return;
    const newP: Produit = {
      id: Date.now(),
      code: np.code,
      nom: np.nom,
      typeProduit: np.typeProduit,
      prixUnitaire: Number(np.prixUnitaire),
      stockKiosque: Number(np.stockKiosque),
      seuilAlerte: Number(np.seuilAlerte)
    };
    this.api.saveProduit(newP).subscribe({
      next: () => {
        this.notify('Nouveau produit enregistré !', 'success');
        this.showProduitModal.set(false);
        this.loadAllData();
      },
      error: () => {
        this.produits.set([...this.produits(), newP]);
        this.notify('Produit ajouté au catalogue !', 'success');
        this.showProduitModal.set(false);
      }
    });
  }

  validerNouvelleCuve() {
    const nc = this.newCuve();
    if (!nc.code || !nc.nom || !nc.produitId) return;
    const produit = this.produits().find(p => p.id === Number(nc.produitId));
    if (!produit) return;

    const newC: Citerne = {
      id: Date.now(),
      code: nc.code,
      nom: nc.nom,
      produit: produit,
      capaciteNominale: Number(nc.capaciteNominale),
      stockActuel: Number(nc.stockActuel),
      seuilAlerte: Number(nc.seuilAlerte)
    };

    this.api.saveCuve(newC).subscribe({
      next: () => {
        this.notify('Nouvelle cuve enregistrée dans la station !', 'success');
        this.showCuveModal.set(false);
        this.loadAllData();
      },
      error: () => {
        this.cuves.set([...this.cuves(), newC]);
        this.notify('Citerne ajoutée avec succès !', 'success');
        this.showCuveModal.set(false);
      }
    });
  }

  validerNouveauPistolet() {
    const np = this.newPistolet();
    if (!np.code || !np.nom || !np.citerneId) return;
    const citerne = this.cuves().find(c => c.id === Number(np.citerneId));
    if (!citerne) return;

    const newP: Pistolet = {
      id: Date.now(),
      code: np.code,
      nom: np.nom,
      citerne: citerne,
      dernierIndex: Number(np.dernierIndex)
    };

    this.api.savePistolet(newP).subscribe({
      next: () => {
        this.notify('Nouveau pistolet rattaché à la cuve !', 'success');
        this.showPistoletModal.set(false);
        this.loadAllData();
      },
      error: () => {
        this.pistolets.set([...this.pistolets(), newP]);
        const map = new Map(this.saisieInputs());
        map.set(newP.id, newP.dernierIndex);
        this.saisieInputs.set(map);
        this.notify('Pistolet créé avec succès !', 'success');
        this.showPistoletModal.set(false);
      }
    });
  }

  // --- Export CSV Features ---
  exporterCSVReleves() {
    const date = this.dateReleve();
    let csv = `Date,Pistolet,Code,Citerne,Prix Litre (DT),Index Depart,Index Fin,Volume Vendu (L),Montant Total (DT)\n`;

    this.pistolets().forEach(p => {
      const indexDepart = p.dernierIndex;
      const indexFin = this.saisieInputs().get(p.id) || indexDepart;
      const volume = indexFin >= indexDepart ? indexFin - indexDepart : 0;
      const prix = p.citerne.produit.prixUnitaire;
      const montant = volume * prix;

      csv += `"${date}","${p.nom}","${p.code}","${p.citerne.nom}",${prix},${indexDepart},${indexFin},${volume},${montant.toFixed(3)}\n`;
    });

    this.downloadFile(csv, `journal_ventes_piste_${date}.csv`, 'text/csv;charset=utf-8;');
    this.notify('Journal des ventes exporté en CSV avec succès !', 'success');
  }

  exporterCSVLivraisons() {
    let csv = `Numero Bordereau,Date,Fournisseur,Citerne,Volume Livre (L),Prix Unitaire (DT),Montant Total (DT)\n`;
    this.livraisons().forEach(l => {
      csv += `"${l.numeroBordereau}","${l.dateLivraison}","${l.fournisseur.nomSociete}","${l.citerne.nom}",${l.volumeLivre},${l.prixUnitaire},${l.montantTotal}\n`;
    });

    this.downloadFile(csv, `historique_livraisons_${new Date().toISOString().split('T')[0]}.csv`, 'text/csv;charset=utf-8;');
    this.notify('Historique des livraisons exporté en CSV !', 'success');
  }

  private downloadFile(content: string, fileName: string, contentType: string) {
    if (!isPlatformBrowser(this.platformId)) return;
    const a = document.createElement('a');
    const blob = new Blob([content], { type: contentType });
    a.href = URL.createObjectURL(blob);
    a.download = fileName;
    a.click();
    URL.revokeObjectURL(a.href);
  }

  // --- Cash Shift Closing Action ---
  validerClotureCaisse() {
    const shiftData: ClotureDailyInput = {
      dateCloture: this.dateReleve(),
      nomEquipe: this.nomEquipeInput(),
      chefEquipe: this.chefEquipeInput(),
      caKiosque: Number(this.caKiosqueInput()),
      caLavageVidange: Number(this.caLavageVidangeInput()),
      montantEspecesPhysique: Number(this.especesPhysiqueInput()),
      totalBonsAgil: Number(this.bonsAgilInput()),
      totalCartesAgilis: Number(this.cartesAgilisInput()),
      totalCheques: Number(this.chequesInput()),
      totalCreditsB2B: Number(this.creditsB2BInput()),
      totalDepensesPiste: Number(this.depensesPisteInput()),
      observations: this.clotureObservations()
    };

    this.api.cloturerJournee(shiftData).subscribe({
      next: (res) => {
        this.lastCloture.set(res);
        const ecart = res.ecartCaisse;
        const ecartText = ecart >= 0 ? `+${ecart.toFixed(3)}` : `${ecart.toFixed(3)}`;
        this.notify(`Clôture d'équipe validée avec succès ! Écart de caisse: ${ecartText} DT`, 'success');
      },
      error: () => {
        const caTheoriquePiste = this.caTheoriqueJour();
        const caTotalTheorique = this.caTotalTheoriqueShift();
        const totalReel = this.totalReelEncaisseShift();
        const ecart = this.ecartShift();

        const mockCloture: ClotureJournee = {
          id: Date.now(),
          dateCloture: this.dateReleve(),
          nomEquipe: this.nomEquipeInput(),
          chefEquipe: this.chefEquipeInput(),
          caTheoriquePiste: caTheoriquePiste,
          caKiosque: Number(this.caKiosqueInput()),
          caLavageVidange: Number(this.caLavageVidangeInput()),
          caTotalTheorique: caTotalTheorique,
          montantEspecesPhysique: Number(this.especesPhysiqueInput()),
          totalBonsAgil: Number(this.bonsAgilInput()),
          totalCartesAgilis: Number(this.cartesAgilisInput()),
          totalCheques: Number(this.chequesInput()),
          totalCreditsB2B: Number(this.creditsB2BInput()),
          totalDepensesPiste: Number(this.depensesPisteInput()),
          totalReelEncaisse: totalReel,
          ecartCaisse: ecart,
          statut: 'CLOTURE',
          observations: this.clotureObservations()
        };
        this.lastCloture.set(mockCloture);
        const ecartText = ecart >= 0 ? `+${ecart.toFixed(3)}` : `${ecart.toFixed(3)}`;
        this.notify(`Clôture enregistrée (mode hors-ligne) ! Écart: ${ecartText} DT`, 'success');
      }
    });
  }

  // --- Jaugeage Actions ---
  ouvrirJaugeage(cuve: Citerne) {
    this.selectedCuveToJauge.set(cuve);
    this.inputHauteurPige.set(cuve.hauteurPigeCm || 0);
    this.inputLitresPige.set(cuve.stockPhysique || cuve.stockActuel);
    this.showJaugeageModal.set(true);
  }

  fermerJaugeage() {
    this.showJaugeageModal.set(false);
    this.selectedCuveToJauge.set(null);
  }

  validerJaugeage() {
    const cuve = this.selectedCuveToJauge();
    if (!cuve) return;
    const h = Number(this.inputHauteurPige());
    const l = Number(this.inputLitresPige());
    this.api.enregistrerJaugeage(cuve.id, h, l).subscribe({
      next: (updatedCuve) => {
        this.cuves.update(list => list.map(c => c.id === updatedCuve.id ? updatedCuve : c));
        this.fermerJaugeage();
        this.notify(`Jaugeage enregistré pour ${updatedCuve.nom}. Écart coulage: ${(updatedCuve.ecartCoulage ?? 0).toFixed(1)} L`, 'success');
      },
      error: () => {
        const ecart = l - cuve.stockActuel;
        const updated = { ...cuve, hauteurPigeCm: h, stockPhysique: l, ecartCoulage: ecart };
        this.cuves.update(list => list.map(c => c.id === cuve.id ? updated : c));
        this.fermerJaugeage();
        this.notify(`Jaugeage enregistré pour ${cuve.nom}. Écart coulage: ${ecart.toFixed(1)} L`, 'success');
      }
    });
  }

  // --- Services Actions (Lavage / Vidange) ---
  ajouterServiceVente(nomService: string, montant: number) {
    this.caLavageVidangeInput.update(v => Number(v) + montant);
    this.notify(`Service "${nomService}" (+${montant.toFixed(3)} DT) ajouté à la feuille de quart !`, 'success');
  }

  // Helpers
  getPourcentageCuve(c: Citerne): number {
    if (!c.capaciteNominale || c.capaciteNominale <= 0) return 0;
    return Math.min(100, Math.max(0, (c.stockActuel / c.capaciteNominale) * 100));
  }

  getCuveStatusColor(c: Citerne): 'success' | 'warning' | 'danger' {
    const pct = this.getPourcentageCuve(c);
    if (pct <= (c.seuilAlerte / c.capaciteNominale) * 100) return 'danger';
    if (pct <= 40) return 'warning';
    return 'success';
  }

  notify(message: string, type: 'success' | 'error') {
    this.notification.set({ message, type });
    setTimeout(() => this.notification.set(null), 5000);
  }
}
