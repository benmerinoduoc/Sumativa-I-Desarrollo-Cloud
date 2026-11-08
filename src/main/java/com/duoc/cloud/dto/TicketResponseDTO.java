package com.duoc.cloud.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TicketResponseDTO {

    private Long id;
    private String codigoTicket;
    private String eventoId;
    private String usuarioId;
    private String nombreAsistente;
    private Double precio;
    private String estado;
    private String efsPath;
    private String s3Key;
    private String s3Url;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}