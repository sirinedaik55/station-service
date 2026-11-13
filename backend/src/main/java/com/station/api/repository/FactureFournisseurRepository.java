package com.station.api.repository;

import com.station.api.model.FactureFournisseur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FactureFournisseurRepository extends JpaRepository<FactureFournisseur, Long> {
    List<FactureFournisseur> findByFournisseurId(Long fournisseurId);
}
