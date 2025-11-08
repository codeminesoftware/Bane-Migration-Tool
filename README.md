# Bane Migration Tool (Technical Assessment)

## Project Structure

- `backend/`: Spring Boot 3 application exposing the migration API.
- `frontend/`: Vue 3 + Vite SPA that consumes the API and offers a migration dashboard.

## Prerequisites

- Java 17+
- Maven 3.9+ (or compatible)
- Node.js 18+ and npm

## Running the Backend

```bash
cd backend
mvn spring-boot:run
```

The API becomes available at `http://localhost:8080`.

## Running the Frontend

```bash
cd frontend
npm install
npm run dev
```

The dashboard runs on `http://localhost:5173` and proxies API requests to the backend.

## API Overview

| Method | Endpoint                 | Description                              |
|--------|--------------------------|------------------------------------------|
| GET    | `/api/legacy/clients`    | Retrieve legacy clients and their status |
| POST   | `/api/migrate/{id}`      | Migrate a legacy client to the new system |
| GET    | `/api/new/clients`       | Retrieve migrated clients                 |

- Clients are stored in memory on application start.
- A migration marks the client as migrated in the legacy list, stores it in the migrated list, and logs a success message.
- Attempting to migrate a missing or already migrated client returns a descriptive error.

## Frontend Experience

- Legacy clients are displayed in a Vuetify data table with **Migrate** actions.
- Migrated clients appear in a live counter and list view.
- Snackbars provide feedback on success or failure.
- A refresh button re-fetches both lists from the backend.

## Technical Choices

- **Spring Boot 3 + Maven** for a lightweight REST API, automatic JSON serialization, and test support.
- **In-memory service layer** (`ClientMigrationService`) to isolate business logic and seed sample data.
- **Global exception handler** to return consistent HTTP error responses.
- **Vue 3 + Vite + Vuetify** for rapid UI development with a modern component library and theming.
- **Axios** for HTTP communication and a Vite dev server proxy to avoid manual CORS configuration.

## Testing

- `ClientMigrationServiceTest` covers the service behaviour (seeding, migration success, duplicate protection).
- Run with:

```bash
cd backend
mvn test
```

