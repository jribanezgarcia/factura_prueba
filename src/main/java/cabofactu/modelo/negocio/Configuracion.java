package cabofactu.modelo.negocio;

import cabofactu.modelo.dominio.Empresa;
import cabofactu.modelo.negocio.sqlite.ConfiguracionDAO;
import cabofactu.utilidades.ValidadorCodigoPostal;
import cabofactu.utilidades.ValidadorDocumentoFiscal;
import cabofactu.utilidades.ValidadorEmail;

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
        if (vacio(e == null ? null : e.getNif()) || !ValidadorDocumentoFiscal.esValido(e.getNif())) {
            faltan.add("NIF");
        }
        if (vacio(e == null ? null : e.getDireccion())) {
            faltan.add("Dirección");
        }
        if (!ValidadorCodigoPostal.esValido(e == null ? null : e.getCp())) {
            faltan.add("CP");
        }
        if (vacio(e == null ? null : e.getLocalidad())) {
            faltan.add("Localidad");
        }
        if (vacio(e == null ? null : e.getProvincia())) {
            faltan.add("Provincia");
        }
        if (vacio(e == null ? null : e.getEmail()) || !ValidadorEmail.esValido(e.getEmail())) {
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

    /** Lanzamos un aviso con los datos que faltan si la empresa no está completa. */
    public void comprobarEmpresaCompleta() throws Exception {
        List<String> faltan = datosPendientes();
        if (!faltan.isEmpty()) {
            throw new Exception("Faltan datos de tu empresa: " + String.join(", ", faltan) + ".\n\n"
                    + "Complétalos en Configuración para poder guardar facturas y exportar PDF.");
        }
    }

    private static boolean vacio(String s) {
        return s == null || s.isBlank();
    }
}
