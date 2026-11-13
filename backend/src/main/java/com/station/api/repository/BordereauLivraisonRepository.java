package com.station.api.repository;

import com.station.api.model.BordereauLivraison;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BordereauLivraisonRepository extends JpaRepository<BordereauLivraison, Long> {
    List<BordereauLivraison> findByCiterneId(Long citerneId);
}
