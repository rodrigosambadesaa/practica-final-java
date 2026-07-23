# Frontend de Garaje Pro

Interfaz Angular 22 standalone, responsive y sin Zone.js. Consume la API mediante rutas relativas
`/api`, servidas por Nginx en la imagen de producción.

## Comandos

```bash
npm ci
npm start
npm test
npm run build
```

Para un entorno reproducible con la API y MySQL:

```bash
docker compose up --build -d
```

Ejecuta este último comando desde la raíz del repositorio.
