package cabofactu.modelo.negocio.sqlite;

import cabofactu.modelo.dominio.EmpresaDisponible;
import cabofactu.modelo.negocio.Empresas;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import cabofactu.modelo.dominio.Empresa;

/**
 * Carga la empresa de demostración: la recrea desde cero (si ya existía
 * se elimina antes, así que ejecutar dos veces no duplica nada), crea sus
 * tablas y ejecuta {@code db/seed_demo.sql}.
 */
public final class CargarDemo {

    public static final String CARPETA = "demo";

    private CargarDemo() {
    }

    /**
     * Parte el script en sentencias por cada {@code ;} que esté fuera de
     * una cadena entrecomillada. En SQL la comilla simple dentro de una
     * cadena se escapa duplicándola ({@code ''}).
     */
    static List<String> trocear(String sql) {
        List<String> partes = new ArrayList<>();
        StringBuilder actual = new StringBuilder();
        boolean enCadena = false;
        for (int i = 0; i < sql.length(); i++) {
            char ch = sql.charAt(i);
            if (ch == '\'') {
                if (enCadena && i + 1 < sql.length() && sql.charAt(i + 1) == '\'') {
                    actual.append("''");
                    i++;
                    continue;
                }
                enCadena = !enCadena;
                actual.append(ch);
                continue;
            }
            if (ch == ';' && !enCadena) {
                partes.add(actual.toString());
                actual.setLength(0);
                continue;
            }
            actual.append(ch);
        }
        partes.add(actual.toString());
        return partes;
    }

    public static EmpresaDisponible cargar() throws Exception {
        EmpresaDisponible info = Empresas.getEmpresas().alta("Demo");
        Empresas.getEmpresas().registrarNombre(CARPETA, "Empresa Demo S.L.");

        String sql;
        try (InputStream in = CargarDemo.class.getClassLoader().getResourceAsStream("db/seed_demo.sql")) {
            if (in == null) {
                throw new Exception("No se encontró seed_demo.sql dentro de la aplicación.");
            }
            sql = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }

        Conexion.setEmpresaActiva(CARPETA);
        int sentencias = 0;
        try {
            Connection c = Conexion.establecerConexion();
            try (Statement st = c.createStatement()) {
                for (String sentencia : trocear(sql)) {
                    String t = sentencia.trim();
                    if (t.isEmpty()) {
                        continue;
                    }
                    st.execute(t);
                    sentencias++;
                }
            }
            copiarLogo();
        } finally {
            Conexion.cerrarConexion();
        }
        System.out.println("Demostración cargada: " + sentencias + " sentencias.");
        return info;
    }

    /**
     * Copiamos el logo de la demostración a su carpeta de datos y dejamos la
     * empresa en modo logo, para que se vea sin configurar nada.
     */
    private static void copiarLogo() throws Exception {
        Path destino = Conexion.rutaBaseDe(CARPETA).getParent().resolve("logo.png");
        try (InputStream in = CargarDemo.class.getClassLoader().getResourceAsStream("db/logo_demo.png")) {
            if (in == null) {
                throw new Exception("No se encontró el logo de la demostración dentro de la aplicación.");
            }
            Files.copy(in, destino, StandardCopyOption.REPLACE_EXISTING);
        }
        String actualizar = "UPDATE empresa SET logo_path = ?, cabecera_modo = 'LOGO' WHERE id = 1";
        try (PreparedStatement sentencia = Conexion.establecerConexion().prepareStatement(actualizar)) {
            sentencia.setString(1, destino.toAbsolutePath().toString());
            sentencia.executeUpdate();
        }
    }

    public static void main(String[] args) throws Exception {
        Path carpeta = Conexion.carpetaRaiz().resolve(CARPETA);
        if (Files.exists(carpeta)) {
            Empresas.getEmpresas().baja(CARPETA);
            if (Files.exists(carpeta)) {
                System.out.println("No se ha podido eliminar la empresa de demostración: "
                        + "cierra la aplicación antes de cargarla.");
                return;
            }
            System.out.println("Empresa demo anterior eliminada.");
        }

        cargar();

        int facturas = 0;
        Conexion.setEmpresaActiva(CARPETA);
        try {
            Connection c = Conexion.establecerConexion();
            try (Statement st = c.createStatement();
                 ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM factura")) {
                rs.next();
                facturas = rs.getInt(1);
            }
        } finally {
            Conexion.cerrarConexion();
        }
        System.out.println("Demostración cargada: " + facturas + " facturas en "
                + Conexion.rutaBaseDe(CARPETA) + ".");
    }
}
