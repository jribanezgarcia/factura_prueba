package cabofactu.utilidades;

import javafx.scene.image.PixelReader;

import java.util.List;

/**
 * Recorre bandas de píxeles del marco de una imagen y guarda los opacos.
 */
final class MuestrasMarco {

    private static final double ALFA_MINIMO = 0.9;

    private final PixelReader pr;
    private final int paso;
    private final List<int[]> opacos;
    private int total;

    MuestrasMarco(PixelReader pr, int paso, List<int[]> opacos) {
        this.pr = pr;
        this.paso = paso;
        this.opacos = opacos;
    }

    int total() {
        return total;
    }

    void banda(int y0, int y1, int x0, int x1) {
        for (int y = y0; y < y1; y += paso) {
            for (int x = x0; x < x1; x += paso) {
                total++;
                int argb = pr.getArgb(x, y);
                if ((argb >>> 24) / 255.0 >= ALFA_MINIMO) {
                    opacos.add(new int[]{(argb >> 16) & 0xff, (argb >> 8) & 0xff, argb & 0xff});
                }
            }
        }
    }
}
