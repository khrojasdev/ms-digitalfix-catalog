package cl.duoc.digitalfix.catalog.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Lo que catalog necesita del evento de orden que publica workorders por
 * ordenes.negocio.exchange. El resto de los campos se ignora.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record OrdenAsignadaEvento(
        String eventKey,
        String tipoEvento,
        Long ordenId,
        Long empresaId,
        Long servicioId,
        String estadoAnterior
) {}
