package cabofactu.modelo.negocio;

import cabofactu.modelo.dominio.Cliente;
import cabofactu.modelo.negocio.sqlite.Conexion;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Los clientes de la empresa activa: es el único sitio con el SQL de la
 * tabla cliente.
 */
public class Clientes {

    private static Clientes clientes;

    private Clientes() {
    }

    public static Clientes getClientes() {
        if (clientes == null) {
            clientes = new Clientes();
        }
        return clientes;
    }

    /** Todos los clientes, o solo los activos, ordenados por nombre. */
    public List<Cliente> listado(boolean soloActivos) throws Exception {
        String consulta = "SELECT id, nombre, nif, direccion, cp, localidad, provincia, email, activo "
                + "FROM cliente";
        if (soloActivos) {
            consulta = consulta + " WHERE activo = 1";
        }
        consulta = consulta + " ORDER BY nombre";
        List<Cliente> lista = new ArrayList<>();
        try (PreparedStatement sentencia = Conexion.establecerConexion().prepareStatement(consulta);
             ResultSet filas = sentencia.executeQuery()) {
            while (filas.next()) {
                lista.add(crearCliente(filas));
            }
        } catch (SQLException e) {
            throw new Exception("Error SQLite: " + e.getMessage());
        }
        return lista;
    }

    /** Clientes que coinciden por nombre o NIF, hasta cien, o solo los activos. */
    public List<Cliente> listado(String texto, boolean soloActivos) throws Exception {
        String consulta = "SELECT id, nombre, nif, direccion, cp, localidad, provincia, email, activo "
                + "FROM cliente WHERE (nombre LIKE ? OR nif LIKE ?)";
        if (soloActivos) {
            consulta = consulta + " AND activo = 1";
        }
        consulta = consulta + " ORDER BY nombre LIMIT 100";
        String parecido = "%" + (texto == null ? "" : texto.trim()) + "%";
        List<Cliente> lista = new ArrayList<>();
        try (PreparedStatement sentencia = Conexion.establecerConexion().prepareStatement(consulta)) {
            sentencia.setString(1, parecido);
            sentencia.setString(2, parecido);
            try (ResultSet filas = sentencia.executeQuery()) {
                while (filas.next()) {
                    lista.add(crearCliente(filas));
                }
            }
        } catch (SQLException e) {
            throw new Exception("Error SQLite: " + e.getMessage());
        }
        return lista;
    }

