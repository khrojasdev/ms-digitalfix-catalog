package cl.duoc.digitalfix.catalog.web;

import cl.duoc.digitalfix.catalog.service.ServicioRepuestoService;
import cl.duoc.digitalfix.catalog.service.ServicioService;
import cl.duoc.digitalfix.catalog.web.dto.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/catalog/services")
@Validated
public class ServicioController {

    private final ServicioService servicios;
    private final ServicioRepuestoService relaciones;

    public ServicioController(ServicioService servicios, ServicioRepuestoService relaciones) {
        this.servicios = servicios;
        this.relaciones = relaciones;
    }

    @GetMapping
    public Page<ServicioRespuesta> listar(
            @RequestParam(required = false, defaultValue = "true") Boolean soloActivos,
            @PageableDefault(size = 20, sort = "nombre") Pageable pagina) {
        return servicios.listar(soloActivos, pagina).map(ServicioRespuesta::de);
    }

    @GetMapping("/{id}")
    public ServicioRespuesta obtener(@PathVariable Long id) {
        return ServicioRespuesta.de(servicios.obtener(id));
    }

    @PostMapping
    public ResponseEntity<ServicioRespuesta> crear(@Valid @RequestBody ServicioSolicitud solicitud) {
        ServicioRespuesta creado = ServicioRespuesta.de(servicios.crear(solicitud));
        return ResponseEntity.created(URI.create("/api/catalog/services/" + creado.id())).body(creado);
    }

    @PutMapping("/{id}")
    public ServicioRespuesta actualizar(@PathVariable Long id,
                                        @Valid @RequestBody ServicioSolicitud solicitud) {
        return ServicioRespuesta.de(servicios.actualizar(id, solicitud));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desactivar(@PathVariable Long id) {
        servicios.desactivar(id);
    }

    @PutMapping("/{id}/parts")
    public List<RepuestoDeServicioRespuesta> asociarRepuestos(
            @PathVariable Long id,
            @RequestBody @NotEmpty(message = "la lista de repuestos no puede estar vacia")
            List<@Valid RepuestoDeServicio> repuestos) {
        return relaciones.asociar(id, repuestos);
    }
}
