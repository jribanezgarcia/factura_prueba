package cabofactu.modelo.dominio;

import cabofactu.utilidades.ValidadorCodigoPostal;
import cabofactu.utilidades.ValidadorDocumentoFiscal;
import cabofactu.utilidades.ValidadorEmail;

/**
 * Datos fiscales y de contacto de la empresa, y cómo se pinta la cabecera de
 * sus facturas. Los setters comprueban lo que reciben, así que una Empresa que
 * existe tiene siempre sus datos obligatorios completos.
 */
public class Empresa {

    public static final String CABECERA_TEXTO = "TEXTO";
    public static final String CABECERA_LOGO = "LOGO";

    private String nombre;
    private String nif;
    private String direccion;
    private String cp;
    private String localidad;
    private String provincia;
    private String email;
    private String telefono;
    private String actividad;
    private String cabeceraModo;
    private String logoPath;
    private String pieLegal;

    public Empresa(String nombre, String nif, String direccion, String cp,
                   String localidad, String provincia, String email, String telefono) throws Exception {
        setNombre(nombre);
        setNif(nif);
        setDireccion(direccion);
        setCp(cp);
        setLocalidad(localidad);
        setProvincia(provincia);
        setEmail(email);
        setTelefono(telefono);
        setActividad("");
        setCabeceraModo(CABECERA_TEXTO);
        setLogoPath("");
        setPieLegal("");
    }

    /** Copiamos una empresa entera, para poder editarla sin tocar la original. */
    public Empresa(Empresa otra) throws Exception {
        this(otra.getNombre(), otra.getNif(), otra.getDireccion(), otra.getCp(),
                otra.getLocalidad(), otra.getProvincia(), otra.getEmail(), otra.getTelefono());
        setActividad(otra.getActividad());
        setCabeceraModo(otra.getCabeceraModo());
        setLogoPath(otra.getLogoPath());
        setPieLegal(otra.getPieLegal());
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) throws Exception {
        String error = errorNombre(nombre);
        if (error != null) {
            throw new Exception(error);
        }
        this.nombre = nombre.trim();
    }

    public String getNif() {
        return nif;
    }

    public void setNif(String nif) throws Exception {
        String error = errorNif(nif);
        if (error != null) {
            throw new Exception(error);
        }
        this.nif = nif.trim().toUpperCase();
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) throws Exception {
        String error = errorDireccion(direccion);
        if (error != null) {
            throw new Exception(error);
        }
        this.direccion = direccion.trim();
    }

    public String getCp() {
        return cp;
    }

    public void setCp(String cp) throws Exception {
        String error = errorCp(cp);
        if (error != null) {
            throw new Exception(error);
        }
        this.cp = cp.trim();
    }

    public String getLocalidad() {
        return localidad;
    }

    public void setLocalidad(String localidad) throws Exception {
        String error = errorLocalidad(localidad);
        if (error != null) {
            throw new Exception(error);
        }
        this.localidad = localidad.trim();
    }

    public String getProvincia() {
        return provincia;
    }

    public void setProvincia(String provincia) throws Exception {
        String error = errorProvincia(provincia);
        if (error != null) {
            throw new Exception(error);
        }
        this.provincia = provincia.trim();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) throws Exception {
        String error = errorEmail(email);
        if (error != null) {
            throw new Exception(error);
        }
        this.email = email.trim();
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) throws Exception {
        String error = errorTelefono(telefono);
        if (error != null) {
            throw new Exception(error);
        }
        this.telefono = telefono.trim();
    }

    public String getActividad() {
        return actividad;
    }

    /** La actividad es opcional: si no se escribe, la guardamos vacía. */
    public void setActividad(String actividad) {
        if (actividad == null || actividad.isBlank()) {
            this.actividad = "";
        } else {
            this.actividad = actividad.trim();
        }
    }

    public String getCabeceraModo() {
        return cabeceraModo;
    }

    public void setCabeceraModo(String cabeceraModo) throws Exception {
        if (!CABECERA_TEXTO.equals(cabeceraModo) && !CABECERA_LOGO.equals(cabeceraModo)) {
            throw new Exception("Modo de cabecera no válido.");
        }
        this.cabeceraModo = cabeceraModo;
    }

