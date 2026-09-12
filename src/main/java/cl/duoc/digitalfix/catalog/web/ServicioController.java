package cl.duoc.digitalfix.catalog.web;

import cl.duoc.digitalfix.catalog.service.ServicioService;
import cl.duoc.digitalfix.catalog.web.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/catalog/services")
@Validated
public class ServicioController {

    private final ServicioService servicios;

    public ServicioController(ServicioService servicios) {
        this.servicios = servicios;
    }

    @GetMapping
    public Page<ServicioRespuesta> listar(@PageableDefault(size = 20, sort = "nombre") Pageable pagina) {
        return servicios.listar(pagina).map(ServicioRespuesta::de);
    }

    @GetMapping("/{id}")
    public ServicioRespuesta obtener(@PathVariable Long id) {
        return ServicioRespuesta.de(servicios.obtener(id));
    }

}
