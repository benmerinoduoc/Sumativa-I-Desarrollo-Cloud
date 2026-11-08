package com.duoc.cloud.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TicketRequestDTO {

    @NotBlank
    private String codigoTicket;

    @NotBlank
    private String eventoId;

    @NotBlank
    private String usuarioId;

    @NotBlank
    private String nombreAsistente;

    @NotNull
    private Double precio;

    private String estado;
}