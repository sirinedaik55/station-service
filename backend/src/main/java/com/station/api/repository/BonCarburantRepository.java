package com.station.api.repository;

import com.station.api.model.BonCarburant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BonCarburantRepository extends JpaRepository<BonCarburant, Long> {
    List<BonCarburant> findByClientIdAndStatut(Long clientId, String statut);
    List<BonCarburant> findByClientId(Long clientId);
}
