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

    public long insertar(Cliente c) {
        return clienteDAO.insertar(c);
    }

    public void actualizar(Cliente c) {
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
