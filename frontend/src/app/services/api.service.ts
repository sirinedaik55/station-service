import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  Citerne, Pistolet, Produit, ReleveDailyBatch, ReleveIndex,
  Client, BonCarburantInput, BonCarburant, FactureClient,
  Fournisseur, LivraisonInput, BordereauLivraison,
  ClotureDailyInput, ClotureJournee
} from '../models/station.models';

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private http = inject(HttpClient);
  private baseUrl = 'http://localhost:8080/api/v1';

  // Cuves (Citernes)
  getCuves(): Observable<Citerne[]> {
    return this.http.get<Citerne[]>(`${this.baseUrl}/cuves`);
  }

  saveCuve(cuve: Partial<Citerne>): Observable<Citerne> {
    return this.http.post<Citerne>(`${this.baseUrl}/cuves`, cuve);
  }

  enregistrerJaugeage(citerneId: number, hauteurPigeCm: number, stockPhysique: number): Observable<Citerne> {
    return this.http.post<Citerne>(`${this.baseUrl}/cuves/${citerneId}/jaugeage?hauteurPigeCm=${hauteurPigeCm}&stockPhysique=${stockPhysique}`, {});
  }

  // Pistolets
  getPistolets(): Observable<Pistolet[]> {
    return this.http.get<Pistolet[]>(`${this.baseUrl}/pistolets`);
  }

  savePistolet(pistolet: Partial<Pistolet>): Observable<Pistolet> {
    return this.http.post<Pistolet>(`${this.baseUrl}/pistolets`, pistolet);
  }

  // Produits
  getProduits(): Observable<Produit[]> {
    return this.http.get<Produit[]>(`${this.baseUrl}/produits`);
  }

  saveProduit(produit: Partial<Produit>): Observable<Produit> {
    return this.http.post<Produit>(`${this.baseUrl}/produits`, produit);
  }

  // Relevés Quotidiens
  enregistrerReleves(batch: ReleveDailyBatch): Observable<ReleveIndex[]> {
    return this.http.post<ReleveIndex[]>(`${this.baseUrl}/releves`, batch);
  }

  getReleves(date?: string): Observable<ReleveIndex[]> {
    const url = date ? `${this.baseUrl}/releves?date=${date}` : `${this.baseUrl}/releves`;
    return this.http.get<ReleveIndex[]>(url);
  }

  // Clients B2B & Bons
  getClients(): Observable<Client[]> {
    return this.http.get<Client[]>(`${this.baseUrl}/clients`);
  }

  saveClient(client: Client): Observable<Client> {
    return this.http.post<Client>(`${this.baseUrl}/clients`, client);
  }

  enregistrerBon(bon: BonCarburantInput): Observable<BonCarburant> {
    return this.http.post<BonCarburant>(`${this.baseUrl}/bons`, bon);
  }

  getBonsParClient(clientId: number): Observable<BonCarburant[]> {
    return this.http.get<BonCarburant[]>(`${this.baseUrl}/bons/client/${clientId}`);
  }

  // Factures
  getFactures(): Observable<FactureClient[]> {
    return this.http.get<FactureClient[]>(`${this.baseUrl}/factures`);
  }

  genererFacture(clientId: number): Observable<FactureClient> {
    return this.http.post<FactureClient>(`${this.baseUrl}/factures/generer/${clientId}`, {});
  }

  // Livraisons & Fournisseurs
  getFournisseurs(): Observable<Fournisseur[]> {
    return this.http.get<Fournisseur[]>(`${this.baseUrl}/fournisseurs`);
  }

  saveFournisseur(f: Fournisseur): Observable<Fournisseur> {
    return this.http.post<Fournisseur>(`${this.baseUrl}/fournisseurs`, f);
  }

  enregistrerReglementFournisseur(r: any): Observable<any> {
    return this.http.post<any>(`${this.baseUrl}/reglements-fournisseur`, r);
  }

  getLivraisons(): Observable<BordereauLivraison[]> {
    return this.http.get<BordereauLivraison[]>(`${this.baseUrl}/livraisons`);
  }

  enregistrerLivraison(livraison: LivraisonInput): Observable<BordereauLivraison> {
    return this.http.post<BordereauLivraison>(`${this.baseUrl}/livraisons`, livraison);
  }

  // Clôture
  cloturerJournee(cloture: ClotureDailyInput): Observable<ClotureJournee> {
    return this.http.post<ClotureJournee>(`${this.baseUrl}/clotures`, cloture);
  }
}
