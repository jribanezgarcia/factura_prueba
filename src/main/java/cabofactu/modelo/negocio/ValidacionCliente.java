package cabofactu.modelo.negocio;

import cabofactu.modelo.dominio.Cliente;
import cabofactu.utilidades.ValidadorCodigoPostal;
import cabofactu.utilidades.ValidadorDocumentoFiscal;
import cabofactu.utilidades.ValidadorEmail;

/**
 * Comprobamos los datos obligatorios del cliente en un solo sitio, para que
 * la ficha, el Editor, la generación mensual y las rectificativas avisen
 * igual. Cada método devuelve el mensaje de error o null si está bien.
 */
public final class ValidacionCliente {

    private ValidacionCliente() {
    }

    /** Pedimos el nombre siempre, aunque la factura lo repita en la versión. */
    public static String errorNombre(String valor) {
        if (vacio(valor)) {
            return "Indique el nombre del cliente.";
        }
        return null;
    }

    /** Distinguimos vacío, formato incorrecto y letra incorrecta. */
    public static String errorNif(String valor) {
        if (vacio(valor)) {
            return "El NIF/NIE es obligatorio.";
        }
        if (!ValidadorDocumentoFiscal.formatoCorrecto(valor)) {
            return "Formato NIF/NIE incorrecto. Debe ser como 12345678Z (DNI), X1234567L (NIE) o B12345674 (CIF).";
        }
        if (!ValidadorDocumentoFiscal.letraCorrecta(valor)) {
            return "La letra no es correcta.";
        }
        return null;
    }

    /** Pedimos la dirección completa para la factura. */
    public static String errorDireccion(String valor) {
        if (vacio(valor)) {
            return "La dirección del cliente es obligatoria.";
        }
        return null;
    }

    /** Pedimos cinco dígitos entre 01 y 52, como en el validador. */
    public static String errorCodigoPostal(String valor) {
        if (vacio(valor)) {
            return "El código postal es obligatorio.";
        }
        if (!ValidadorCodigoPostal.esValido(valor)) {
            return "El código postal debe tener cinco dígitos y comenzar entre 01 y 52.";
        }
        return null;
    }

    /** Pedimos la localidad para la factura. */
    public static String errorLocalidad(String valor) {
        if (vacio(valor)) {
            return "La localidad del cliente es obligatoria.";
        }
        return null;
    }

    /** Pedimos la provincia para la factura. */
    public static String errorProvincia(String valor) {
        if (vacio(valor)) {
            return "La provincia del cliente es obligatoria.";
        }
        return null;
    }

    /** El email es opcional, pero si se escribe lo comprobamos. */
    public static String errorEmail(String valor) {
        if (!vacio(valor) && !ValidadorEmail.esValido(valor)) {
            return "Revise el formato del correo electrónico.";
        }
        return null;
    }

    /**
     * Comprobamos el cliente entero y lanzamos el primer error en este
     * orden: nombre, NIF, dirección, código postal, localidad, provincia
     * y email. Sin cliente no hay factura.
     */
    public static void comprobar(Cliente cliente) throws ValidacionException {
        if (cliente == null) {
            throw new ValidacionException("Indique los datos del cliente.");
        }
        String error = errorNombre(cliente.getNombre());
        if (error == null) {
            error = errorNif(cliente.getNif());
        }
        if (error == null) {
            error = errorDireccion(cliente.getDireccion());
        }
        if (error == null) {
            error = errorCodigoPostal(cliente.getCp());
        }
        if (error == null) {
            error = errorLocalidad(cliente.getLocalidad());
        }
        if (error == null) {
            error = errorProvincia(cliente.getProvincia());
        }
        if (error == null) {
            error = errorEmail(cliente.getEmail());
        }
        if (error != null) {
            throw new ValidacionException(error);
        }
    }

    private static boolean vacio(String valor) {
        return valor == null || valor.isBlank();
    }
}
