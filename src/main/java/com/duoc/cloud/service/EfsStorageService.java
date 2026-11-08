package com.duoc.cloud.service;

import com.duoc.cloud.model.Ticket;
import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@Service
public class EfsStorageService {

    @Value("${app.efs.base-path:./temp-efs}")
    private String efsBasePath;

    public String generarYGuardarTicketPdf(Ticket ticket) throws IOException {
        String carpetaDestinoPath = efsBasePath + File.separator + ticket.getEventoId() + File.separator + ticket.getUsuarioId();
        File carpetaDestino = new File(carpetaDestinoPath);
        if (!carpetaDestino.exists()) {
            carpetaDestino.mkdirs();
        }

        String nombreArchivo = ticket.getCodigoTicket() + ".pdf";
        File archivoPdf = new File(carpetaDestino, nombreArchivo);

        Document document = new Document();
        try (FileOutputStream fos = new FileOutputStream(archivoPdf)) {
            PdfWriter.getInstance(document, fos);
            document.open();
            document.add(new Paragraph("         TICKET DE ENTRADA OFICIAL         "));
            document.add(new Paragraph("Código Ticket: " + ticket.getCodigoTicket()));
            document.add(new Paragraph("Evento ID: " + ticket.getEventoId()));
            document.add(new Paragraph("Usuario ID: " + ticket.getUsuarioId()));
            document.add(new Paragraph("Asistente: " + ticket.getNombreAsistente()));
            document.add(new Paragraph("Precio: $" + ticket.getPrecio()));
            document.add(new Paragraph("Estado: " + ticket.getEstado()));
            document.add(new Paragraph("Fecha: " + ticket.getFechaCreacion()));
        } catch (Exception e) {
            throw new IOException("Error al escribir el archivo PDF en EFS", e);
        }

        return archivoPdf.getAbsolutePath();
    }
}