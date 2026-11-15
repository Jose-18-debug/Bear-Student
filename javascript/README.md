# Bear Student para JavaScript

## Abrir la interfaz sin terminal
Dar doble clic a `javascript/abrir-bear-student.html`. Se abre en el navegador y no necesita servidor
(la fuente Nunito se descarga de internet; sin conexión se usa Segoe UI o Arial).

Ese archivo se genera a partir de `src/`. Si cambias algo en `src/`, vuelve a generarlo antes del commit:

    cd javascript
    npm run empaquetar

Si se te olvida, `npm test` falla avisando que está desactualizado.

## Desarrollo
Requisitos: Node.js 20 o superior y Python 3, solo para el servidor local de la interfaz

## Pruebas
    cd javascript
    npm test

## Interfaz
    cd javascript
    python -m http.server 8000

Abrir http://localhost:8000/src/ui/index.html