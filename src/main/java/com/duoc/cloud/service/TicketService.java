package com.duoc.cloud.service;

import com.duoc.cloud.dto.EstadisticasResponseDTO;
import com.duoc.cloud.dto.TicketRequestDTO;
import com.duoc.cloud.dto.TicketResponseDTO;
import com.duoc.cloud.model.Ticket;
import com.duoc.cloud.repository.TicketRepository;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final EfsStorageService efsStorageService;
    private final S3StorageService s3StorageService;

    public TicketService(TicketRepository ticketRepository, EfsStorageService efsStorageService, S3StorageService s3StorageService) {
        this.ticketRepository = ticketRepository;
        this.efsStorageService = efsStorageService;
        this.s3StorageService = s3StorageService;
    }

    public TicketResponseDTO generarTicket(TicketRequestDTO dto) throws IOException {
        String s3Key = dto.getEventoId() + "/" + dto.getUsuarioId() + "/" + dto.getCodigoTicket() + ".pdf";

        Ticket ticket = Ticket.builder()
                .codigoTicket(dto.getCodigoTicket())
                .eventoId(dto.getEventoId())
                .usuarioId(dto.getUsuarioId())
                .nombreAsistente(dto.getNombreAsistente())
                .precio(dto.getPrecio())
                .estado(dto.getEstado() != null ? dto.getEstado() : "RESERVADO")
                .s3Key(s3Key)
                .build();

        String efsPath = efsStorageService.generarYGuardarTicketPdf(ticket);
        ticket.setEfsPath(efsPath);

        Ticket guardado = ticketRepository.save(ticket);
        return mapToDTO(guardado);
    }

    public TicketResponseDTO subirTicketAS3(Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado con id: " + id));

        File archivoLocal = new File(ticket.getEfsPath());
        if (!archivoLocal.exists()) {
            throw new RuntimeException("El archivo local en EFS no existe: " + ticket.getEfsPath());
        }

        String urlS3 = s3StorageService.subirArchivo(ticket.getS3Key(), archivoLocal);
        ticket.setS3Url(urlS3);

        Ticket actualizado = ticketRepository.save(ticket);
        return mapToDTO(actualizado);
    }

    public TicketResponseDTO modificarTicket(Long id, TicketRequestDTO dto) throws IOException {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado con id: " + id));

        ticket.setNombreAsistente(dto.getNombreAsistente());
        ticket.setPrecio(dto.getPrecio());
        if (dto.getEstado() != null) {
            ticket.setEstado(dto.getEstado());
        }

        String nuevoEfsPath = efsStorageService.generarYGuardarTicketPdf(ticket);
        ticket.setEfsPath(nuevoEfsPath);

        if (ticket.getS3Url() != null) {
            File archivoActualizado = new File(nuevoEfsPath);
            String urlS3 = s3StorageService.actualizarArchivo(ticket.getS3Key(), archivoActualizado);
            ticket.setS3Url(urlS3);
        }

        Ticket guardado = ticketRepository.save(ticket);
        return mapToDTO(guardado);
    }

    public byte[] descargarTicketPorS3(Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado con id: " + id));
        return s3StorageService.descargarArchivo(ticket.getS3Key());
    }

    public void eliminarTicket(Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado con id: " + id));

        if (ticket.getS3Url() != null) {
            s3StorageService.eliminarArchivo(ticket.getS3Key());
        }

        File archivoLocal = new File(ticket.getEfsPath());
        if (archivoLocal.exists()) {
            archivoLocal.delete();
        }

        ticketRepository.delete(ticket);
    }

    public List<TicketResponseDTO> consultarHistorial(String eventoId, String usuarioId) {
        List<Ticket> tickets;
        if (eventoId != null && usuarioId != null) {
            tickets = ticketRepository.findByEventoIdAndUsuarioId(eventoId, usuarioId);
        } else if (eventoId != null) {
            tickets = ticketRepository.findByEventoId(eventoId);
        } else if (usuarioId != null) {
            tickets = ticketRepository.findByUsuarioId(usuarioId);
        } else {
            tickets = ticketRepository.findAll();
        }
        return tickets.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public EstadisticasResponseDTO obtenerEstadisticas(String eventoId) {
        List<Ticket> tickets = ticketRepository.findByEventoId(eventoId);
        long totalGenerados = tickets.size();
        double totalRecaudado = tickets.stream().mapToDouble(Ticket::getPrecio).sum();

        return EstadisticasResponseDTO.builder()
                .eventoId(eventoId)
                .totalTicketsGenerados(totalGenerados)
                .totalRecaudado(totalRecaudado)
                .build();
    }

    private TicketResponseDTO mapToDTO(Ticket ticket) {
        return TicketResponseDTO.builder()
                .id(ticket.getId())
                .codigoTicket(ticket.getCodigoTicket())
                .eventoId(ticket.getEventoId())
                .usuarioId(ticket.getUsuarioId())
                .nombreAsistente(ticket.getNombreAsistente())
                .precio(ticket.getPrecio())
                .estado(ticket.getEstado())
                .efsPath(ticket.getEfsPath())
                .s3Key(ticket.getS3Key())
                .s3Url(ticket.getS3Url())
                .fechaCreacion(ticket.getFechaCreacion())
                .fechaActualizacion(ticket.getFechaActualizacion())
                .build();
    }
}