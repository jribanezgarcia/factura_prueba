package cabofactu.vista;

/**
 * Lo que la Vista puede pedirle a cada pantalla.
 *
 * Cómo funciona: los métodos llevan una implementación por defecto, así que
 * cada pantalla solo escribe los que necesita.
 */
public interface Pantalla {

    /** Se llama cuando la pantalla ya está dentro de la ventana: atajos de teclado y foco. */
    default void alMostrar() {
    }

    /** Devolvemos false si hay cambios sin guardar y el usuario decide quedarse. */
    default boolean puedeCerrar() {
        return true;
    }

    /** Se llama al cerrar la aplicación. */
    default void alCerrar() {
    }
}
