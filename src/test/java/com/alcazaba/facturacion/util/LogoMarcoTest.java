package com.alcazaba.facturacion.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.alcazaba.facturacion.ui.JavaFxTestSupport;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Test del clasificador y del relleno de LogoMarco usando imagenes artificiales
 * (WritableImage) construidas sobre el toolkit JavaFX de test.
 */
class LogoMarcoTest {

    @BeforeAll
    static void arrancar() throws Exception {
        JavaFxTestSupport.arrancarFx();
    }

    @Test
    void marcoBlancoProducePlanoBlancoPuro() throws Exception {
        LogoMarco.Resultado r = enFx(() -> {
            WritableImage img = creada(100, 100);
            pintarMarco(img, 0xFFFFFFFF, 0xFF000000);
            return LogoMarco.clasificar(img);
        });
        assertEquals(LogoMarco.Tipo.PLANO, r.tipo);
        assertEquals(255, canal(r.color, 0), "rojo");
        assertEquals(255, canal(r.color, 1), "verde");
        assertEquals(255, canal(r.color, 2), "azul");
    }

    @Test
    void marcoDeColorProducePlanoExacto() throws Exception {
        LogoMarco.Resultado r = enFx(() -> {
            WritableImage img = creada(100, 100);
            pintarMarco(img, 0xFF6496C8, 0xFF000000);
            return LogoMarco.clasificar(img);
        });
        assertEquals(LogoMarco.Tipo.PLANO, r.tipo);
        assertEquals(100, canal(r.color, 0));
        assertEquals(150, canal(r.color, 1));
        assertEquals(200, canal(r.color, 2));
    }

    @Test
    void marcoTransparenteNoTocaNada() throws Exception {
        LogoMarco.Resultado r = enFx(() -> {
            WritableImage img = creada(100, 100);
            pintarEsquina(img, 0, 0, 3, 3, 0xFFFFFFFF);
            return LogoMarco.clasificar(img);
        });
        assertEquals(LogoMarco.Tipo.TRANSPARENTE, r.tipo);
        assertNull(r.color);
    }

    @Test
    void marcoConRuidoProduceDifuminado() throws Exception {
        LogoMarco.Resultado r = enFx(() -> {
            WritableImage img = creada(100, 100);
            pintarMarcoRuidoso(img);
            return LogoMarco.clasificar(img);
        });
        assertEquals(LogoMarco.Tipo.DIFUMINADO, r.tipo);
        assertNull(r.color);
    }

    @Test
    void imagenNulaProduceTransparente() throws Exception {
        LogoMarco.Resultado r = enFx(() -> LogoMarco.clasificar(null));
        assertEquals(LogoMarco.Tipo.TRANSPARENTE, r.tipo);
    }

    @Test
    void imagenMiniaturaProduceTransparente() throws Exception {
        LogoMarco.Resultado r = enFx(() -> {
            WritableImage img = creada(2, 2);
            pintarRect(img, 0, 0, 2, 2, 0xFFFF0000);
            return LogoMarco.clasificar(img);
        });
        assertEquals(LogoMarco.Tipo.TRANSPARENTE, r.tipo);
    }

    @Test
    void aplicarLimpiaYCasoPlanoPintaEstilo() throws Exception {
        enFx(() -> {
            StackPane pane = caja(280, 100);
            pane.setStyle("-fx-background-color: red;");
            Image img = creadaConMarco(100, 100, 0xFFFFFFFF, 0xFF000000);
            LogoMarco.aplicar(pane, img);
            assertTrue(pane.getStyle().contains("#FFFFFF"), "estilo con color");
            assertNull(pane.getClip());
            return null;
        });
    }

    @Test
    void aplicarLimpiaYCasoDifuminadoPoneRespaldo() throws Exception {
        enFx(() -> {
            StackPane pane = caja(100, 100);
            Image img = creadaConMarcoRuidoso(100, 100);
            LogoMarco.aplicar(pane, img);
            assertEquals(1, pane.getChildren().size(), "respaldo presente");
            ImageView respaldo = (ImageView) pane.getChildren().get(0);
            assertFalse(respaldo.isManaged(), "respaldo no gestionado");
            assertTrue(respaldo.isMouseTransparent(), "respaldo no capta clics");
            assertEquals(160, respaldo.getFitWidth(), 0.01, "ancho = caja + desborde");
            assertEquals(160, respaldo.getFitHeight(), 0.01, "alto = caja + desborde");
            assertNotNull(pane.getClip(), "clip de esquinas");
            assertTrue(pane.getClip() instanceof Rectangle, "clip redondeado");
            Rectangle clip = (Rectangle) pane.getClip();
            assertEquals(20, clip.getArcWidth(), 0.01, "arco = 2x radio 10");
            LogoMarco.aplicar(pane, imagenTransparente());
            assertEquals(0, pane.getChildren().size(), "limpieza en segunda aplicacion");
            assertNull(pane.getClip(), "clip retirado");
            return null;
        });
    }

    @Test
    void limpiarRetiraRespaldoYClip() throws Exception {
        enFx(() -> {
            StackPane pane = caja(100, 100);
            Image img = creadaConMarcoRuidoso(100, 100);
            LogoMarco.aplicar(pane, img);
            LogoMarco.limpiar(pane);
            assertEquals(0, pane.getChildren().size());
            assertNull(pane.getClip());
            assertEquals("", pane.getStyle());
            return null;
        });
    }

