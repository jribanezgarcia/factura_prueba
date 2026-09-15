package cabofactu.modelo.negocio;

import cabofactu.modelo.dominio.Cliente;
import cabofactu.modelo.negocio.sqlite.ClienteDAO;

import java.util.List;

public class Clientes {

    private final ClienteDAO clienteDAO;

    public Clientes(ClienteDAO clienteDAO) {
        this.clienteDAO = clienteDAO;
    }

    public List<Cliente> listar(boolean soloActivos) {
        return clienteDAO.listar(soloActivos);
    }

    public List<Cliente> buscar(String texto, boolean soloActivos) {
        return clienteDAO.buscar(texto, soloActivos);
    }

    /** Guardamos el cliente tras comprobar sus datos obligatorios. */
    public long insertar(Cliente c) throws ValidacionException {
        ValidacionCliente.comprobar(c);
        return clienteDAO.insertar(c);
    }

    /** Guardamos los cambios tras comprobar sus datos obligatorios. */
    public void actualizar(Cliente c) throws ValidacionException {
        ValidacionCliente.comprobar(c);
        clienteDAO.actualizar(c);
    }

    public boolean tieneFacturas(long id) {
        return clienteDAO.tieneFacturas(id);
    }

    public void setActivo(long id, boolean activo) {
        clienteDAO.setActivo(id, activo);
    }

    public void borrarFisico(long id) {
        clienteDAO.borrarFisico(id);
    }
}
