package cabofactu.modelo.dominio;

import cabofactu.utilidades.ValidadorCodigoPostal;
import cabofactu.utilidades.ValidadorDocumentoFiscal;
import cabofactu.utilidades.ValidadorEmail;

/**
 * Datos de un cliente. Los setters comprueban lo que reciben y lanzan
 * Exception con el mensaje que verá el usuario, así que un Cliente que
 * existe es siempre un Cliente válido.
 */
public class Cliente {

    private Long id;
    private String nombre;
    private String nif;
    private String direccion;
    private String cp;
    private String localidad;
    private String provincia;
    private String email;
    private boolean activo;

    public Cliente(String nombre, String nif, String direccion, String cp,
                   String localidad, String provincia) throws Exception {
        setNombre(nombre);
        setNif(nif);
        setDireccion(direccion);
        setCp(cp);
        setLocalidad(localidad);
        setProvincia(provincia);
        setEmail("");
        setActivo(true);
    }

    /** Copiamos un cliente entero, para poder editarlo sin tocar el original. */
    public Cliente(Cliente otro) throws Exception {
        this(otro.getNombre(), otro.getNif(), otro.getDireccion(), otro.getCp(),
                otro.getLocalidad(), otro.getProvincia());
        setId(otro.getId());
        setEmail(otro.getEmail());
        setActivo(otro.isActivo());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    /** El email es opcional: si no se escribe, lo guardamos vacío. */
    public void setEmail(String email) throws Exception {
        String error = errorEmail(email);
        if (error != null) {
            throw new Exception(error);
        }
        if (email == null || email.isBlank()) {
            this.email = "";
        } else {
            this.email = email.trim();
        }
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    @Override
    public boolean equals(Object otro) {
        if (this == otro) {
            return true;
        }
        if (otro == null || getClass() != otro.getClass()) {
            return false;
        }
        Cliente cliente = (Cliente) otro;
        return nif.equals(cliente.nif);
    }

    @Override
    public int hashCode() {
        return nif.hashCode();
    }

    @Override
    public String toString() {
        return getNombreNif();
    }

    /** «Activo» o «Inactivo», para la columna Estado de la tabla. */
    public String getEstadoTexto() {
        if (activo) {
            return "Activo";
        }
        return "Inactivo";
    }

    /** «Nombre (NIF)», para el desplegable de clientes del Editor. */
    public String getNombreNif() {
        return nombre + " (" + nif + ")";
    }

    /** Decimos si otro cliente tiene los mismos datos que este, sin mirar el id ni si está activo. */
    public boolean tieneLosMismosDatos(Cliente otro) {
        if (otro == null) {
            return false;
        }
        return nombre.equals(otro.getNombre())
                && nif.equals(otro.getNif())
                && direccion.equals(otro.getDireccion())
                && cp.equals(otro.getCp())
                && localidad.equals(otro.getLocalidad())
                && provincia.equals(otro.getProvincia())
                && email.equals(otro.getEmail());
    }

    /** Decimos qué le pasa al nombre, o null si está bien. */
    public static String errorNombre(String valor) {
        if (valor == null || valor.isBlank()) {
            return "Indique el nombre del cliente.";
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
            return "La dirección del cliente es obligatoria.";
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
            return "La localidad del cliente es obligatoria.";
        }
        return null;
    }

    /** Decimos qué le pasa a la provincia, o null si está bien. */
    public static String errorProvincia(String valor) {
        if (valor == null || valor.isBlank()) {
            return "La provincia del cliente es obligatoria.";
        }
        return null;
    }

    /** El email es opcional: vacío está bien. */
    public static String errorEmail(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        if (!ValidadorEmail.esValido(valor.trim())) {
            return "Revise el formato del correo electrónico.";
        }
        return null;
    }
}