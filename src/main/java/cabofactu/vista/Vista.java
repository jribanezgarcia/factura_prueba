package cabofactu.vista;

import cabofactu.modelo.Modelo;

/**
 * Contrato de las vistas FXML: reciben modelo y navegador tras su carga.
 */
public interface Vista {

    default void setModelo(Modelo m) {
    }

    default void setNavegador(Navegador n) {
    }

    /** Se llama despues de inyectar modelo; aqui se puebla la UI. */
    default void alIniciar() {
    }

    /** false si hay cambios sin guardar que impiden volver o cerrar. */
    default boolean puedeCerrar() {
        return true;
    }

    /** Se llama al cerrar la ventana (para persistir preferencias). */
    default void alCerrar() {
    }
}
