package com.duoc.cloud.controller;

import com.duoc.cloud.dto.EstadisticasResponseDTO;
import com.duoc.cloud.dto.TicketRequestDTO;
import com.duoc.cloud.dto.TicketResponseDTO;
import com.duoc.cloud.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping("/generar")
    public ResponseEntity<TicketResponseDTO> generarTicket(@Valid @RequestBody TicketRequestDTO requestDTO) throws IOException {
        TicketResponseDTO respuesta = ticketService.generarTicket(requestDTO);
        return new ResponseEntity<>(respuesta, HttpStatus.CREATED);
    }

    @PostMapping("/{id}/subir-s3")
    public ResponseEntity<TicketResponseDTO> subirAS3(@PathVariable Long id) {
        TicketResponseDTO respuesta = ticketService.subirTicketAS3(id);
        return ResponseEntity.ok(respuesta);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TicketResponseDTO> modificarTicket(@PathVariable Long id, @Valid @RequestBody TicketRequestDTO requestDTO) throws IOException {
        TicketResponseDTO respuesta = ticketService.modificarTicket(id, requestDTO);
        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/{id}/descargar-s3")
    public ResponseEntity<byte[]> descargarTicketS3(@PathVariable Long id) {
        byte[] pdfBytes = ticketService.descargarTicketPorS3(id);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "ticket_" + id + ".pdf");
        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarTicket(@PathVariable Long id) {
        ticketService.eliminarTicket(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/historial")
    public ResponseEntity<List<TicketResponseDTO>> consultarHistorial(
            @RequestParam(required = false) String eventoId,
            @RequestParam(required = false) String usuarioId) {
        List<TicketResponseDTO> historial = ticketService.consultarHistorial(eventoId, usuarioId);
        return ResponseEntity.ok(historial);
    }

    @GetMapping("/estadisticas/{eventoId}")
    public ResponseEntity<EstadisticasResponseDTO> obtenerEstadisticas(@PathVariable String eventoId) {
        EstadisticasResponseDTO estadisticas = ticketService.obtenerEstadisticas(eventoId);
        return ResponseEntity.ok(estadisticas);
    }
}