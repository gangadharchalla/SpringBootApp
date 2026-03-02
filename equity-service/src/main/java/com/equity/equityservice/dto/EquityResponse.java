package com.equity.equityservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EquityResponse {

    private Long id;
    private String equityName;
    private String symbol;
    private Double price;
    private Integer quantity;
}
