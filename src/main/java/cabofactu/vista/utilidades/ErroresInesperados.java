package cabofactu.vista.utilidades;

import cabofactu.modelo.negocio.sqlite.Conexion;
import javafx.application.Platform;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Manejador global de errores que ninguna pantalla ha capturado.
 *
 * Cómo funciona: {@code Thread.setDefaultUncaughtExceptionHandler} es el
 * método al que Java llama con cualquier excepción que se escape sin
 * {@code catch}, también las que lanzan los botones de JavaFX. Registrándolo
 * una vez al arrancar, un error no previsto avisa al usuario y queda anotado
 * en vez de perderse en la consola.
 */
public final class ErroresInesperados {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String FICHERO = "errores.log";

    private ErroresInesperados() {
    }

    /** Registramos el manejador global para toda la aplicación. */
    public static void registrar() {
        Thread.setDefaultUncaughtExceptionHandler((hilo, error) -> tratar(error));
    }

    /** Guardamos el error y, si estamos en el hilo de JavaFX, avisamos al usuario. */
    public static void tratar(Throwable error) {
        guardar(error);
        try {
            if (Platform.isFxApplicationThread()) {
                String texto = String.format(
                        "Ha ocurrido un error inesperado:%n%s%n%nLa aplicación sigue abierta. "
                                + "Si se repite, revisa errores.log en la carpeta de datos.",
                        mensajeDe(error));
                Dialogos.mostrarDialogoError("Error inesperado", texto);
            }
        } catch (Exception e) {
            System.err.println("No se pudo mostrar el aviso de error inesperado: " + e.getMessage());
        }
    }

    /** Añadimos una entrada a errores.log con la fecha, el tipo, el mensaje y el detalle completo. */
    public static void guardar(Throwable error) {
        StringWriter detalle = new StringWriter();
        error.printStackTrace(new PrintWriter(detalle));

        StringBuilder entrada = new StringBuilder();
        entrada.append(LocalDateTime.now().format(FORMATO_FECHA));
        entrada.append("  ").append(error.getClass().getSimpleName()).append(": ").append(mensajeDe(error));
        entrada.append(System.lineSeparator());
        for (String linea : detalle.toString().split(System.lineSeparator())) {
            entrada.append("    ").append(linea).append(System.lineSeparator());
        }

        Path fichero = Conexion.carpetaRaiz().resolve(FICHERO);
        try {
            Files.writeString(fichero, entrada.toString(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (Exception e) {
            System.err.println("No se pudo escribir " + FICHERO + ": " + e.getMessage());
        }
    }

    /** El mensaje del error, o su tipo si no tiene mensaje. */
    private static String mensajeDe(Throwable error) {
        String mensaje = error.getMessage();
        if (mensaje == null || mensaje.isBlank()) {
            return error.getClass().getSimpleName();
        }
        return mensaje;
    }
}
