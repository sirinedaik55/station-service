-- Migration V3: Enrichissement Fonctionnalités Réelles Agil Tunisie

-- 1. Citernes: colonnes pour le jaugeage physique et calcul des coulages
ALTER TABLE citernes ADD COLUMN IF NOT EXISTS hauteur_pige_cm NUMERIC(8, 2) DEFAULT 0;
ALTER TABLE citernes ADD COLUMN IF NOT EXISTS stock_physique NUMERIC(12, 2) DEFAULT 0;
ALTER TABLE citernes ADD COLUMN IF NOT EXISTS ecart_coulage NUMERIC(12, 2) DEFAULT 0;

-- 2. Factures: timbre fiscal tunisien (1.000 DT)
ALTER TABLE factures_client ADD COLUMN IF NOT EXISTS timbre_fiscal NUMERIC(8, 3) DEFAULT 1.000;

-- 3. Clôtures de shift & feuille de vente par équipe
ALTER TABLE clotures_journee DROP CONSTRAINT IF EXISTS clotures_journee_date_cloture_key;
ALTER TABLE clotures_journee ADD COLUMN IF NOT EXISTS nom_equipe VARCHAR(50) DEFAULT 'Équipe 1 (Matin)';
ALTER TABLE clotures_journee ADD COLUMN IF NOT EXISTS chef_equipe VARCHAR(100) DEFAULT 'Chef de Piste';
ALTER TABLE clotures_journee ADD COLUMN IF NOT EXISTS ca_lavage_vidange NUMERIC(12, 3) DEFAULT 0;
ALTER TABLE clotures_journee ADD COLUMN IF NOT EXISTS total_bons_agil NUMERIC(12, 3) DEFAULT 0;
ALTER TABLE clotures_journee ADD COLUMN IF NOT EXISTS total_cartes_agilis NUMERIC(12, 3) DEFAULT 0;
ALTER TABLE clotures_journee ADD COLUMN IF NOT EXISTS total_cheques NUMERIC(12, 3) DEFAULT 0;
ALTER TABLE clotures_journee ADD COLUMN IF NOT EXISTS total_credits_b2b NUMERIC(12, 3) DEFAULT 0;
ALTER TABLE clotures_journee ADD COLUMN IF NOT EXISTS total_depenses_piste NUMERIC(12, 3) DEFAULT 0;
ALTER TABLE clotures_journee ADD COLUMN IF NOT EXISTS total_reel_encaisse NUMERIC(12, 3) DEFAULT 0;

-- 4. Insertion des vrais produits de la gamme officielle Agil (Huiles Tanix, Gaz, Lavage, Filtres)
INSERT INTO produits (code, nom, type_produit, prix_unitaire, stock_kiosque, seuil_alerte) VALUES
('GO50', 'Gasoil 50 Soufre Bas', 'CARBURANT', 2.250, 0, 0),
('SSP', 'Super Sans Plomb', 'CARBURANT', 2.450, 0, 0),
('PRIM', 'Gasoil / Super Prima', 'CARBURANT', 2.600, 0, 0),
('GO', 'Gasoil Ordinaire', 'CARBURANT', 1.980, 0, 0),
('PET', 'Pétrole Lampant', 'CARBURANT', 1.200, 0, 0),
('OIL_T500_1L', 'Huile Agil TANIX Super 500 1L', 'HUILES', 16.500, 35, 10),
('OIL_T500_4L', 'Huile Agil TANIX Super 500 4L', 'HUILES', 58.000, 20, 5),
('OIL_T300_5L', 'Huile Agil TANIX Diesel 300 SHD 40 5L', 'HUILES', 62.000, 18, 5),
('OIL_T700_1L', 'Huile Agil TANIX Diesel 700 1L', 'HUILES', 19.800, 25, 8),
('OIL_2T_1L', 'Huile Agil TANIX 2Temps Moto 1L', 'HUILES', 14.200, 30, 10),
('OIL_VITEX_1L', 'Huile Boîte Agil VITEX Special EP 85W90 1L', 'HUILES', 18.500, 20, 5),
('GAZ_B13', 'Bouteille Gaz Butane 13kg Agil', 'GAZ', 8.800, 80, 20),
('LAV_EXT', 'Prestation Lavage Jet Haute Pression', 'LAVAGES', 8.000, 1000, 0),
('LAV_COMPLET', 'Lavage Complet Mousse Intérieur/Extérieur', 'LAVAGES', 15.000, 1000, 0),
('FORFAIT_VIDANGE', 'Forfait Vidange & Main d''œuvre Baie', 'SERVICES', 12.000, 1000, 0),
('EAU_LAVE_GLACE', 'Liquide Lave-Glace Démoustiquant 5L', 'ACCESSOIRES', 7.500, 40, 10)
ON CONFLICT (code) DO UPDATE SET
  nom = EXCLUDED.nom,
  prix_unitaire = EXCLUDED.prix_unitaire,
  type_produit = EXCLUDED.type_produit;
