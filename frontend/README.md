# Super Metroid Map Randomizer - Frontend

Vue.js 3 single-page application for the Super Metroid Map Randomizer web interface.

## Prerequisites

- Node.js 18+ and npm

## Installation

```bash
npm install
```

## Development

Run the development server:

```bash
npm run dev
```

The Vite dev server will run on http://localhost:5173 and proxy API requests to the backend on http://localhost:8080.

## Building

Build for production:

```bash
npm run build
```

The built files will be output to `../src/main/resources/static` where Spring Boot will serve them.

## Testing

Run unit tests:

```bash
npm run test
```

Run E2E tests:

```bash
npm run test:e2e
```

## Project Structure

```
src/
├── components/      # Vue components
├── services/        # API clients
├── stores/          # Pinia state management
├── router/          # Vue Router configuration
├── views/           # Page components
├── assets/          # Static assets
├── App.vue          # Root component
└── main.js          # Application entry point
```

## Technology Stack

- **Vue.js 3**: Progressive JavaScript framework
- **Vite**: Next-generation frontend tooling
- **Vue Router**: Official router for Vue.js
- **Pinia**: State management for Vue.js
- **Axios**: HTTP client for API requests