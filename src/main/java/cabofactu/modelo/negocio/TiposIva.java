package cabofactu.modelo.negocio;

import cabofactu.modelo.dominio.TipoIva;
import cabofactu.modelo.negocio.sqlite.Conexion;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * Los tipos de IVA de la empresa activa: es el único sitio con el SQL de la
 * tabla tipo_iva.
 */
public class TiposIva {

    private static TiposIva tiposIva;

    private TiposIva() {
    }

    public static TiposIva getTiposIva() {
        if (tiposIva == null) {
            tiposIva = new TiposIva();
        }
        return tiposIva;
    }

    /** Todos los tipos, o solo los activos, ordenados por id. */
    public List<TipoIva> listado(boolean soloActivos) throws Exception {
        String consulta = "SELECT id, nombre, porcentaje, motivo_exencion, activo, es_suplido "
                + "FROM tipo_iva";
        if (soloActivos) {
            consulta = consulta + " WHERE activo = 1";
        }
        consulta = consulta + " ORDER BY id";
        List<TipoIva> lista = new ArrayList<>();
        try (PreparedStatement sentencia = Conexion.establecerConexion().prepareStatement(consulta);
             ResultSet filas = sentencia.executeQuery()) {
            while (filas.next()) {
                lista.add(crearTipoIva(filas));
            }
        } catch (SQLException e) {
            throw new Exception("Error SQLite: " + e.getMessage());
        }
        return lista;
    }

    /** Un tipo por su id, o null si no está. */
    public TipoIva buscar(long id) throws Exception {
        String consulta = "SELECT id, nombre, porcentaje, motivo_exencion, activo, es_suplido "
                + "FROM tipo_iva WHERE id = ?";
        try (PreparedStatement sentencia = Conexion.establecerConexion().prepareStatement(consulta)) {
            sentencia.setLong(1, id);
            try (ResultSet filas = sentencia.executeQuery()) {
                if (filas.next()) {
                    return crearTipoIva(filas);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new Exception("Error SQLite: " + e.getMessage());
        }
    }

    /** Damos de alta el tipo y devolvemos el id que le ha puesto la base de datos. */
    public long alta(TipoIva tipo) throws Exception {
        if (tipo == null) {
            throw new Exception("Indique los datos del tipo de IVA.");
        }
        String insertar = "INSERT INTO tipo_iva (nombre, porcentaje, motivo_exencion, activo, es_suplido) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement sentencia = Conexion.establecerConexion()
                .prepareStatement(insertar, Statement.RETURN_GENERATED_KEYS)) {
            ponerDatos(sentencia, tipo);
            int filas = sentencia.executeUpdate();
            if (filas == 0) {
                throw new Exception("No se ha podido guardar el tipo de IVA.");
            }
            try (ResultSet claves = sentencia.getGeneratedKeys()) {
                claves.next();
                return claves.getLong(1);
            }
        } catch (SQLException e) {
            throw new Exception("Error SQLite: " + e.getMessage());
        }
    }

    /** Guardamos los cambios de un tipo de IVA, sin dejar cambiar lo que estropearía el histórico. */
    public void modificar(TipoIva tipo) throws Exception {
        TipoIva actual = buscar(tipo.getId());
        if (actual == null) {
            throw new Exception("No se ha encontrado el tipo de IVA.");
        }
        if (actual.isEsSuplido() != tipo.isEsSuplido()) {
            throw new Exception("Un tipo existente no puede convertirse en suplido ni dejar de serlo.");
        }
        if (actual.isExento() != tipo.isExento()) {
            throw new Exception("Un tipo existente no puede pasar de porcentaje a exento ni al revés.");
        }
        if (enUso(tipo.getId()) && !mismoPorcentaje(actual, tipo)) {
            throw new Exception("El porcentaje de un tipo que ya aparece en facturas no se puede modificar.");
        }
        String actualizar = "UPDATE tipo_iva SET nombre = ?, porcentaje = ?, motivo_exencion = ?, "
                + "activo = ?, es_suplido = ? WHERE id = ?";
        try (PreparedStatement sentencia = Conexion.establecerConexion().prepareStatement(actualizar)) {
            ponerDatos(sentencia, tipo);
            sentencia.setLong(6, tipo.getId());
            int filas = sentencia.executeUpdate();
            if (filas == 0) {
                throw new Exception("No se ha podido guardar el tipo de IVA.");
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
        String borrar = "DELETE FROM tipo_iva WHERE id = ?";
        try (PreparedStatement sentencia = Conexion.establecerConexion().prepareStatement(borrar)) {
            sentencia.setLong(1, id);
            int filas = sentencia.executeUpdate();
            if (filas == 0) {
                throw new Exception("No se ha encontrado el tipo de IVA.");
            }
        } catch (SQLException e) {
            throw new Exception("Error SQLite: " + e.getMessage());
        }
    }

    /**
     * True si el tipo de IVA aparece en líneas del histórico (entonces ya no
     * se puede tocar su porcentaje ni pasarlo a exento).
     */
    public boolean enUso(long id) throws Exception {
        String consulta = "SELECT COUNT(*) FROM factura_linea WHERE tipo_iva_id = ?";
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

    /** Ponemos los cinco datos del tipo en la sentencia, para el alta y la modificación. */
    private void ponerDatos(PreparedStatement sentencia, TipoIva tipo) throws SQLException {
        sentencia.setString(1, tipo.getNombre());
        if (tipo.isExento()) {
            sentencia.setNull(2, Types.INTEGER);
        } else {
            sentencia.setInt(2, tipo.getPorcentaje());
        }
        sentencia.setString(3, tipo.getMotivoExencion());
        int activo = 0;
        if (tipo.isActivo()) {
            activo = 1;
        }
        sentencia.setInt(4, activo);
        int suplido = 0;
        if (tipo.isEsSuplido()) {
            suplido = 1;
        }
        sentencia.setInt(5, suplido);
    }

    /** Decimos si los dos tipos llevan el mismo porcentaje, contando el exento. */
    private boolean mismoPorcentaje(TipoIva actual, TipoIva tipo) {
        if (actual.getPorcentaje() == null) {
            return tipo.getPorcentaje() == null;
        }
        return actual.getPorcentaje().equals(tipo.getPorcentaje());
    }

    /** Pasamos la fila a objeto: el constructor con los obligatorios y después los opcionales. */
    private TipoIva crearTipoIva(ResultSet fila) throws Exception {
        int porcentaje = fila.getInt("porcentaje");
        Integer valor = null;
        if (!fila.wasNull()) {
            valor = porcentaje;
        }
        TipoIva tipo = new TipoIva(fila.getString("nombre"), valor, false);
        tipo.setId(fila.getLong("id"));
        tipo.setMotivoExencion(fila.getString("motivo_exencion"));
        tipo.setActivo(fila.getInt("activo") == 1);
        tipo.setEsSuplido(fila.getInt("es_suplido") == 1);
        return tipo;
    }
}