    @Test
    void cambiarDeFotograAFlancoNoDejaRastro() throws Exception {
        enFx(() -> {
            StackPane pane = caja(100, 100);
            LogoMarco.aplicar(pane, creadaConMarcoRuidoso(100, 100));
            assertEquals(1, pane.getChildren().size(), "foto con respaldo");
            assertNotNull(pane.getClip());
            LogoMarco.aplicar(pane, creadaConMarco(100, 100, 0xFFFFFFFF, 0xFF000000));
            assertEquals(0, pane.getChildren().size(), "sin respaldo al pasar a plano");
            assertNull(pane.getClip(), "sin clip al pasar a plano");
            assertTrue(pane.getStyle().contains("#FFFFFF"), "color del plano");
            return null;
        });
    }

    @Test
    void cambiarDePlanoAFotografiaNoDejaRastro() throws Exception {
        enFx(() -> {
            StackPane pane = caja(100, 100);
            LogoMarco.aplicar(pane, creadaConMarco(100, 100, 0xFFFFFFFF, 0xFF000000));
            assertTrue(pane.getStyle().contains("#FFFFFF"), "arranca plano");
            LogoMarco.aplicar(pane, creadaConMarcoRuidoso(100, 100));
            assertEquals(1, pane.getChildren().size(), "foto con respaldo");
            assertNotNull(pane.getClip(), "clip para la foto");
            assertEquals("", pane.getStyle(), "sin estilo restante");
            return null;
        });
    }

    @Test
    void casoTransparenteNoTocaNada() throws Exception {
        enFx(() -> {
            StackPane pane = caja(100, 100);
            LogoMarco.aplicar(pane, imagenTransparente());
            assertEquals(0, pane.getChildren().size());
            assertNull(pane.getClip());
            assertEquals("", pane.getStyle());
            return null;
        });
    }

    private static int canal(Color c, int canal) {
        double v = canal == 0 ? c.getRed() : canal == 1 ? c.getGreen() : c.getBlue();
        return (int) Math.round(v * 255);
    }

    private static StackPane caja(double w, double h) {
        StackPane p = new StackPane();
        p.setPrefSize(w, h);
        return p;
    }

    private static WritableImage creada(int w, int h) {
        WritableImage img = new WritableImage(w, h);
        PixelWriter pw = img.getPixelWriter();
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                pw.setArgb(x, y, 0x00000000);
            }
        }
        return img;
    }

    private static WritableImage creadaConMarco(int w, int h, int colorMarco, int interior) {
        WritableImage img = creada(w, h);
        pintarMarco(img, colorMarco, interior);
        return img;
    }

    private static WritableImage creadaConMarcoRuidoso(int w, int h) {
        WritableImage img = creada(w, h);
        pintarMarcoRuidoso(img);
        return img;
    }

    private static WritableImage imagenTransparente() {
        return creada(100, 100);
    }

    private static void pintarRect(WritableImage img, int x0, int y0, int x1, int y1, int argb) {
        PixelWriter pw = img.getPixelWriter();
        for (int y = y0; y < y1; y++) {
            for (int x = x0; x < x1; x++) {
                pw.setArgb(x, y, argb);
            }
        }
    }

    private static void pintarEsquina(WritableImage img, int x, int y, int anch, int alt, int argb) {
        pintarRect(img, x, y, x + anch, y + alt, argb);
    }

    private static void pintarMarco(WritableImage img, int colorMarco, int interior) {
        int w = (int) img.getWidth();
        int h = (int) img.getHeight();
        int b = Math.max(1, Math.round(w * 0.06f));
        pintarRect(img, 0, 0, w, b, colorMarco);
        pintarRect(img, 0, h - b, w, h, colorMarco);
        pintarRect(img, 0, b, b, h - b, colorMarco);
        pintarRect(img, w - b, b, w, h - b, colorMarco);
        pintarRect(img, b, b, w - b, h - b, interior);
    }

    private static void pintarMarcoRuidoso(WritableImage img) {
        int w = (int) img.getWidth();
        int h = (int) img.getHeight();
        int b = Math.max(1, Math.round(w * 0.06f));
        PixelWriter pw = img.getPixelWriter();
        int n = 0;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                boolean marco = y < b || y >= h - b || x < b || x >= w - b;
                if (marco) {
                    n++;
                    int v = (x * 31 + y * 17) & 0xFF;
                    pw.setArgb(x, y, 0xFF000000 | (v << 16) | ((v * 5) << 8) | (v * 3));
                }
            }
        }
    }

    private static <T> T enFx(Callable<T> tarea) throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<T> resultado = new AtomicReference<>();
        AtomicReference<Throwable> error = new AtomicReference<>();
        Platform.runLater(() -> {
            try {
                resultado.set(tarea.call());
            } catch (Throwable t) {
                error.set(t);
            } finally {
                latch.countDown();
            }
        });
        if (!latch.await(30, TimeUnit.SECONDS)) {
            throw new IllegalStateException("JavaFX no respondio en 30 s");
        }
        if (error.get() != null) {
            throw new AssertionError("Error en hilo JavaFX", error.get());
        }
        return resultado.get();
    }
}
