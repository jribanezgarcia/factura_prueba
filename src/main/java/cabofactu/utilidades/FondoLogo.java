package cabofactu.utilidades;

import javafx.scene.paint.Color;

/**
 * El resultado de clasificar el logo: en qué caso cae y, si es plano, el
 * color detectado en el marco.
 */
public final class FondoLogo {

    private final TipoFondoLogo tipo;
    private final Color color;

    FondoLogo(TipoFondoLogo tipo, Color color) {
        this.tipo = tipo;
        this.color = color;
    }

    public TipoFondoLogo getTipo() {
        return tipo;
    }

    public Color getColor() {
        return color;
    }
}
