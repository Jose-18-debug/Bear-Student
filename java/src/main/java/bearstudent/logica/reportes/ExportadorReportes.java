package bearstudent.logica.reportes;

import bearstudent.logica.ValidacionException;
import bearstudent.logica.estadisticas.EstadisticasAcademicasUseCase.ClasificacionEstudiante;
import bearstudent.logica.estadisticas.EstadisticasAcademicasUseCase.ResumenEstadistico;
import bearstudent.logica.reportes.GeneradorReportes.Reporte;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Módulo de la Persona 5: convierte un Reporte a JSON o CSV y lo guarda en un archivo.
 * No usa librerías externas: el texto se arma a mano (con el escape correcto de comillas, comas, etc.).
 * Los números se escriben con punto decimal y 2 decimales; un promedio sin notas queda null (JSON) o vacío (CSV).
 */
public final class ExportadorReportes {

    private static final String FIN_CSV = "\r\n";   // fin de línea estándar de CSV (RFC 4180)

    private ExportadorReportes() { }

    // ---------- JSON ----------

    public static String aJson(Reporte r) {
        StringBuilder sb = new StringBuilder("{\n");
        sb.append("  \"tipo\": ").append(texto(r.tipo().name().toLowerCase(Locale.ROOT))).append(",\n");
        sb.append("  \"clave\": ").append(texto(r.clave())).append(",\n");
        sb.append("  \"titulo\": ").append(texto(r.titulo())).append(",\n");

        sb.append("  \"estudiantes\": [");
        List<ClasificacionEstudiante> lista = r.estudiantes();
        for (int i = 0; i < lista.size(); i++) {
            ClasificacionEstudiante c = lista.get(i);
            sb.append(i == 0 ? "\n" : ",\n");
            sb.append("    {\"matricula\": ").append(texto(c.matricula()))
                    .append(", \"nombre\": ").append(texto(c.nombre()))
                    .append(", \"grupo\": ").append(texto(c.grupo()))
                    .append(", \"promedio\": ").append(valorJson(c.promedio()))
                    .append(", \"estado\": ").append(texto(c.estado().name()))
                    .append(", \"enRiesgo\": ").append(c.enRiesgo())
                    .append('}');
        }
        sb.append(lista.isEmpty() ? "],\n" : "\n  ],\n");

        sb.append("  \"estadisticas\": {");
        boolean primero = true;
        for (Map.Entry<String, Object> e : estadisticas(r.resumen()).entrySet()) {
            sb.append(primero ? "\n" : ",\n");
            sb.append("    ").append(texto(e.getKey())).append(": ").append(valorJson(e.getValue()));
            primero = false;
        }
        sb.append("\n  }\n}\n");
        return sb.toString();
    }

    // ---------- CSV ----------

    /** Primero una fila por estudiante; después, separadas por una línea vacía, las estadísticas. */
    public static String aCsv(Reporte r) {
        StringBuilder sb = new StringBuilder();
        sb.append("matricula,nombre,grupo,promedio,estado,en_riesgo").append(FIN_CSV);
        for (ClasificacionEstudiante c : r.estudiantes()) {
            sb.append(String.join(",",
                    campo(c.matricula()), campo(c.nombre()), campo(c.grupo()),
                    valorCsv(c.promedio()), c.estado().name(), c.enRiesgo() ? "si" : "no"))
                    .append(FIN_CSV);
        }
        sb.append(FIN_CSV);
        sb.append("estadistica,valor").append(FIN_CSV);
        for (Map.Entry<String, Object> e : estadisticas(r.resumen()).entrySet()) {
            sb.append(campo(e.getKey())).append(',').append(valorCsv(e.getValue())).append(FIN_CSV);
        }
        return sb.toString();
    }

    // ---------- Archivos ----------

    /** Guarda el reporte; el formato sale de la extensión del archivo (.json o .csv). */
    public static void exportar(Reporte r, Path archivo) throws IOException {
        String nombre = archivo.getFileName() == null ? "" : archivo.getFileName().toString().toLowerCase(Locale.ROOT);
        if (nombre.endsWith(".json")) {
            Files.writeString(archivo, aJson(r), StandardCharsets.UTF_8);
        } else if (nombre.endsWith(".csv")) {
            // El BOM al inicio hace que Excel lea bien los acentos (UTF-8)
            Files.writeString(archivo, "﻿" + aCsv(r), StandardCharsets.UTF_8);
        } else {
            throw new ValidacionException("El archivo debe terminar en .json o .csv");
        }
    }

    /** Nombre de archivo sugerido, p. ej. reporte-grupo-302.csv o reporte-asignatura-MAT101.json. */
    public static String nombreSugerido(Reporte r, String extension) {
        String base = "reporte-" + r.tipo().name().toLowerCase(Locale.ROOT);
        if (!r.clave().isEmpty()) {
            base += "-" + r.clave().replaceAll("[^A-Za-z0-9_-]", "_");
        }
        return base + "." + extension;
    }

    // ---------- Apoyo ----------

    /** Estadísticas en orden fijo; los valores que dependen de calificaciones quedan null si nadie tiene notas. */
    private static Map<String, Object> estadisticas(ResumenEstadistico s) {
        boolean hayNotas = s.totalEvaluados() > 0;
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("totalEstudiantes", s.totalEstudiantes());
        m.put("totalEvaluados", s.totalEvaluados());
        m.put("aprobados", s.totalAprobados());
        m.put("reprobados", s.totalReprobados());
        m.put("enRiesgo", s.totalEnRiesgo());
        m.put("promedioGeneral", hayNotas ? s.promedioGeneral() : null);
        m.put("promedioMaximo", hayNotas ? s.promedioMaximo() : null);
        m.put("promedioMinimo", hayNotas ? s.promedioMinimo() : null);
        m.put("porcentajeAprobacion", hayNotas ? s.porcentajeAprobacion() : null);
        return m;
    }

    private static String valorJson(Object valor) {
        if (valor == null) return "null";
        if (valor instanceof Double d) return dosDecimales(d);
        return valor.toString();
    }

    private static String valorCsv(Object valor) {
        if (valor == null) return "";
        if (valor instanceof Double d) return dosDecimales(d);
        return valor.toString();
    }

    private static String dosDecimales(double d) {
        return String.format(Locale.US, "%.2f", d);
    }

    /** Texto JSON entre comillas, escapando comillas, diagonales invertidas y caracteres de control. */
    private static String texto(String s) {
        StringBuilder sb = new StringBuilder("\"");
        for (char ch : s.toCharArray()) {
            switch (ch) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (ch < 0x20) sb.append(String.format("\\u%04x", (int) ch));
                    else sb.append(ch);
                }
            }
        }
        return sb.append('"').toString();
    }

    /** Campo CSV: si trae coma, comillas o salto de línea, va entre comillas y las comillas se duplican. */
    private static String campo(String s) {
        if (s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }
}