    /** Decimos si la cabecera del PDF se pinta con el logo. */
    public boolean isCabeceraLogo() {
        return CABECERA_LOGO.equals(cabeceraModo);
    }

    public String getLogoPath() {
        return logoPath;
    }

    /** La ruta del logo es opcional: si no se escribe, la guardamos vacía. */
    public void setLogoPath(String logoPath) {
        if (logoPath == null || logoPath.isBlank()) {
            this.logoPath = "";
        } else {
            this.logoPath = logoPath.trim();
        }
    }

    public String getPieLegal() {
        return pieLegal;
    }

    /** El pie legal es opcional: si no se escribe, lo guardamos vacío. */
    public void setPieLegal(String pieLegal) {
        if (pieLegal == null || pieLegal.isBlank()) {
            this.pieLegal = "";
        } else {
            this.pieLegal = pieLegal.trim();
        }
    }

    @Override
    public boolean equals(Object otro) {
        if (this == otro) {
            return true;
        }
        if (otro == null || getClass() != otro.getClass()) {
            return false;
        }
        Empresa empresa = (Empresa) otro;
        return nif.equals(empresa.nif);
    }

    @Override
    public int hashCode() {
        return nif.hashCode();
    }

    @Override
    public String toString() {
        return nombre;
    }

    /** Decimos qué le pasa al nombre, o null si está bien. */
    public static String errorNombre(String valor) {
        if (valor == null || valor.isBlank()) {
            return "Indique el nombre o razón social de la empresa.";
        }
        return null;
    }

    /** Decimos qué le pasa al NIF, o null si está bien. */
    public static String errorNif(String valor) {
        if (valor == null || valor.isBlank()) {
            return "El NIF/NIE es obligatorio.";
        }
        String nif = valor.trim().toUpperCase();
        if (!ValidadorDocumentoFiscal.formatoCorrecto(nif)) {
            return "Formato NIF/NIE incorrecto. Debe ser como 12345678Z (DNI), "
                    + "X1234567L (NIE) o B12345674 (CIF).";
        }
        if (!ValidadorDocumentoFiscal.letraCorrecta(nif)) {
            return "La letra no es correcta.";
        }
        return null;
    }

    /** Decimos qué le pasa a la dirección, o null si está bien. */
    public static String errorDireccion(String valor) {
        if (valor == null || valor.isBlank()) {
            return "La dirección de la empresa es obligatoria.";
        }
        return null;
    }

    /** Decimos qué le pasa al código postal, o null si está bien. */
    public static String errorCp(String valor) {
        if (valor == null || valor.isBlank()) {
            return "El código postal es obligatorio.";
        }
        if (!ValidadorCodigoPostal.esValido(valor.trim())) {
            return "El código postal debe tener cinco dígitos y comenzar entre 01 y 52.";
        }
        return null;
    }

    /** Decimos qué le pasa a la localidad, o null si está bien. */
    public static String errorLocalidad(String valor) {
        if (valor == null || valor.isBlank()) {
            return "La localidad de la empresa es obligatoria.";
        }
        return null;
    }

    /** Decimos qué le pasa a la provincia, o null si está bien. */
    public static String errorProvincia(String valor) {
        if (valor == null || valor.isBlank()) {
            return "La provincia de la empresa es obligatoria.";
        }
        return null;
    }

    /** A diferencia del cliente, el email de la empresa es obligatorio. */
    public static String errorEmail(String valor) {
        if (valor == null || valor.isBlank()) {
            return "El email de la empresa es obligatorio.";
        }
        if (!ValidadorEmail.esValido(valor.trim())) {
            return "Revise el formato del correo electrónico.";
        }
        return null;
    }

    /** Decimos qué le pasa al teléfono, o null si está bien. */
    public static String errorTelefono(String valor) {
        if (valor == null || valor.isBlank()) {
            return "El teléfono de la empresa es obligatorio.";
        }
        return null;
    }
}
