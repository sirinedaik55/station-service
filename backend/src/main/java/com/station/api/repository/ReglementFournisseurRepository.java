package com.station.api.repository;

import com.station.api.model.ReglementFournisseur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReglementFournisseurRepository extends JpaRepository<ReglementFournisseur, Long> {
    List<ReglementFournisseur> findByFournisseurId(Long fournisseurId);
}