    /** Un cliente por su id, o null si no está. */
    public Cliente buscar(long id) throws Exception {
        String consulta = "SELECT id, nombre, nif, direccion, cp, localidad, provincia, email, activo "
                + "FROM cliente WHERE id = ?";
        try (PreparedStatement sentencia = Conexion.establecerConexion().prepareStatement(consulta)) {
            sentencia.setLong(1, id);
            try (ResultSet filas = sentencia.executeQuery()) {
                if (filas.next()) {
                    return crearCliente(filas);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new Exception("Error SQLite: " + e.getMessage());
        }
    }

    /** El cliente que tiene ese NIF, esté activo o no, o null si no lo tiene ninguno. */
    public Cliente buscarPorNif(String nif) throws Exception {
        String consulta = "SELECT id, nombre, nif, direccion, cp, localidad, provincia, email, activo "
                + "FROM cliente WHERE nif = ?";
        try (PreparedStatement sentencia = Conexion.establecerConexion().prepareStatement(consulta)) {
            sentencia.setString(1, nif);
            try (ResultSet filas = sentencia.executeQuery()) {
                if (filas.next()) {
                    return crearCliente(filas);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new Exception("Error SQLite: " + e.getMessage());
        }
    }

    /**
     * Damos de alta el cliente y devolvemos el id que le ha puesto la base
     * de datos.
     */
    public long alta(Cliente cliente) throws Exception {
        if (cliente == null) {
            throw new Exception("Indique los datos del cliente.");
        }
        comprobarNif(cliente);
        String insertar = """
                INSERT INTO cliente (nombre, nif, direccion, cp, localidad, provincia, email, activo)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement sentencia = Conexion.establecerConexion()
                .prepareStatement(insertar, Statement.RETURN_GENERATED_KEYS)) {
            sentencia.setString(1, cliente.getNombre());
            sentencia.setString(2, cliente.getNif());
            sentencia.setString(3, cliente.getDireccion());
            sentencia.setString(4, cliente.getCp());
            sentencia.setString(5, cliente.getLocalidad());
            sentencia.setString(6, cliente.getProvincia());
            sentencia.setString(7, cliente.getEmail());
            int activo = 0;
            if (cliente.isActivo()) {
                activo = 1;
            }
            sentencia.setInt(8, activo);
            int filas = sentencia.executeUpdate();
            if (filas == 0) {
                throw new Exception("No se ha podido guardar el cliente.");
            }
            try (ResultSet clave = sentencia.getGeneratedKeys()) {
                clave.next();
                return clave.getLong(1);
            }
        } catch (SQLException e) {
            throw new Exception("Error SQLite: " + e.getMessage());
        }
    }

    /** Guardamos los cambios de un cliente ya existente. */
    public void modificar(Cliente cliente) throws Exception {
        if (cliente == null) {
            throw new Exception("Indique los datos del cliente.");
        }
        comprobarNif(cliente);
        String actualizar = """
                UPDATE cliente SET nombre = ?, nif = ?, direccion = ?, cp = ?, localidad = ?,
                provincia = ?, email = ?, activo = ? WHERE id = ?
                """;
        try (PreparedStatement sentencia = Conexion.establecerConexion().prepareStatement(actualizar)) {
            sentencia.setString(1, cliente.getNombre());
            sentencia.setString(2, cliente.getNif());
            sentencia.setString(3, cliente.getDireccion());
            sentencia.setString(4, cliente.getCp());
            sentencia.setString(5, cliente.getLocalidad());
            sentencia.setString(6, cliente.getProvincia());
            sentencia.setString(7, cliente.getEmail());
            int activo = 0;
            if (cliente.isActivo()) {
                activo = 1;
            }
            sentencia.setInt(8, activo);
            sentencia.setLong(9, cliente.getId());
            int filas = sentencia.executeUpdate();
            if (filas == 0) {
                throw new Exception("No se ha encontrado el cliente.");
            }
        } catch (SQLException e) {
            throw new Exception("Error SQLite: " + e.getMessage());
        }
    }

    /** Borramos la fila del cliente. */
    public void baja(long id) throws Exception {
        String borrar = "DELETE FROM cliente WHERE id = ?";
        try (PreparedStatement sentencia = Conexion.establecerConexion().prepareStatement(borrar)) {
            sentencia.setLong(1, id);
            int filas = sentencia.executeUpdate();
            if (filas == 0) {
                throw new Exception("No se ha encontrado el cliente.");
            }
        } catch (SQLException e) {
            throw new Exception("Error SQLite: " + e.getMessage());
        }
    }

    /** Marcamos el cliente como inactivo. */
    public void desactivar(long id) throws Exception {
        String actualizar = "UPDATE cliente SET activo = 0 WHERE id = ?";
        try (PreparedStatement sentencia = Conexion.establecerConexion().prepareStatement(actualizar)) {
            sentencia.setLong(1, id);
            int filas = sentencia.executeUpdate();
            if (filas == 0) {
                throw new Exception("No se ha encontrado el cliente.");
            }
        } catch (SQLException e) {
            throw new Exception("Error SQLite: " + e.getMessage());
        }
    }

    /** Decimos si el cliente tiene alguna factura asociada. */
    public boolean tieneFacturas(long id) throws Exception {
        String consulta = "SELECT COUNT(*) FROM factura WHERE cliente_id = ?";
        try (PreparedStatement sentencia = Conexion.establecerConexion().prepareStatement(consulta)) {
            sentencia.setLong(1, id);
            try (ResultSet filas = sentencia.executeQuery()) {
                filas.next();
                return filas.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new Exception("Error SQLite: " + e.getMessage());
        }
    }

    /** Ningún otro cliente puede tener este NIF. Al modificar, el propio cliente no cuenta. */
    private void comprobarNif(Cliente cliente) throws Exception {
        Cliente otro = buscarPorNif(cliente.getNif());
        if (otro != null && !otro.getId().equals(cliente.getId())) {
            throw new Exception(String.format("Ya existe un cliente con el NIF %s: %s.",
                    otro.getNif(), otro.getNombre()));
        }
    }

    /**
     * Pasamos una fila de la consulta a un objeto Cliente. Como el constructor
     * comprueba los datos, una fila incompleta se convierte en un aviso.
     */
    private Cliente crearCliente(ResultSet fila) throws Exception {
        Cliente cliente = new Cliente(
                fila.getString("nombre"),
                fila.getString("nif"),
                fila.getString("direccion"),
                fila.getString("cp"),
                fila.getString("localidad"),
                fila.getString("provincia"));
        cliente.setId(fila.getLong("id"));
        cliente.setEmail(fila.getString("email"));
        cliente.setActivo(fila.getInt("activo") == 1);
        return cliente;
    }
}