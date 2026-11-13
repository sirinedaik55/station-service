-- Migration V2: Initial Seed Data for Station-Service Demo

-- Produits Carburants & Boutique
INSERT INTO produits (code, nom, type_produit, prix_unitaire, stock_kiosque, seuil_alerte) VALUES
('G01', 'Gazole Supérieur G01', 'CARBURANT', 2.150, 0, 0),
('G02', 'Gazole Normal G02', 'CARBURANT', 1.980, 0, 0),
('G050', 'Gazole 50 ppm G050', 'CARBURANT', 2.250, 0, 0),
('SSP1', 'Sans Plomb Premium SSP1', 'CARBURANT', 2.450, 0, 0),
('SSP2', 'Sans Plomb 95 SSP2', 'CARBURANT', 2.380, 0, 0),
('Pr7', 'Produit Spécial Pr7', 'CARBURANT', 2.600, 0, 0),
('OIL_5W40', 'Huile Moteur Synthétique 5W40 4L', 'HUILLE', 45.000, 25, 5),
('OIL_10W40', 'Huile Moteur 10W40 1L', 'HUILLE', 14.500, 40, 10);

-- Citernes (Dynamiques)
INSERT INTO citernes (code, nom, produit_id, capacite_nominale, stock_actuel, seuil_alerte) VALUES
('C_G01', 'Citerne G01 (30 000 L)', (SELECT id FROM produits WHERE code='G01'), 30000.00, 22500.00, 5000.00),
('C_G02', 'Citerne G02 (10 000 L)', (SELECT id FROM produits WHERE code='G02'), 10000.00, 6800.00, 2000.00),
('C_G050', 'Citerne G050 (30 000 L)', (SELECT id FROM produits WHERE code='G050'), 30000.00, 18200.00, 5000.00),
('C_SSP1', 'Citerne SSP1 (30 000 L)', (SELECT id FROM produits WHERE code='SSP1'), 30000.00, 24000.00, 5000.00),
('C_SSP2', 'Citerne SSP2 (10 000 L)', (SELECT id FROM produits WHERE code='SSP2'), 10000.00, 4500.00, 2000.00),
('C_Pr7', 'Citerne Pr7 (10 000 L)', (SELECT id FROM produits WHERE code='Pr7'), 10000.00, 8100.00, 2000.00);

-- Pistolets Rattachés aux Citernes
INSERT INTO pistolets (code, nom, citerne_id, dernier_index) VALUES
('P1_G01', 'Pistolet 1 - G01 Piste Nord', (SELECT id FROM citernes WHERE code='C_G01'), 154200.00),
('P2_G01', 'Pistolet 2 - G01 Piste Sud', (SELECT id FROM citernes WHERE code='C_G01'), 98500.00),
('P1_G02', 'Pistolet 1 - G02 Piste Nord', (SELECT id FROM citernes WHERE code='C_G02'), 45200.00),
('P1_G050', 'Pistolet 1 - G050 Piste Sud', (SELECT id FROM citernes WHERE code='C_G050'), 210400.00),
('P1_SSP1', 'Pistolet 1 - SSP1 Piste Nord', (SELECT id FROM citernes WHERE code='C_SSP1'), 189300.00),
('P1_SSP2', 'Pistolet 1 - SSP2 Piste Sud', (SELECT id FROM citernes WHERE code='C_SSP2'), 67100.00),
('P1_Pr7', 'Pistolet 1 - Pr7 Piste Spéciale', (SELECT id FROM citernes WHERE code='C_Pr7'), 32000.00);

-- Clients B2B de Démo
INSERT INTO clients (raison_sociale, telephone, email, plafond_credit, solde_du) VALUES
('Société de Transport Express', '+216 71 123 456', 'contact@transport-express.tn', 15000.000, 1250.000),
('BTP Construction SARL', '+216 72 987 654', 'compta@btp-construction.tn', 25000.000, 3800.500),
('Taxi Services & Co', '+216 73 555 444', 'contact@taxiservices.tn', 5000.000, 420.000);

-- Fournisseurs Pétroliers
INSERT INTO fournisseurs (nom_societe, telephone, adresse) VALUES
('Société Nationale Pétrolière (SNDP)', '+216 71 800 000', 'Avenue Habib Bourguiba, Tunis'),
('Shell Distribution', '+216 71 900 000', 'Zone Industrielle Rades');
