package cabofactu.modelo.negocio;

import cabofactu.modelo.dominio.TipoRetencion;
import cabofactu.modelo.negocio.sqlite.Conexion;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Los tipos de retención de la empresa activa: es el único sitio con el SQL
 * de la tabla tipo_retencion.
 */
public class TiposRetencion {

    private static TiposRetencion tiposRetencion;

    private TiposRetencion() {
    }

    public static TiposRetencion getTiposRetencion() {
        if (tiposRetencion == null) {
            tiposRetencion = new TiposRetencion();
        }
        return tiposRetencion;
    }

    /** Todos los tipos, o solo los activos, ordenados por id. */
    public List<TipoRetencion> listado(boolean soloActivos) throws Exception {
        String consulta = "SELECT id, nombre, porcentaje, activo FROM tipo_retencion";
        if (soloActivos) {
            consulta = consulta + " WHERE activo = 1";
        }
        consulta = consulta + " ORDER BY id";
        List<TipoRetencion> lista = new ArrayList<>();
        try (PreparedStatement sentencia = Conexion.establecerConexion().prepareStatement(consulta);
             ResultSet filas = sentencia.executeQuery()) {
            while (filas.next()) {
                lista.add(crearTipoRetencion(filas));
            }
        } catch (SQLException e) {
            throw new Exception("Error SQLite: " + e.getMessage());
        }
        return lista;
    }

