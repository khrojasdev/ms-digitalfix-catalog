package cl.duoc.digitalfix.catalog.web;

import cl.duoc.digitalfix.catalog.service.RepuestoService;
import cl.duoc.digitalfix.catalog.web.dto.RepuestoRespuesta;
import cl.duoc.digitalfix.catalog.web.dto.RepuestoSolicitud;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/catalog/parts")
public class RepuestoController {

    private final RepuestoService repuestos;

    public RepuestoController(RepuestoService repuestos) { this.repuestos = repuestos; }

    @GetMapping
    public Page<RepuestoRespuesta> listar(@PageableDefault(size = 20, sort = "sku") Pageable pagina) {
        return repuestos.listar(pagina).map(RepuestoRespuesta::de);
    }

    /** Va antes de /{id} a proposito: si no, "low-stock" se tomaria como un id. */
    @GetMapping("/low-stock")
    public List<RepuestoRespuesta> bajoMinimo() {
        return repuestos.bajoMinimo().stream().map(RepuestoRespuesta::de).toList();
    }

    @GetMapping("/{id}")
    public RepuestoRespuesta obtener(@PathVariable Long id) {
        return RepuestoRespuesta.de(repuestos.obtener(id));
    }

    @PostMapping
    public ResponseEntity<RepuestoRespuesta> crear(@Valid @RequestBody RepuestoSolicitud solicitud) {
        RepuestoRespuesta creado = RepuestoRespuesta.de(repuestos.crear(solicitud));
        return ResponseEntity.created(URI.create("/api/catalog/parts/" + creado.id())).body(creado);
    }

    @PutMapping("/{id}")
    public RepuestoRespuesta actualizar(@PathVariable Long id,
                                        @Valid @RequestBody RepuestoSolicitud solicitud) {
        return RepuestoRespuesta.de(repuestos.actualizar(id, solicitud));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desactivar(@PathVariable Long id) {
        repuestos.desactivar(id);
    }
}
