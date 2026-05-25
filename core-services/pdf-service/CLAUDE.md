# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Install dependencies
npm install

# Development (hot reload via nodemon + babel-node)
npm run dev

# Build (transpile ES6 → ES5 into dist/)
npm run build

# Production start (triggers build via prestart, 8GB heap)
npm start

# Lint (ESLint — this is the only test command)
npm test
```

There is no unit test suite; `npm test` runs ESLint only.

## Architecture

This is a **Node.js/Express microservice** (ES6, transpiled by Babel) for config-driven PDF generation within the DIGIT platform. All source lives under `src/`, compiled output goes to `dist/`.

### Request Flow

**Synchronous (`/_create`, `/_createnosave`)**:
1. `prepareBegin()` — extracts the target array from the request using `baseKeyPath` (JSONPath), then for each item: runs `directMapping`, `externalAPIMapping`, QR code generation, and localisation fetching to build a `variableTovalueMap`
2. Mustache-renders the format config using that map to produce a pdfmake document definition
3. `createPdfBinary()` — renders each doc definition via pdfmake, streams to buffer; `/_create` uploads to filestore and inserts a DB record, `/_createnosave` returns the binary directly

**Asynchronous (Kafka)**:
- Consumer listens on `PDF_GEN_RECEIVE` topic, calls `createNoSave()` for each message writing PDFs to local disk (`/mnt/pdf/{jobId}/`), then merges all into a single PDF via `mergePdf.js` and uploads to filestore

### Configuration System

The service is **entirely config-driven** — no code changes are needed to add new PDF templates. Two config types are loaded at startup from URLs (supports `file://`, `http://`, `https://`):

- **Data config** (`DATA_CONFIG_URLS`): defines `key`, `baseKeyPath`, `entityIdPath`, and variable `mappings`. Mapping types:
  - `direct` — JSONPath extraction from the request body
  - `externalApi` — HTTP call to a dependent service with query params
  - `derived` — arithmetic/string operations on already-resolved variables
  - `QRCode` — generate QR code image from a string
- **Format config** (`FORMAT_CONFIG_URLS`): pdfmake document definition (content, styles, fonts, footer). The footer field is a serialized function; pdfmake requires it restored from string after JSON round-trips.

### Key Source Files

| File | Purpose |
|------|---------|
| `src/index.js` | All Express routes + core PDF generation logic (`prepareBegin`, `createAndSave`, `createNoSave`, `createPdfBinary`) |
| `src/EnvironmentVariables.js` | All env var defaults (edit here for local overrides, but prefer actual env vars) |
| `src/queries.js` | PostgreSQL operations for PDF job metadata |
| `src/utils/directMapping.js` | JSONPath-based variable extraction from request data |
| `src/utils/externalAPIMapping.js` | HTTP calls to dependent services + derived value computation |
| `src/utils/commons.js` | Localisation fetching (with 300s node-cache TTL) and date formatting |
| `src/utils/fileStoreAPICall.js` | Upload PDFs to egov-filestore, retrieve signed URLs |
| `src/kafka/consumer.js` | Async bulk PDF processing queue (async.js, concurrency=1) |
| `src/kafka/producer.js` | Publishes to `PDF_GEN_CREATE` and `PDF_GEN_ERROR` topics |

### External Dependencies (must be reachable)

| Service | Env Var | Purpose |
|---------|---------|---------|
| egov-localization | `EGOV_LOCALISATION_HOST` | Multi-language label lookup |
| egov-filestore | `EGOV_FILESTORE_SERVICE_HOST` | PDF persistence and signed URL generation |
| PostgreSQL | `DB_HOST`, `DB_NAME`, `DB_USER`, `DB_PASSWORD` | Job metadata (schema: `egov_pdf_gen`) |
| Kafka | `KAFKA_BROKER_HOST` | Async bulk processing (`PDF_GEN_RECEIVE` / `PDF_GEN_CREATE` topics) |

### Notable Implementation Details

- `mustache.escape` is set to identity (`text => text`) — HTML escaping is intentionally disabled for PDF content
- PDFs exceeding `MAX_NUMBER_PAGES` (default: 80) are split into multiple files before upload
- The footer field in format config is a function serialized as a string; `createPdfBinary()` must reconstruct it via `eval`-style restoration before passing to pdfmake
- Database schema prefix is tenant-aware: `{schema}.egov_pdf_gen` — the schema is derived from `tenantId` based on `STATE_SCHEMA_INDEX_POSITION_TENANTID`
- `IS_ENVIRONMENT_CENTRAL_INSTANCE=true` enables hierarchical multi-tenancy (state + ULB tenants sharing a single DB)

### Local Development

Set `DATA_CONFIG_URLS` and `FORMAT_CONFIG_URLS` to `file:///absolute/path/to/config.json` in `src/EnvironmentVariables.js` for local file-based configs. Sample configs are available in the [egovernments/configs](https://github.com/egovernments/configs/tree/master/pdf-service) repository.
