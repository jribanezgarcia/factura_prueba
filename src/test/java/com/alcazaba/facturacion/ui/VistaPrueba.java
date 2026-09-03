package com.alcazaba.facturacion.ui;

/**
 * Vista minima para los tests de navegacion: su `puedeCerrar()` se gobierna
 * con el flag estatico `bloquear`.
 */
public class VistaPrueba implements Vista {

    static volatile boolean bloquear;

    @Override
    public boolean puedeCerrar() {
        return !bloquear;
    }
}
