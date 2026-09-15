package cabofactu.modelo.negocio;

import cabofactu.modelo.dominio.TipoRetencion;
import cabofactu.modelo.negocio.sqlite.TipoRetencionDAO;

import java.util.List;

public class TiposRetencion {

    private final TipoRetencionDAO tipoRetencionDAO;

    public TiposRetencion(TipoRetencionDAO tipoRetencionDAO) {
        this.tipoRetencionDAO = tipoRetencionDAO;
    }

    public List<TipoRetencion> listar(boolean soloActivos) {
        return tipoRetencionDAO.listar(soloActivos);
    }

    public long insertar(TipoRetencion t) {
        return tipoRetencionDAO.insertar(t);
    }

    public void actualizar(TipoRetencion t) {
        tipoRetencionDAO.actualizar(t);
    }

    public void setActivo(long id, boolean activo) {
        tipoRetencionDAO.setActivo(id, activo);
    }

    public boolean enUso(long id) {
        return tipoRetencionDAO.enUso(id);
    }
}
