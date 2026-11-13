package com.station.api.repository;

import com.station.api.model.Citerne;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CiterneRepository extends JpaRepository<Citerne, Long> {
    Optional<Citerne> findByCode(String code);
}
