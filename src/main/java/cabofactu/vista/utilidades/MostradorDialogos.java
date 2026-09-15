package cabofactu.vista.utilidades;

/**
 * Mostrador de avisos: por aquí pasan todos los diálogos de la aplicación.
 *
 * Cómo funciona: los avisos reales abren ventanas que esperan a que se pulse
 * un botón; en los tests cambiamos el mostrador por uno falso que solo cuenta
 * los avisos, para que no se queden bloqueados.
 */
public interface MostradorDialogos {
    void error(String titulo, String mensaje);

    void info(String titulo, String mensaje);

    boolean confirmar(String titulo, String mensaje);

    CambiosSinGuardar confirmarCambiosSinGuardar();

    /**
     * Pregunta como guardar la edicion de una factura ya guardada. El
     * mapeo por defecto reutiliza confirmar para no romper las
     * implementaciones de test existentes: true sobrescribe y false
     * cancela.
     */
    default ModoGuardarVersion modoGuardarVersion() {
        if (confirmar("Guardar cambios",
                "La factura ya está guardada. ¿Desea sobrescribir la versión actual con los cambios?")) {
            return ModoGuardarVersion.SOBRESCRIBIR;
        }
        return ModoGuardarVersion.CANCELAR;
    }
}
