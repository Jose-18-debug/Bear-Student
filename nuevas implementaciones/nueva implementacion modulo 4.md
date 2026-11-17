# Persona 4 — Aprobación y estadísticas académicas

Implementado en:
- `javascript/src/estadisticas.js` + `tests/estadisticas.test.js`
- `javascript copy/src/estadisticas.js` + `tests/estadisticas.test.js`

## Módulo
Clase `EstadisticasAcademicas(gestor)` con:
- Clasificación global y por curso: `APROBADO`, `REPROBADO`, `SIN_EVALUAR`.
- Riesgo académico: `enRiesgo` si promedio válido y <= 7.5.
- Estadísticas: totales, promedio, máximo, mínimo y porcentaje de aprobación.
- Validación de clave con `ValidacionError`.

## Cambio adicional
En el registro de evaluaciones, la ponderación ahora admite el carácter `%` (ej. `40%`).

## Pruebas
Ambas carpetas pasan `npm test` en verde.