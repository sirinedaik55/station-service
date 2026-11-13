package com.station.api.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReleveDailyBatchDto {
    private LocalDate dateReleve;
    private List<ReleveInputDto> releves;
}
