package cabofactu.modelo.negocio;

import cabofactu.modelo.dominio.Empresa;
import cabofactu.modelo.negocio.sqlite.Conexion;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * La empresa activa y las preferencias: es el único sitio con el SQL de las
 * tablas empresa y preferencias.
 */
public class Configuracion {

    private static Configuracion configuracion;

    private Configuracion() {
    }

    public static Configuracion getConfiguracion() {
        if (configuracion == null) {
            configuracion = new Configuracion();
        }
        return configuracion;
    }

    /**
     * Leemos la empresa. Si le falta algún dato obligatorio devolvemos null: eso
     * es lo que hace que la aplicación se quede en Configuración hasta completarla.
     */
    public Empresa buscarEmpresa() throws Exception {
        String consulta = """
                SELECT nombre, nif, direccion, cp, localidad, provincia, email, telefono,
                       actividad, cabecera_modo, logo_path, pie_legal
                FROM empresa WHERE id = 1
                """;
        try (PreparedStatement sentencia = Conexion.establecerConexion().prepareStatement(consulta);
             ResultSet fila = sentencia.executeQuery()) {
            if (!fila.next() || !datosCompletos(fila)) {
                return null;
            }
            return crearEmpresa(fila);
        } catch (SQLException e) {
            throw new Exception("Error SQLite: " + e.getMessage());
        }
    }

    /** Guardamos la empresa entera, que siempre viene completa. */
    public void modificarEmpresa(Empresa empresa) throws Exception {
        if (empresa == null) {
            throw new Exception("Indique los datos de la empresa.");
        }
        String actualizar = """
                UPDATE empresa SET nombre = ?, nif = ?, direccion = ?, cp = ?, localidad = ?, provincia = ?,
                    actividad = ?, email = ?, telefono = ?, cabecera_modo = ?, logo_path = ?, pie_legal = ?
                WHERE id = 1
                """;
        try (PreparedStatement sentencia = Conexion.establecerConexion().prepareStatement(actualizar)) {
            sentencia.setString(1, empresa.getNombre());
            sentencia.setString(2, empresa.getNif());
            sentencia.setString(3, empresa.getDireccion());
            sentencia.setString(4, empresa.getCp());
            sentencia.setString(5, empresa.getLocalidad());
            sentencia.setString(6, empresa.getProvincia());
            sentencia.setString(7, empresa.getActividad());
            sentencia.setString(8, empresa.getEmail());
            sentencia.setString(9, empresa.getTelefono());
            sentencia.setString(10, empresa.getCabeceraModo());
            sentencia.setString(11, empresa.getLogoPath());
            sentencia.setString(12, empresa.getPieLegal());
            int filas = sentencia.executeUpdate();
            if (filas == 0) {
                throw new Exception("No se ha podido guardar la empresa.");
            }
        } catch (SQLException e) {
            throw new Exception("Error SQLite: " + e.getMessage());
        }
    }

    /** El valor guardado para esa clave, o null si no hay ninguno. */
    public String preferencia(String clave) throws Exception {
        String consulta = "SELECT valor FROM preferencias WHERE clave = ?";
        try (PreparedStatement sentencia = Conexion.establecerConexion().prepareStatement(consulta)) {
            sentencia.setString(1, clave);
            try (ResultSet fila = sentencia.executeQuery()) {
                if (fila.next()) {
                    return fila.getString(1);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new Exception("Error SQLite: " + e.getMessage());
        }
    }

    /** Guardamos la preferencia, la haya o no de antes. */
    public void guardarPreferencia(String clave, String valor) throws Exception {
        String guardar = "INSERT INTO preferencias (clave, valor) VALUES (?, ?) "
                + "ON CONFLICT(clave) DO UPDATE SET valor = excluded.valor";
        try (PreparedStatement sentencia = Conexion.establecerConexion().prepareStatement(guardar)) {
            sentencia.setString(1, clave);
            sentencia.setString(2, valor);
            sentencia.executeUpdate();
        } catch (SQLException e) {
            throw new Exception("Error SQLite: " + e.getMessage());
        }
    }

    /**
     * Decimos si la fila trae los ocho datos obligatorios válidos: preguntamos
     * a cada errorX de Empresa y en cuanto uno se queja, no está completa.
     */
    private boolean datosCompletos(ResultSet fila) throws SQLException {
        if (Empresa.errorNombre(fila.getString("nombre")) != null) {
            return false;
        }
        if (Empresa.errorNif(fila.getString("nif")) != null) {
            return false;
        }
        if (Empresa.errorDireccion(fila.getString("direccion")) != null) {
            return false;
        }
        if (Empresa.errorCp(fila.getString("cp")) != null) {
            return false;
        }
        if (Empresa.errorLocalidad(fila.getString("localidad")) != null) {
            return false;
        }
        if (Empresa.errorProvincia(fila.getString("provincia")) != null) {
            return false;
        }
        if (Empresa.errorEmail(fila.getString("email")) != null) {
            return false;
        }
        if (Empresa.errorTelefono(fila.getString("telefono")) != null) {
            return false;
        }
        return true;
    }

    /** Pasamos la fila a objeto: el constructor con los obligatorios y después los opcionales. */
    private Empresa crearEmpresa(ResultSet fila) throws Exception {
        Empresa empresa = new Empresa(
                fila.getString("nombre"),
                fila.getString("nif"),
                fila.getString("direccion"),
                fila.getString("cp"),
                fila.getString("localidad"),
                fila.getString("provincia"),
                fila.getString("email"),
                fila.getString("telefono"));
        empresa.setActividad(fila.getString("actividad"));
        empresa.setCabeceraModo(fila.getString("cabecera_modo"));
        empresa.setLogoPath(fila.getString("logo_path"));
        empresa.setPieLegal(fila.getString("pie_legal"));
        return empresa;
    }
}
