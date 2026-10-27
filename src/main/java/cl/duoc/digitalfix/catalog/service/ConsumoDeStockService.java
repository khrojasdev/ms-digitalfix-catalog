package cl.duoc.digitalfix.catalog.service;

import cl.duoc.digitalfix.catalog.domain.ConsumoDeOrden;
import cl.duoc.digitalfix.catalog.domain.ConsumoDeOrden.Resultado;
import cl.duoc.digitalfix.catalog.domain.Repuesto;
import cl.duoc.digitalfix.catalog.domain.ServicioRepuesto;
import cl.duoc.digitalfix.catalog.mensajeria.MensajeInvalido;
import cl.duoc.digitalfix.catalog.repository.ConsumoDeOrdenRepository;
import cl.duoc.digitalfix.catalog.repository.RepuestoRepository;
import cl.duoc.digitalfix.catalog.repository.ServicioRepository;
import cl.duoc.digitalfix.catalog.repository.ServicioRepuestoRepository;
import cl.duoc.digitalfix.catalog.service.dto.OrdenAsignadaEvento;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Regla del caso: al asignar la orden se descuenta el stock de los repuestos
 * que consume su servicio (SERVICE_PART).
 *
 * - Todo o nada: si a un repuesto no le alcanza, no se descuenta ninguno y se
 *   registra STOCK_INSUFICIENTE (la orden ya esta asignada en workorders; el
 *   supervisor ve la alerta y repone).
 * - Una vez por orden: si la orden ya tiene registro, el evento se ignora.
 * - Una reasignacion (estadoAnterior = ASIGNADA) no vuelve a descontar.
 * - Despues de descontar se avisa por cada repuesto que quede bajo su minimo.
 */
@Service
public class ConsumoDeStockService {

    private static final Logger log = LoggerFactory.getLogger(ConsumoDeStockService.class);

    private final ConsumoDeOrdenRepository consumos;
    private final ServicioRepository servicios;
    private final ServicioRepuestoRepository serviciosRepuestos;
    private final RepuestoRepository repuestos;
    private final StockAlertaPublisher alertas;

    public ConsumoDeStockService(ConsumoDeOrdenRepository consumos, ServicioRepository servicios,
                                 ServicioRepuestoRepository serviciosRepuestos, RepuestoRepository repuestos,
                                 StockAlertaPublisher alertas) {
        this.consumos = consumos;
        this.servicios = servicios;
        this.serviciosRepuestos = serviciosRepuestos;
        this.repuestos = repuestos;
        this.alertas = alertas;
    }

    /**
     * @return el registro creado, o vacio si el evento no corresponde (otro
     *         tipo, reasignacion u orden ya procesada)
     */
    @Transactional
    public Optional<ConsumoDeOrden> alAsignar(OrdenAsignadaEvento evento) {
        if (!"ASIGNADA".equalsIgnoreCase(evento.tipoEvento())) {
            return Optional.empty();
        }
        if ("ASIGNADA".equalsIgnoreCase(evento.estadoAnterior())) {
            log.info("Orden {} reasignada: el stock ya se desconto en la primera asignacion.", evento.ordenId());
            return Optional.empty();
        }
        if (evento.ordenId() == null || evento.empresaId() == null) {
            throw new MensajeInvalido("el evento ASIGNADA no trae ordenId o empresaId");
        }
        if (consumos.existsByCompanyIdAndOrdenId(evento.empresaId(), evento.ordenId())) {
            log.info("Orden {} ya tiene su descuento de stock registrado; evento {} ignorado.",
                    evento.ordenId(), evento.eventKey());
            return Optional.empty();
        }

        ConsumoDeOrden consumo = descontar(evento);
        consumo = consumos.save(consumo);
        log.info("Orden {} (servicio {}): {} {}", evento.ordenId(), evento.servicioId(),
                consumo.getResultado(), consumo.getDetalle() == null ? "" : consumo.getDetalle());
        return Optional.of(consumo);
    }

    private ConsumoDeOrden descontar(OrdenAsignadaEvento evento) {
        Long empresa = evento.empresaId();
        Long servicioId = evento.servicioId();

        if (servicioId == null || servicios.findByIdAndCompanyId(servicioId, empresa).isEmpty()) {
            return registro(evento, Resultado.SERVICIO_NO_ENCONTRADO,
                    "el servicio " + servicioId + " no existe en la empresa " + empresa);
        }

        List<ServicioRepuesto> receta = serviciosRepuestos.findByIdServicioId(servicioId);
        if (receta.isEmpty()) {
            return registro(evento, Resultado.SIN_REPUESTOS, "el servicio no tiene repuestos asociados");
        }

        // Primero se bloquean y revisan todos; solo si alcanza para todos se descuenta.
        List<Repuesto> bloqueados = new ArrayList<>();
        List<String> faltantes = new ArrayList<>();
        for (ServicioRepuesto linea : receta) {
            Optional<Repuesto> repuesto = repuestos.bloquearParaDescontar(linea.getRepuestoId(), empresa);
            if (repuesto.isEmpty()) {
                faltantes.add("repuesto " + linea.getRepuestoId() + " no existe");
                continue;
            }
            Repuesto r = repuesto.get();
            bloqueados.add(r);
            if (r.getStock() < linea.getCantidad()) {
                faltantes.add(r.getSku() + " (hay " + r.getStock() + ", se piden " + linea.getCantidad() + ")");
            }
        }

        if (!faltantes.isEmpty()) {
            bloqueados.forEach(alertas::publicarSiBajoMinimo);
            return registro(evento, Resultado.STOCK_INSUFICIENTE, "no alcanza: " + String.join(", ", faltantes));
        }

        List<String> detalle = new ArrayList<>();
        for (int i = 0; i < receta.size(); i++) {
            ServicioRepuesto linea = receta.get(i);
            Repuesto r = bloqueados.get(i);
            r.descontar(linea.getCantidad());
            repuestos.save(r);
            detalle.add(r.getSku() + " -" + linea.getCantidad() + " (queda " + r.getStock() + ")");
        }
        bloqueados.forEach(alertas::publicarSiBajoMinimo);
        return registro(evento, Resultado.DESCONTADO, String.join(", ", detalle));
    }

    private static ConsumoDeOrden registro(OrdenAsignadaEvento evento, Resultado resultado, String detalle) {
        return new ConsumoDeOrden(evento.empresaId(), evento.ordenId(), evento.servicioId(),
                evento.eventKey() == null ? "sin-clave" : evento.eventKey(), resultado, detalle);
    }
}