    /** Un tipo por su id, o null si no está. */
    public TipoRetencion buscar(long id) throws Exception {
        String consulta = "SELECT id, nombre, porcentaje, activo FROM tipo_retencion WHERE id = ?";
        try (PreparedStatement sentencia = Conexion.establecerConexion().prepareStatement(consulta)) {
            sentencia.setLong(1, id);
            try (ResultSet filas = sentencia.executeQuery()) {
                if (filas.next()) {
                    return crearTipoRetencion(filas);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new Exception("Error SQLite: " + e.getMessage());
        }
    }

    /** El tipo que tiene ese nombre, o null si no lo tiene ninguno. */
    public TipoRetencion buscarPorNombre(String nombre) throws Exception {
        String consulta = "SELECT id, nombre, porcentaje, activo FROM tipo_retencion WHERE nombre = ?";
        try (PreparedStatement sentencia = Conexion.establecerConexion().prepareStatement(consulta)) {
            sentencia.setString(1, nombre);
            try (ResultSet filas = sentencia.executeQuery()) {
                if (filas.next()) {
                    return crearTipoRetencion(filas);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new Exception("Error SQLite: " + e.getMessage());
        }
    }

    /** Damos de alta el tipo y devolvemos el id que le ha puesto la base de datos. */
    public long alta(TipoRetencion tipo) throws Exception {
        if (tipo == null) {
            throw new Exception("Indique los datos del tipo de retención.");
        }
        comprobarNombre(tipo);
        String insertar = "INSERT INTO tipo_retencion (nombre, porcentaje, activo) VALUES (?, ?, ?)";
        try (PreparedStatement sentencia = Conexion.establecerConexion()
                .prepareStatement(insertar, Statement.RETURN_GENERATED_KEYS)) {
            ponerDatos(sentencia, tipo);
            int filas = sentencia.executeUpdate();
            if (filas == 0) {
                throw new Exception("No se ha podido guardar el tipo de retención.");
            }
            try (ResultSet claves = sentencia.getGeneratedKeys()) {
                claves.next();
                return claves.getLong(1);
            }
        } catch (SQLException e) {
            throw new Exception("Error SQLite: " + e.getMessage());
        }
    }

    /** Guardamos los cambios de un tipo de retención, sin dejar cambiar lo que estropearía el histórico. */
    public void modificar(TipoRetencion tipo) throws Exception {
        if (tipo == null) {
            throw new Exception("Indique los datos del tipo de retención.");
        }
        comprobarNombre(tipo);
        TipoRetencion actual = buscar(tipo.getId());
        if (actual == null) {
            throw new Exception("No se ha encontrado el tipo de retención.");
        }
        if (enUso(tipo.getId()) && !mismoPorcentaje(actual, tipo)) {
            throw new Exception("El porcentaje de un tipo que ya aparece en facturas no se puede modificar.");
        }
        String actualizar = "UPDATE tipo_retencion SET nombre = ?, porcentaje = ?, activo = ? WHERE id = ?";
        try (PreparedStatement sentencia = Conexion.establecerConexion().prepareStatement(actualizar)) {
            ponerDatos(sentencia, tipo);
            sentencia.setLong(4, tipo.getId());
            int filas = sentencia.executeUpdate();
            if (filas == 0) {
                throw new Exception("No se ha podido guardar el tipo de retención.");
            }
        } catch (SQLException e) {
            throw new Exception("Error SQLite: " + e.getMessage());
        }
    }

    /** Borramos el tipo, que no puede aparecer en ninguna factura. */
    public void baja(long id) throws Exception {
        if (enUso(id)) {
            throw new Exception("El tipo ya aparece en facturas y no se puede eliminar. Desactívalo en su ficha.");
        }
        String borrar = "DELETE FROM tipo_retencion WHERE id = ?";
        try (PreparedStatement sentencia = Conexion.establecerConexion().prepareStatement(borrar)) {
            sentencia.setLong(1, id);
            int filas = sentencia.executeUpdate();
            if (filas == 0) {
                throw new Exception("No se ha encontrado el tipo de retención.");
            }
        } catch (SQLException e) {
            throw new Exception("Error SQLite: " + e.getMessage());
        }
    }

    /** True si el tipo aparece en versiones del histórico (entonces ya no se puede tocar su porcentaje). */
    public boolean enUso(long id) throws Exception {
        String consulta = "SELECT COUNT(*) FROM factura_version WHERE tipo_retencion_id = ?";
        try (PreparedStatement sentencia = Conexion.establecerConexion().prepareStatement(consulta)) {
            sentencia.setLong(1, id);
            try (ResultSet filas = sentencia.executeQuery()) {
                if (filas.next()) {
                    return filas.getInt(1) > 0;
                }
                return false;
            }
        } catch (SQLException e) {
            throw new Exception("Error SQLite: " + e.getMessage());
        }
    }

    /** Ningún otro tipo puede tener este nombre. */
    private void comprobarNombre(TipoRetencion tipo) throws Exception {
        TipoRetencion otro = buscarPorNombre(tipo.getNombre());
        if (otro != null && !otro.getId().equals(tipo.getId())) {
            throw new Exception(String.format("Ya existe un tipo de retención con el nombre %s.", otro.getNombre()));
        }
    }

    /** Ponemos los tres datos del tipo en la sentencia, para el alta y la modificación. */
    private void ponerDatos(PreparedStatement sentencia, TipoRetencion tipo) throws SQLException {
        sentencia.setString(1, tipo.getNombre());
        sentencia.setInt(2, tipo.getPorcentaje());
        int activo = 0;
        if (tipo.isActivo()) {
            activo = 1;
        }
        sentencia.setInt(3, activo);
    }

    /** Decimos si los dos tipos llevan el mismo porcentaje. */
    private boolean mismoPorcentaje(TipoRetencion actual, TipoRetencion tipo) {
        return actual.getPorcentaje().equals(tipo.getPorcentaje());
    }

    /** Pasamos la fila a objeto: el constructor con los obligatorios y después los opcionales. */
    private TipoRetencion crearTipoRetencion(ResultSet fila) throws Exception {
        TipoRetencion tipo = new TipoRetencion(fila.getString("nombre"), fila.getInt("porcentaje"));
        tipo.setId(fila.getLong("id"));
        tipo.setActivo(fila.getInt("activo") == 1);
        return tipo;
    }
}
