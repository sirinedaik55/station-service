-- Migration V1: Full Schema for Station-Service Management

CREATE TABLE IF NOT EXISTS produits (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL,
    nom VARCHAR(100) NOT NULL,
    type_produit VARCHAR(30) NOT NULL, -- CARBURANT, HUILLE, ACCESSOR, BOISSON
    prix_unitaire NUMERIC(12, 3) NOT NULL,
    stock_kiosque NUMERIC(12, 2) DEFAULT 0,
    seuil_alerte NUMERIC(12, 2) DEFAULT 10
);

CREATE TABLE IF NOT EXISTS citernes (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL,
    nom VARCHAR(100) NOT NULL,
    produit_id BIGINT NOT NULL REFERENCES produits(id) ON DELETE CASCADE,
    capacite_nominale NUMERIC(12, 2) NOT NULL, -- ex: 10000.00, 30000.00
    stock_actuel NUMERIC(12, 2) NOT NULL,
    seuil_alerte NUMERIC(12, 2) NOT NULL DEFAULT 3000.00
);

CREATE TABLE IF NOT EXISTS pistolets (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL,
    nom VARCHAR(100) NOT NULL,
    citerne_id BIGINT NOT NULL REFERENCES citernes(id) ON DELETE CASCADE,
    dernier_index NUMERIC(12, 2) NOT NULL DEFAULT 0.00
);

CREATE TABLE IF NOT EXISTS releves_index (
    id BIGSERIAL PRIMARY KEY,
    date_releve DATE NOT NULL,
    pistolet_id BIGINT NOT NULL REFERENCES pistolets(id),
    index_depart NUMERIC(12, 2) NOT NULL,
    index_fin NUMERIC(12, 2) NOT NULL,
    volume_vendu NUMERIC(12, 2) NOT NULL,
    prix_unitaire NUMERIC(12, 3) NOT NULL,
    montant_total NUMERIC(12, 3) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS clotures_journee (
    id BIGSERIAL PRIMARY KEY,
    date_cloture DATE UNIQUE NOT NULL,
    ca_theorique_piste NUMERIC(12, 3) NOT NULL DEFAULT 0,
    ca_kiosque NUMERIC(12, 3) NOT NULL DEFAULT 0,
    ca_total_theorique NUMERIC(12, 3) NOT NULL DEFAULT 0,
    montant_especes_physique NUMERIC(12, 3) NOT NULL DEFAULT 0,
    ecart_caisse NUMERIC(12, 3) NOT NULL DEFAULT 0,
    statut VARCHAR(30) NOT NULL DEFAULT 'CLOTURE', -- BROUILLON, CLOTURE
    observations TEXT
);

CREATE TABLE IF NOT EXISTS clients (
    id BIGSERIAL PRIMARY KEY,
    raison_sociale VARCHAR(150) NOT NULL,
    telephone VARCHAR(50),
    email VARCHAR(100),
    plafond_credit NUMERIC(12, 3) DEFAULT 10000.000,
    solde_du NUMERIC(12, 3) DEFAULT 0.000
);

CREATE TABLE IF NOT EXISTS factures_client (
    id BIGSERIAL PRIMARY KEY,
    numero_facture VARCHAR(50) UNIQUE NOT NULL,
    date_facture DATE NOT NULL,
    client_id BIGINT NOT NULL REFERENCES clients(id),
    montant_ht NUMERIC(12, 3) NOT NULL,
    montant_ttc NUMERIC(12, 3) NOT NULL,
    montant_paye NUMERIC(12, 3) DEFAULT 0,
    statut VARCHAR(30) NOT NULL DEFAULT 'IMPAYEE', -- IMPAYEE, PARTIELLE, PAYEE
    date_echeance DATE
);

CREATE TABLE IF NOT EXISTS bons_carburant (
    id BIGSERIAL PRIMARY KEY,
    numero_bon VARCHAR(50) NOT NULL,
    date_emission DATE NOT NULL,
    client_id BIGINT NOT NULL REFERENCES clients(id),
    produit_id BIGINT NOT NULL REFERENCES produits(id),
    volume NUMERIC(12, 2) NOT NULL,
    prix_unitaire NUMERIC(12, 3) NOT NULL,
    montant NUMERIC(12, 3) NOT NULL,
    mode_reglement VARCHAR(50) DEFAULT 'CARTE_BON', -- BON_VALEUR, CARTE_AGELIS, ESPECE, CHÈQUE
    statut VARCHAR(30) NOT NULL DEFAULT 'NON_FACTURE', -- NON_FACTURE, FACTURE
    facture_id BIGINT REFERENCES factures_client(id) SET NULL
);

CREATE TABLE IF NOT EXISTS fournisseurs (
    id BIGSERIAL PRIMARY KEY,
    nom_societe VARCHAR(150) NOT NULL,
    telephone VARCHAR(50),
    adresse TEXT,
    solde_du NUMERIC(12, 3) DEFAULT 0.000
);

CREATE TABLE IF NOT EXISTS bordereaux_livraison (
    id BIGSERIAL PRIMARY KEY,
    numero_bordereau VARCHAR(50) NOT NULL,
    date_livraison DATE NOT NULL,
    fournisseur_id BIGINT NOT NULL REFERENCES fournisseurs(id),
    citerne_id BIGINT NOT NULL REFERENCES citernes(id),
    volume_livre NUMERIC(12, 2) NOT NULL,
    prix_unitaire NUMERIC(12, 3) NOT NULL,
    montant_total NUMERIC(12, 3) NOT NULL,
    observations TEXT
);

CREATE TABLE IF NOT EXISTS factures_fournisseur (
    id BIGSERIAL PRIMARY KEY,
    numero_facture VARCHAR(50) UNIQUE NOT NULL,
    date_facture DATE NOT NULL,
    fournisseur_id BIGINT NOT NULL REFERENCES fournisseurs(id),
    bordereau_id BIGINT REFERENCES bordereaux_livraison(id) SET NULL,
    montant_total NUMERIC(12, 3) NOT NULL,
    montant_paye NUMERIC(12, 3) DEFAULT 0.000,
    statut VARCHAR(30) NOT NULL DEFAULT 'IMPAYEE', -- IMPAYEE, PARTIELLE, PAYEE
    date_echeance DATE
);

CREATE TABLE IF NOT EXISTS reglements_fournisseur (
    id BIGSERIAL PRIMARY KEY,
    date_reglement DATE NOT NULL,
    fournisseur_id BIGINT NOT NULL REFERENCES fournisseurs(id),
    facture_id BIGINT REFERENCES factures_fournisseur(id) SET NULL,
    montant NUMERIC(12, 3) NOT NULL,
    mode_reglement VARCHAR(50) NOT NULL, -- VIREMENT, CHÈQUE, ESPÈCES
    reference VARCHAR(100)
);

