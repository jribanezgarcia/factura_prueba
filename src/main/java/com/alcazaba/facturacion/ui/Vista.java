package com.alcazaba.facturacion.ui;

import com.alcazaba.facturacion.service.Servicios;

/**
 * Contrato de las vistas FXML: reciben servicios y navegador tras su carga.
 */
public interface Vista {

    default void setServicios(Servicios s) {
    }

    default void setNavegador(Navegador n) {
    }

    /** Se llama despues de inyectar servicios; aqui se puebla la UI. */
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
