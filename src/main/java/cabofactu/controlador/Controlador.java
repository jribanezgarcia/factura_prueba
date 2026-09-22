package cabofactu.controlador;

import cabofactu.InstanciaUnica;
import cabofactu.PreparacionDatos;
import cabofactu.modelo.Modelo;
import cabofactu.modelo.dominio.Cliente;
import cabofactu.modelo.dominio.Empresa;
import cabofactu.modelo.dominio.EmpresaDisponible;
import cabofactu.modelo.dominio.TipoIva;
import cabofactu.modelo.dominio.TipoRetencion;
import cabofactu.modelo.negocio.sqlite.Conexion;
import cabofactu.vista.Vista;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

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

    public List<Cliente> listadoClientes(boolean soloActivos) throws Exception {
        return modelo.listadoClientes(soloActivos);
    }

    public List<Cliente> listadoClientes(String texto, boolean soloActivos) throws Exception {
        return modelo.listadoClientes(texto, soloActivos);
    }

    public Cliente buscarCliente(long id) throws Exception {
        return modelo.buscarCliente(id);
    }

    public long altaCliente(Cliente cliente) throws Exception {
        return modelo.altaCliente(cliente);
    }

    public void modificarCliente(Cliente cliente) throws Exception {
        modelo.modificarCliente(cliente);
    }

    public void bajaCliente(long id) throws Exception {
        modelo.bajaCliente(id);
    }

    public void desactivarCliente(long id) throws Exception {
        modelo.desactivarCliente(id);
    }

    public boolean clienteTieneFacturas(long id) throws Exception {
        return modelo.clienteTieneFacturas(id);
    }

    public List<EmpresaDisponible> listadoEmpresas() throws Exception {
        return modelo.listadoEmpresas();
    }

    public EmpresaDisponible altaEmpresa(String nombre) throws Exception {
        return modelo.altaEmpresa(nombre);
    }

    public void bajaEmpresa(String carpeta) throws Exception {
        modelo.bajaEmpresa(carpeta);
    }

    public void abrirEmpresa(String carpeta, LocalDate fecha) throws Exception {
        modelo.abrirEmpresa(carpeta, fecha);
    }

    public void cerrarEmpresa() {
        modelo.cerrarEmpresa();
    }

    public Empresa buscarEmpresa() throws Exception {
        return modelo.buscarEmpresa();
    }

    public void modificarEmpresa(Empresa empresa) throws Exception {
        modelo.modificarEmpresa(empresa);
    }

    public String preferencia(String clave) throws Exception {
        return modelo.preferencia(clave);
    }

    public void guardarPreferencia(String clave, String valor) throws Exception {
        modelo.guardarPreferencia(clave, valor);
    }

    public List<TipoIva> listadoTiposIva(boolean soloActivos) throws Exception {
        return modelo.listadoTiposIva(soloActivos);
    }

    public TipoIva buscarTipoIva(long id) throws Exception {
        return modelo.buscarTipoIva(id);
    }

    public long altaTipoIva(TipoIva tipo) throws Exception {
        return modelo.altaTipoIva(tipo);
    }

    public void modificarTipoIva(TipoIva tipo) throws Exception {
        modelo.modificarTipoIva(tipo);
    }

    public void bajaTipoIva(long id) throws Exception {
        modelo.bajaTipoIva(id);
    }

    public boolean tipoIvaEnUso(long id) throws Exception {
        return modelo.tipoIvaEnUso(id);
    }

    public List<TipoRetencion> listadoTiposRetencion(boolean soloActivos) throws Exception {
        return modelo.listadoTiposRetencion(soloActivos);
    }

    public TipoRetencion buscarTipoRetencion(long id) throws Exception {
        return modelo.buscarTipoRetencion(id);
    }

    public long altaTipoRetencion(TipoRetencion tipo) throws Exception {
        return modelo.altaTipoRetencion(tipo);
    }

    public void modificarTipoRetencion(TipoRetencion tipo) throws Exception {
        modelo.modificarTipoRetencion(tipo);
    }

    public void bajaTipoRetencion(long id) throws Exception {
        modelo.bajaTipoRetencion(id);
    }

    public boolean tipoRetencionEnUso(long id) throws Exception {
        return modelo.tipoRetencionEnUso(id);
    }

    /**
     * Damos el modelo mientras quedan pantallas por rehacer. Cuando todas llamen
     * a las operaciones del controlador, este método desaparece.
     */
    public Modelo getModelo() {
        return modelo;
    }
}
