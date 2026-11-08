package com.duoc.cloud.repository;

import com.duoc.cloud.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findByUsuarioId(String usuarioId);
    List<Ticket> findByEventoId(String eventoId);
    List<Ticket> findByEventoIdAndUsuarioId(String eventoId, String usuarioId);

    Optional<Ticket> findByCodigoTicket(String codigoTicket);

    long countByEventoId(String eventoId);
}