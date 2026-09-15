package cabofactu.modelo.negocio;

import cabofactu.modelo.dominio.TipoIva;
import cabofactu.modelo.negocio.sqlite.TipoIvaDAO;

import java.util.List;

public class TiposIva {

    private final TipoIvaDAO tipoIvaDAO;

    public TiposIva(TipoIvaDAO tipoIvaDAO) {
        this.tipoIvaDAO = tipoIvaDAO;
    }

    public List<TipoIva> listar(boolean soloActivos) {
        return tipoIvaDAO.listar(soloActivos);
    }

    public TipoIva getById(long id) {
        return tipoIvaDAO.getById(id);
    }

    public long insertar(TipoIva t) {
        return tipoIvaDAO.insertar(t);
    }

    public void actualizar(TipoIva t) {
        tipoIvaDAO.actualizar(t);
    }

    public void setActivo(long id, boolean activo) {
        tipoIvaDAO.setActivo(id, activo);
    }

    public boolean enUso(long id) {
        return tipoIvaDAO.enUso(id);
    }
}
