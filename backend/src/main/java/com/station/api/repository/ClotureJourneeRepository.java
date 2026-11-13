package com.station.api.repository;

import com.station.api.model.ClotureJournee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface ClotureJourneeRepository extends JpaRepository<ClotureJournee, Long> {
    Optional<ClotureJournee> findByDateCloture(LocalDate dateCloture);
}
