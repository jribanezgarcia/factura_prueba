package com.alcazaba.facturacion.db;

import java.sql.SQLException;

public class DatosException extends RuntimeException {

    public DatosException(SQLException causa) {
        super(causa.getMessage(), causa);
    }

    public DatosException(String mensaje) {
        super(mensaje);
    }

    public DatosException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
