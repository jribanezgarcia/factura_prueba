package com.alcazaba.facturacion.db;

import com.alcazaba.facturacion.service.EmpresaManager;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Carga la empresa de demostración: la recrea desde cero (si ya existía
 * se elimina antes, así que ejecutar dos veces no duplica nada), aplica
 * las migraciones y ejecuta {@code db/seed_demo.sql}.
 */
public final class CargarDemo {

    private static final String SLUG = "demo";

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

    public static void main(String[] args) throws Exception {
        Path carpeta = Database.baseDataDir().resolve(SLUG);
        if (Files.exists(carpeta)) {
            EmpresaManager.eliminarEmpresa(SLUG);
            if (Files.exists(carpeta)) {
                System.out.println("No se ha podido eliminar la empresa de demostración: "
                        + "cierra la aplicación antes de cargarla.");
                return;
            }
            System.out.println("Empresa demo anterior eliminada.");
        }

        EmpresaManager.crearEmpresa("Demo");
        EmpresaManager.registrarNombre(SLUG, "Empresa Demo S.L.");

        String sql;
        try (InputStream in = CargarDemo.class.getClassLoader().getResourceAsStream("db/seed_demo.sql")) {
            if (in == null) {
                throw new IllegalStateException("seed_demo.sql no encontrado en recursos");
            }
            sql = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }

        Database.setEmpresaActiva(SLUG);
        int sentencias = 0;
        try {
            Connection c = Database.getConnection();
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
        } finally {
            Database.resetConnection();
        }

        int facturas = 0;
        Database.setEmpresaActiva(SLUG);
        try {
            Connection c = Database.getConnection();
            try (Statement st = c.createStatement();
                 ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM factura")) {
                rs.next();
                facturas = rs.getInt(1);
            }
        } finally {
            Database.resetConnection();
        }
        System.out.println("Demostración cargada: " + facturas + " facturas en "
                + Database.dbPathDe(SLUG) + " (" + sentencias + " sentencias).");
    }
}
