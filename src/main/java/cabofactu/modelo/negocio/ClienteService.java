package cabofactu.modelo.negocio;

import cabofactu.modelo.dominio.Cliente;
import cabofactu.modelo.negocio.sqlite.ClienteRepository;

import java.util.List;

public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    public List<Cliente> listar(boolean soloActivos) {
        return clienteRepository.listar(soloActivos);
    }

    public List<Cliente> buscar(String texto, boolean soloActivos) {
        return clienteRepository.buscar(texto, soloActivos);
    }

    public long insertar(Cliente c) {
        return clienteRepository.insertar(c);
    }

    public void actualizar(Cliente c) {
        clienteRepository.actualizar(c);
    }

    public boolean tieneFacturas(long id) {
        return clienteRepository.tieneFacturas(id);
    }

    public void setActivo(long id, boolean activo) {
        clienteRepository.setActivo(id, activo);
    }

    public void borrarFisico(long id) {
        clienteRepository.borrarFisico(id);
    }
}
