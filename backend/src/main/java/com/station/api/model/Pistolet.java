package com.station.api.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "pistolets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pistolet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 100)
    private String nom;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "citerne_id", nullable = false)
    private Citerne citerne;

    @Column(name = "dernier_index", nullable = false, precision = 12, scale = 2)
    private BigDecimal dernierIndex;
}
