package com.station.api.repository;

import com.station.api.model.Pistolet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PistoletRepository extends JpaRepository<Pistolet, Long> {
    List<Pistolet> findByCiterneId(Long citerneId);
}
