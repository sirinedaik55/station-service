package com.station.api.repository;

import com.station.api.model.ReleveIndex;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReleveIndexRepository extends JpaRepository<ReleveIndex, Long> {
    List<ReleveIndex> findByDateReleve(LocalDate dateReleve);
    List<ReleveIndex> findByPistoletIdOrderByDateReleveDesc(Long pistoletId);
}
