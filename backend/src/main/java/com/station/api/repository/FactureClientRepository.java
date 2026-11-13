package com.station.api.repository;

import com.station.api.model.FactureClient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FactureClientRepository extends JpaRepository<FactureClient, Long> {
    List<FactureClient> findByClientId(Long clientId);
}
