package com.alcazaba.facturacion.model;

public enum EstadoFactura {
    EMITIDA("Emitida"),
    ANULADA("Anulada");

    private final String label;

    EstadoFactura(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public static EstadoFactura from(String value) {
        for (EstadoFactura e : values()) {
            if (e.name().equalsIgnoreCase(value)) {
                return e;
            }
        }
        return EMITIDA;
    }
}
