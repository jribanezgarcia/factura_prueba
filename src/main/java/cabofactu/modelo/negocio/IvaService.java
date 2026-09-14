package cabofactu.modelo.negocio;

import cabofactu.modelo.dominio.TipoIva;
import cabofactu.modelo.negocio.sqlite.IvaRepository;

import java.util.List;

public class IvaService {

    private final IvaRepository ivaRepository;

    public IvaService(IvaRepository ivaRepository) {
        this.ivaRepository = ivaRepository;
    }

    public List<TipoIva> listar(boolean soloActivos) {
        return ivaRepository.listar(soloActivos);
    }

    public TipoIva getById(long id) {
        return ivaRepository.getById(id);
    }

    public long insertar(TipoIva t) {
        return ivaRepository.insertar(t);
    }

    public void actualizar(TipoIva t) {
        ivaRepository.actualizar(t);
    }

    public void setActivo(long id, boolean activo) {
        ivaRepository.setActivo(id, activo);
    }

    public boolean enUso(long id) {
        return ivaRepository.enUso(id);
    }
}
