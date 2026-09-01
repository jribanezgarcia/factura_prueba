package com.alcazaba.facturacion.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/**
 * Verifica que ningun FXML declare un styleClass con espacios separando
 * clases: FXMLLoader parte esas listas por comas, de modo que un espacio
 * crea una clase literal que no coincide con ningun selector CSS.
 */
class StyleClassSeparadorTest {

    private static final Path DIR_FXML = Path.of(
            "src", "main", "resources", "com", "alcazaba", "facturacion", "ui");

    private static final Pattern ATRIBUTO_STYLE_CLASS =
            Pattern.compile("styleClass\\s*=\\s*\"([^\"]*)\"");

    private static final Pattern ESPACIO_SIN_COMA =
            Pattern.compile("(?<!,)\\s");

    static List<String> incompatibilidades(String contenidoFxml) {
        List<String> problemas = new ArrayList<>();
        Matcher matcher = ATRIBUTO_STYLE_CLASS.matcher(contenidoFxml);
        while (matcher.find()) {
            String valor = matcher.group(1);
            if (ESPACIO_SIN_COMA.matcher(valor).find()) {
                problemas.add(valor);
            }
        }
        return problemas;
    }

    @Test
    void ningunFxmlConEspaciosEnStyleClass() throws IOException {
        List<String> fxmls;
        try (Stream<Path> stream = Files.list(DIR_FXML)) {
            fxmls = stream
                    .filter(p -> p.getFileName().toString().endsWith(".fxml"))
                    .sorted()
                    .map(p -> p.getFileName().toString())
                    .toList();
        }
        assertFalse(fxmls.isEmpty(), "Debe haber FXMLs en " + DIR_FXML);

        for (String nombre : fxmls) {
            String contenido = Files.readString(
                    DIR_FXML.resolve(nombre), StandardCharsets.UTF_8);
            List<String> incompatibles = incompatibilidades(contenido);
            assertEquals(List.of(), incompatibles,
                    "El FXML " + nombre
                            + " tiene styleClass con espacios sin coma");
        }
    }

    @Test
    void detectaElEspacioEntreClases() {
        String ejemplo = "<VBox styleClass=\"card zona-contenido\"/>";
        assertNotNull(ejemplo);
        assertEquals(List.of("card zona-contenido"), incompatibilidades(ejemplo));
    }

    @Test
    void aceptaSeparadorConComa() {
        String corregido = "<VBox styleClass=\"card, zona-contenido\"/>";
        assertEquals(List.of(), incompatibilidades(corregido));
    }
}