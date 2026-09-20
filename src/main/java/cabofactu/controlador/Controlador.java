package cabofactu.controlador;

import cabofactu.InstanciaUnica;
import cabofactu.PreparacionDatos;
import cabofactu.modelo.Modelo;
import cabofactu.modelo.negocio.sqlite.Conexion;
import cabofactu.vista.Vista;

import java.io.IOException;

/**
 * Une el modelo y la vista: arranca la aplicación, prepara la carpeta de datos
 * y la cierra al terminar.
 */
public class Controlador {

    private Modelo modelo;
    private Vista vista;

    public Controlador(Modelo modelo, Vista vista) {
        if (modelo == null) {
            throw new IllegalArgumentException("El modelo no puede ser nulo.");
        }
        if (vista == null) {
            throw new IllegalArgumentException("La vista no puede ser nula.");
        }
        this.modelo = modelo;
        this.vista = vista;
        this.vista.setControlador(this);
    }

    /** Arrancamos la vista; cuando se cierra la última ventana, terminamos. */
    public void comenzar() {
        vista.comenzar();
        terminar();
    }

    /** Soltamos el bloqueo de instancia única y cerramos la base de datos. */
    public void terminar() {
        InstanciaUnica.liberar();
        Conexion.cerrarConexion();
    }

    /**
     * Creamos la carpeta de datos y comprobamos que no haya otra aplicación
     * abierta. Si algo falla, lanzamos la excepción con el mensaje para el usuario.
     */
    public void prepararDatos() throws Exception {
        try {
            PreparacionDatos.crearCarpeta();
        } catch (IOException e) {
            throw new Exception("No se pudo preparar la carpeta de datos:\n" + e.getMessage());
        }
        boolean adquirido;
        try {
            adquirido = InstanciaUnica.adquirir();
        } catch (IOException e) {
            throw new Exception("No se pudo comprobar si la aplicación ya está abierta:\n" + e.getMessage());
        }
        if (!adquirido) {
            throw new Exception("La aplicación ya está en ejecución.\nSolo puede abrirse una instancia.");
        }
    }

    /** Cargamos la empresa de demostración si no hay ninguna; devolvemos true si se ha cargado. */
    public boolean cargarDemostracion() throws Exception {
        return PreparacionDatos.cargarDemoSiNoHayEmpresas();
    }

    /**
     * Damos el modelo mientras quedan pantallas por rehacer. Cuando todas llamen
     * a las operaciones del controlador, este método desaparece.
     */
    public Modelo getModelo() {
        return modelo;
    }
}
