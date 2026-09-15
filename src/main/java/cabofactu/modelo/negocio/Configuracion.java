package cabofactu.modelo.negocio;

import cabofactu.modelo.dominio.Empresa;
import cabofactu.modelo.negocio.sqlite.ConfiguracionDAO;
import cabofactu.utilidades.CodigoPostalValidator;
import cabofactu.utilidades.DocumentoFiscalValidator;
import cabofactu.utilidades.EmailValidator;

import java.util.ArrayList;
import java.util.List;

public class Configuracion {

    private final ConfiguracionDAO configuracionDAO;

    public Configuracion(ConfiguracionDAO configuracionDAO) {
        this.configuracionDAO = configuracionDAO;
    }

    public Empresa getEmpresa() {
        return configuracionDAO.getEmpresa();
    }

    public void saveEmpresa(Empresa e) {
        configuracionDAO.saveEmpresa(e);
    }

    public String getPreferencia(String clave) {
        return configuracionDAO.getPreferencia(clave);
    }

    public void setPreferencia(String clave, String valor) {
        configuracionDAO.setPreferencia(clave, valor);
    }

    public List<String> datosPendientes(Empresa e) {
        List<String> faltan = new ArrayList<>();
        if (vacio(e == null ? null : e.getNombre())) {
            faltan.add("Nombre / razón social");
        }
        if (vacio(e == null ? null : e.getNif()) || !DocumentoFiscalValidator.esValido(e.getNif())) {
            faltan.add("NIF");
        }
        if (vacio(e == null ? null : e.getDireccion())) {
            faltan.add("Dirección");
        }
        if (!CodigoPostalValidator.esValido(e == null ? null : e.getCp())) {
            faltan.add("CP");
        }
        if (vacio(e == null ? null : e.getLocalidad())) {
            faltan.add("Localidad");
        }
        if (vacio(e == null ? null : e.getProvincia())) {
            faltan.add("Provincia");
        }
        if (vacio(e == null ? null : e.getEmail()) || !EmailValidator.esValido(e.getEmail())) {
            faltan.add("Email");
        }
        if (vacio(e == null ? null : e.getTelefono())) {
            faltan.add("Teléfono");
        }
        return faltan;
    }

    public List<String> datosPendientes() {
        return datosPendientes(getEmpresa());
    }

    public boolean empresaCompleta() {
        return datosPendientes().isEmpty();
    }

    private static boolean vacio(String s) {
        return s == null || s.isBlank();
    }
}
