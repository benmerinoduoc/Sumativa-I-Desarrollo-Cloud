package com.duoc.cloud.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EstadisticasResponseDTO {

    private String eventoId;
    private long totalTicketsGenerados;
    private double totalRecaudado;
}