package com.alcazaba.facturacion.model;

import java.time.LocalDate;

public record DatosPago(String formaPago, LocalDate vencimiento, String realizadaPor) {
}
