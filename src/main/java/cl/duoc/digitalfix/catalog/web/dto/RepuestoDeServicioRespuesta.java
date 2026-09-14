package cl.duoc.digitalfix.catalog.web.dto;

public record RepuestoDeServicioRespuesta(Long repuestoId, String sku, String nombre,
                                          int cantidad, int stockDisponible) { }
