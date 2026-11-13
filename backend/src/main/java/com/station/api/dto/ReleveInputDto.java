package com.station.api.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReleveInputDto {
    private Long pistoletId;
    private BigDecimal indexFin;
}
