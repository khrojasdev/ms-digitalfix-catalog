package cl.duoc.digitalfix.catalog.domain;

import cl.duoc.digitalfix.catalog.error.StockInsuficiente;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

class RepuestoTest {

    private Repuesto repuesto(int stock, int minimo) {
        return new Repuesto(1L, "SKU-1", "Breaker 20A", stock, minimo, new BigDecimal("1990.00"));
    }

    @Test
    @DisplayName("descontar baja el stock cuando alcanza")
    void descuentaCuandoAlcanza() {
        Repuesto r = repuesto(10, 2);
        r.descontar(4);
        assertThat(r.getStock()).isEqualTo(6);
    }

    @Test
    @DisplayName("descontar mas de lo que hay falla y no deja el stock bajo cero")
    void noDejaStockNegativo() {
        Repuesto r = repuesto(3, 1);
        assertThatThrownBy(() -> r.descontar(4))
                .isInstanceOf(StockInsuficiente.class)
                .hasMessageContaining("SKU-1");
        assertThat(r.getStock()).isEqualTo(3);
    }

    @Test
    @DisplayName("descontar exactamente el stock disponible lo deja en cero, no falla")
    void permiteLlegarACero() {
        Repuesto r = repuesto(5, 1);
        r.descontar(5);
        assertThat(r.getStock()).isZero();
    }

    @Test
    @DisplayName("una cantidad no positiva se rechaza")
    void rechazaCantidadNoPositiva() {
        Repuesto r = repuesto(5, 1);
        assertThatThrownBy(() -> r.descontar(0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> r.reponer(-1)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("esta bajo minimo cuando el stock es igual o menor al minimo")
    void detectaBajoMinimo() {
        assertThat(repuesto(2, 5).estaBajoMinimo()).isTrue();
        assertThat(repuesto(5, 5).estaBajoMinimo()).isTrue();
        assertThat(repuesto(6, 5).estaBajoMinimo()).isFalse();
    }

    @Test
    @DisplayName("reponer devuelve el stock comprometido")
    void repone() {
        Repuesto r = repuesto(4, 1);
        r.descontar(3);
        r.reponer(3);
        assertThat(r.getStock()).isEqualTo(4);
    }
}
