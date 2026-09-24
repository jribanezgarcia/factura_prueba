package cabofactu.vista;

import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Paint;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Los siete temas visten la escena sin romperse: cada variable de color de
 * su `.root` resuelve, y la fuente trae valor. Es el fallo del punto del
 * `.root` borrado, que dejaba la paleta sin definir. La fuente no se puede
 * resolver en headless (todo da System), así que de ella solo miramos el texto.
 */
class TemasTest {

    private static final Path DIR_TEMAS = Path.of(
            "src", "main", "resources", "cabofactu", "vista", "recursos", "temas");

    @BeforeAll
    static void arrancar() throws Exception {
        PruebasJavaFx.arrancarFx();
    }

    @Test
    void fondosYPaleta() throws Exception {
        List<String> temas = new ArrayList<>();
        File[] ficheros = DIR_TEMAS.toFile().listFiles();
        if (ficheros != null) {
            Arrays.sort(ficheros);
            for (File fichero : ficheros) {
                if (fichero.getName().startsWith("tema-")) {
                    temas.add(fichero.getName());
                }
            }
        }
        assertFalse(temas.isEmpty(), "Debe haber temas en " + DIR_TEMAS);
        for (String tema : temas) {
            comprobar(tema);
        }
    }

    private void comprobar(String tema) {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> error = new AtomicReference<>();
        Platform.runLater(() -> {
            try {
                revisarTema(tema);
            } catch (Throwable t) {
                error.set(t);
            } finally {
                latch.countDown();
            }
        });
        await(latch, error, tema);
    }

    private static void revisarTema(String tema) throws Exception {
        Path base = DIR_TEMAS.resolve("base.css");
        Path fichero = DIR_TEMAS.resolve(tema);
        Scene escena = new Scene(new StackPane());
        escena.getStylesheets().setAll(
                base.toUri().toURL().toExternalForm(),
                fichero.toUri().toURL().toExternalForm());
        Parent raiz = escena.getRoot();
        raiz.applyCss();
        List<String> variables = new ArrayList<>(variablesDe(fichero).keySet());
        assertFalse(variables.isEmpty(), "El .root de " + tema + " no define variables");
        for (Map.Entry<String, String> variable : variablesDe(fichero).entrySet()) {
            comprobarVariable(raiz, variable.getKey(), variable.getValue(), tema);
        }
    }

    private static void comprobarVariable(Parent raiz, String nombre, String valor, String tema) {
        if (nombre.contains("font")) {
            // En headless no hay fuentes y todo resuelve a System; solo que traiga valor.
            assertFalse(valor.isBlank(), "La variable " + nombre + " de " + tema + " está vacía");
        } else {
            comprobarColor(raiz, nombre, tema);
        }
    }

    private static void comprobarColor(Parent raiz, String variable, String tema) {
        Region sonda = new Region();
        sonda.setStyle("-fx-background-color: " + variable + ";");
        ((StackPane) raiz).getChildren().add(sonda);
        sonda.applyCss();
        Paint color = null;
        if (sonda.getBackground() != null && !sonda.getBackground().getFills().isEmpty()) {
            color = sonda.getBackground().getFills().get(0).getFill();
        }
        assertNotNull(color, "La variable " + variable + " de " + tema + " no resuelve");
    }

    private static Map<String, String> variablesDe(Path fichero) throws Exception {
        Map<String, String> variables = new LinkedHashMap<>();
        boolean dentro = false;
        for (String linea : Files.readAllLines(fichero, StandardCharsets.UTF_8)) {
            String recortada = linea.trim();
            if (recortada.startsWith(".root")) {
                dentro = true;
            }
            if (dentro) {
                int dosPuntos = recortada.indexOf(':');
                if (recortada.startsWith("-fx-") && dosPuntos > 0) {
                    String nombre = recortada.substring(0, dosPuntos).trim();
                    String valor = recortada.substring(dosPuntos + 1).trim();
                    if (valor.endsWith(";")) {
                        valor = valor.substring(0, valor.length() - 1).trim();
                    }
                    variables.put(nombre, valor);
                }
            }
            if (dentro && recortada.contains("}")) {
                dentro = false;
            }
        }
        return variables;
    }

    private void await(CountDownLatch latch, AtomicReference<Throwable> error, String tema) {
        try {
            if (!latch.await(30, TimeUnit.SECONDS)) {
                fail("El tema " + tema + " no termino de comprobarse en 30 s");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("Interrumpido comprobando " + tema);
        }
        if (error.get() != null) {
            throw new AssertionError("Error en el tema " + tema, error.get());
        }
    }
}
