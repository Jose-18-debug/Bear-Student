package bearstudent.logica.reportes;

import bearstudent.logica.ValidacionException;
import bearstudent.logica.reportes.GeneradorReportes.Reporte;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ExportadorReportesTest {
    private GeneradorReportes generador;

    @BeforeEach
    void preparar() {
        generador = EscenarioReportes.crear();
    }

    @Test
    void jsonDeGrupoTraeEstudiantesYEstadisticas() {
        String json = ExportadorReportes.aJson(generador.reportePorGrupo("302"));
        assertTrue(json.contains("\"tipo\": \"grupo\""));
        assertTrue(json.contains("\"clave\": \"302\""));
        assertTrue(json.contains("{\"matricula\": \"A001\", \"nombre\": \"Ana Pérez\", \"grupo\": \"302\", "
                + "\"promedio\": 9.00, \"estado\": \"APROBADO\", \"enRiesgo\": false}"));
        assertTrue(json.contains("\"matricula\": \"A004\", \"nombre\": \"Juan López\", \"grupo\": \"302\", "
                + "\"promedio\": null, \"estado\": \"SIN_EVALUAR\""));
        assertTrue(json.contains("\"porcentajeAprobacion\": 50.00"));
    }

    @Test
    void jsonEscapaComillas() {
        String json = ExportadorReportes.aJson(generador.reportePorGrupo("301"));
        assertTrue(json.contains("\"nombre\": \"Ruiz, Sofía \\\"Sofi\\\"\""));
    }

    @Test
    void jsonSinCalificacionesDejaEstadisticasEnNull() {
        String json = ExportadorReportes.aJson(generador.reportePorAsignatura("HIS200"));
        assertTrue(json.contains("\"promedioGeneral\": null"));
        assertTrue(json.contains("\"porcentajeAprobacion\": null"));
    }

    @Test
    void csvTieneEncabezadoFilasYEstadisticas() {
        String[] lineas = ExportadorReportes.aCsv(generador.reportePorAsignatura("MAT101")).split("\r\n");
        assertEquals("matricula,nombre,grupo,promedio,estado,en_riesgo", lineas[0]);
        assertEquals("A001,Ana Pérez,302,9.00,APROBADO,no", lineas[1]);
        assertEquals("A003,\"Ruiz, Sofía \"\"Sofi\"\"\",301,7.20,APROBADO,si", lineas[2]);
        assertEquals("A002,Luis Hernández,302,6.00,REPROBADO,si", lineas[3]);
        assertEquals("", lineas[4]);
        assertEquals("estadistica,valor", lineas[5]);
        assertEquals("totalEstudiantes,3", lineas[6]);
        assertEquals("porcentajeAprobacion,66.67", lineas[lineas.length - 1]);
    }

    @Test
    void exportaArchivosSegunLaExtension(@TempDir Path carpeta) throws Exception {
        Reporte r = generador.reporteGeneral();

        Path json = carpeta.resolve("reporte.json");
        ExportadorReportes.exportar(r, json);
        assertEquals(ExportadorReportes.aJson(r), Files.readString(json, StandardCharsets.UTF_8));

        Path csv = carpeta.resolve("reporte.CSV");
        ExportadorReportes.exportar(r, csv);
        assertEquals("﻿" + ExportadorReportes.aCsv(r), Files.readString(csv, StandardCharsets.UTF_8));

        ValidacionException e = assertThrows(ValidacionException.class,
                () -> ExportadorReportes.exportar(r, carpeta.resolve("reporte.txt")));
        assertEquals("El archivo debe terminar en .json o .csv", e.getMessage());
    }

    @Test
    void nombreSugeridoSegunElTipo() {
        assertEquals("reporte-general.json", ExportadorReportes.nombreSugerido(generador.reporteGeneral(), "json"));
        assertEquals("reporte-grupo-302.csv", ExportadorReportes.nombreSugerido(generador.reportePorGrupo("302"), "csv"));
        assertEquals("reporte-asignatura-MAT101.csv",
                ExportadorReportes.nombreSugerido(generador.reportePorAsignatura("MAT101"), "csv"));
    }
}
