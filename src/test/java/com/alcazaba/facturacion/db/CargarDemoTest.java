package com.alcazaba.facturacion.db;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CargarDemoTest {

    @Test
    void sentenciaSimple() {
        assertEquals(List.of("SELECT 1", ""), CargarDemo.trocear("SELECT 1;"));
    }

    @Test
    void dosSentencias() {
        assertEquals(List.of("SELECT 1", " SELECT 2", ""), CargarDemo.trocear("SELECT 1; SELECT 2;"));
    }

    @Test
    void puntoYComaDentroDeCadenaNoCorta() {
        assertEquals(List.of("INSERT INTO t VALUES ('Montaje; incluye transporte')", ""),
                CargarDemo.trocear("INSERT INTO t VALUES ('Montaje; incluye transporte');"));
    }

    @Test
    void comillaEscapadaNoCortaLaCadena() {
        assertEquals(List.of("INSERT INTO t VALUES ('L''Hospitalet; centro')", ""),
                CargarDemo.trocear("INSERT INTO t VALUES ('L''Hospitalet; centro');"));
    }
}
