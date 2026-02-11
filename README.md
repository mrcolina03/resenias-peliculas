# Reseñas de Películas (Frontend + Backend)

Proyecto full stack para administración de películas, cines, usuarios y reseñas con experiencia reactiva en vivo por película.

## ¿Qué contiene el proyecto?

- **Backend (`proyecto-resenas-backend`)**
  - API REST con Spring Boot 3 + JPA.
  - Seguridad con JWT.
  - Gestión de:
    - Usuarios
    - Películas
    - Cines
    - Reseñas
  - Flujo reactivo para reseñas en vivo por película mediante **SSE**.
  - Simulador de chat y laboratorio de backpressure.

- **Frontend (`proyecto-resenas-frontend`)**
  - Aplicación React + Vite.
  - Páginas de autenticación, administración y detalle de película.
  - Vista de reseñas tipo **chat en vivo** dentro del detalle de cada película.

## Requisitos para ejecutar en cualquier PC

- Git
- Java 21
- Node.js 20+ y npm
- (Opcional) MySQL; por defecto puedes usar configuración local según `application.yml`.

## 1) Clonar el repositorio

```bash
git clone https://github.com/mrcolina03/resenias-peliculas.git
cd resenias-peliculas
```

> Cuando estos cambios estén integrados, estarán en la rama `main`.

## 2) Ejecutar backend

```bash
cd proyecto-resenas-backend
./gradlew bootRun
```

Backend disponible en: `http://localhost:8080`

## 3) Ejecutar frontend

En otra terminal:

```bash
cd proyecto-resenas-frontend
npm install
npm run dev
```

Frontend disponible en: `http://localhost:5173` (o el puerto que Vite asigne, por ejemplo `5174`).

## 4) Variables de entorno del frontend

Crear un archivo `.env` en `proyecto-resenas-frontend` con:

```bash
VITE_API_BASE_URL=http://localhost:8080
```

## 5) Ejecutar pruebas unitarias del backend

```bash
cd proyecto-resenas-backend
./gradlew test
```

## Funcionalidad destacada: reseñas en vivo por película

En la página de detalle de película:

- Las reseñas se renderizan con estilo de chat.
- El cliente abre un stream SSE por película.
- Cada reseña nueva aparece en tiempo real para todos los usuarios conectados a esa sala.

## Estructura general

```text
resenias-peliculas/
├── proyecto-resenas-backend/
└── proyecto-resenas-frontend/
```
