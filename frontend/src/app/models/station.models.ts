export interface Produit {
  id: number;
  code: string;
  nom: string;
  typeProduit: string; // CARBURANT, HUILLE, ACCESSOR, BOISSON
  prixUnitaire: number;
  stockKiosque?: number;
  seuilAlerte?: number;
}

export interface Citerne {
  id: number;
  code: string;
  nom: string;
  produit: Produit;
  capaciteNominale: number;
  stockActuel: number;
  seuilAlerte: number;
  hauteurPigeCm?: number;
  stockPhysique?: number;
  ecartCoulage?: number;
}

export interface Pistolet {
  id: number;
  code: string;
  nom: string;
  citerne: Citerne;
  dernierIndex: number;
}

export interface ReleveInput {
  pistoletId: number;
  indexFin: number;
}

export interface ReleveDailyBatch {
  dateReleve?: string;
  releves: ReleveInput[];
}

export interface ReleveIndex {
  id: number;
  dateReleve: string;
  pistolet: Pistolet;
  indexDepart: number;
  indexFin: number;
  volumeVendu: number;
  prixUnitaire: number;
  montantTotal: number;
}

export interface Client {
  id?: number;
  raisonSociale: string;
  telephone?: string;
  email?: string;
  plafondCredit?: number;
  soldeDu?: number;
}

export interface BonCarburantInput {
  numeroBon: string;
  dateEmission?: string;
  clientId: number;
  produitId: number;
  volume: number;
  modeReglement?: string;
}

export interface BonCarburant {
  id: number;
  numeroBon: string;
  dateEmission: string;
  client: Client;
  produit: Produit;
  volume: number;
  prixUnitaire: number;
  montant: number;
  modeReglement: string;
  statut: string;
  facture?: FactureClient;
}

export interface FactureClient {
  id: number;
  numeroFacture: string;
  dateFacture: string;
  client: Client;
  montantHt: number;
  timbreFiscal?: number;
  montantTtc: number;
  montantPaye: number;
  statut: string;
  dateEcheance: string;
}

export interface Fournisseur {
  id?: number;
  nomSociete: string;
  telephone?: string;
  adresse?: string;
  soldeDu?: number;
}

export interface ReglementFournisseur {
  id?: number;
  dateReglement?: string;
  fournisseur: Fournisseur;
  montant: number;
  modeReglement: string; // VIREMENT, CHÈQUE, ESPÈCES
  reference?: string;
}


export interface LivraisonInput {
  numeroBordereau: string;
  dateLivraison?: string;
  fournisseurId: number;
  citerneId: number;
  volumeLivre: number;
  prixUnitaire: number;
  observations?: string;
}

export interface BordereauLivraison {
  id: number;
  numeroBordereau: string;
  dateLivraison: string;
  fournisseur: Fournisseur;
  citerne: Citerne;
  volumeLivre: number;
  prixUnitaire: number;
  montantTotal: number;
  observations?: string;
}

export interface ClotureDailyInput {
  dateCloture?: string;
  nomEquipe?: string;
  chefEquipe?: string;
  caKiosque?: number;
  caLavageVidange?: number;
  montantEspecesPhysique: number;
  totalBonsAgil?: number;
  totalCartesAgilis?: number;
  totalCheques?: number;
  totalCreditsB2B?: number;
  totalDepensesPiste?: number;
  observations?: string;
}

export interface ClotureJournee {
  id: number;
  dateCloture: string;
  nomEquipe?: string;
  chefEquipe?: string;
  caTheoriquePiste: number;
  caKiosque: number;
  caLavageVidange?: number;
  caTotalTheorique: number;
  montantEspecesPhysique: number;
  totalBonsAgil?: number;
  totalCartesAgilis?: number;
  totalCheques?: number;
  totalCreditsB2B?: number;
  totalDepensesPiste?: number;
  totalReelEncaisse?: number;
  ecartCaisse: number;
  statut: string;
  observations?: string;
}
