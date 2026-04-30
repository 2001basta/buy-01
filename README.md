# Marketplace Microservices — Project Context

Spring Boot 3 microservices backend + Angular 17 frontend.
Sellers can list products with images. Clients browse products.
Three core services: User, Product, Media. Spring Cloud Gateway as single entry point.

---

## Architecture

```
Angular → API Gateway → [User Service | Product Service | Media Service]
                                      ↕ Kafka (async, add later)
                               [MongoDB per service — never shared]
```

Each service has its own MongoDB database. Do NOT query across databases.
Kafka is optional — build without it first, add it after core services work.

---

## Project Structure

```
/
├── user-service/        # Auth, JWT, profiles
├── product-service/     # Product CRUD, ownership
├── media-service/       # File upload, validation
├── api-gateway/         # Spring Cloud Gateway, JWT filter
└── frontend/            # Angular 17
```

---

## Common Commands

```bash
# Run a service (from its directory)
mvn spring-boot:run

# Run all services (from root if docker-compose present)
docker-compose up

# Angular dev server
cd frontend && ng serve

# Run tests for a service
mvn test

# Build all
mvn clean install
```

---

## Tech Stack

- **Backend:** Spring Boot 3, Spring Security 6 (JWT), Spring Cloud Gateway
- **Database:** MongoDB (Mongoose-style documents, no JPA)
- **Events:** Apache Kafka (async, optional phase 2)
- **Frontend:** Angular 17+, standalone components, HttpClient
- **Auth:** JWT — stateless, no sessions

---

## Critical Business Rules — Always Enforce

1. **Only SELLER role can create/update/delete products.**
   Check `role == SELLER` before any write. Return `403 FORBIDDEN` otherwise.

2. **Only the product owner can modify it.**
   Check `product.sellerId == currentUserId`. Never skip this.

3. **File uploads: 2MB max, image/jpeg or image/png only.**
   Validate real MIME type — do not trust the file extension.

4. **Only the media owner can delete their file.**
   Check `media.ownerId == currentUserId`.

---

## JWT

Token payload: `{ sub: userId, role: "SELLER"|"CLIENT", exp: <unix> }`

- Validated at the **Gateway** before forwarding to services
- Services extract `userId` and `role` from the forwarded token/header
- Password is **never** returned in any response
- Use BCrypt for password hashing

---

## API Routes

| Path prefix       | Routes to        |
|-------------------|-----------------|
| `/api/auth/**`    | User Service    |
| `/api/users/**`   | User Service    |
| `/api/products/**`| Product Service |
| `/api/media/**`   | Media Service   |

---

## Error Response Shape

Always return consistent error JSON:

```json
{ "error": "FORBIDDEN", "message": "Only sellers can create products" }
{ "error": "FILE_TOO_LARGE", "maxSize": "2MB" }
{ "error": "UNAUTHORIZED_ACTION", "message": "You do not own this resource" }
{ "error": "INVALID_FILE_TYPE", "allowed": ["image/jpeg", "image/png"] }
```

---

## Inter-Service Calls (Sync REST)

- **Product Service → Media Service:** validate that imageIds exist before attaching
- **User Service → Media Service:** upload avatar and get back a URL

Use `WebClient` (non-blocking) or `RestTemplate` for service-to-service calls.

---

## Code Conventions

- Java: use records for DTOs, `@Valid` on request bodies, constructor injection
- No field injection (`@Autowired` on fields) — use constructor injection only
- Angular: standalone components, typed HttpClient responses, interceptor for JWT
- MongoDB: use `@Document`, `ObjectId` for ids, never expose `_id` raw — map to `id`
- Always validate ownership in the **service layer**, not just the controller

---

## Build Order (if starting fresh)

1. User Service (auth + JWT)
2. Product Service
3. Media Service
4. API Gateway (wire everything together)
5. Kafka events (after everything above works)

---

## Gotchas

- `spring-boot-starter-web` and `spring-cloud-gateway` conflict — Gateway must use **reactive** stack (`spring-boot-starter-webflux`), not servlet stack
- MongoDB `ObjectId` serializes as `{ "$oid": "..." }` in some drivers — configure Jackson to serialize as plain string
- File MIME validation: use Apache Tika or check magic bytes, not `file.getContentType()` (it trusts the client)
- JWT secret must be the same across all services — externalise to env var `JWT_SECRET`
- Angular interceptor must handle 401 responses and redirect to login

---

## Environment Variables Expected

```
JWT_SECRET=<shared-secret-min-32-chars>
MONGODB_URI_USER=mongodb://localhost:27017/userdb
MONGODB_URI_PRODUCT=mongodb://localhost:27017/productdb
MONGODB_URI_MEDIA=mongodb://localhost:27017/mediadb
UPLOAD_DIR=./uploads          # local storage path
```