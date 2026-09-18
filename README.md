# Buy 01 Marketplace

Buy 01 is a small e-commerce marketplace built with Spring Boot microservices and an Angular single-page application. Clients can browse products. Sellers can create, update, delete, and attach images to their own products.

## Architecture

```text
Angular frontend -> API Gateway :8080
                     | JWT validation, CORS, routing
                     +-> UserService :8081
                     +-> ProductService :8082
                     +-> MediaService :8083

ProductService -- product-events --> Kafka
MediaService   <-- product-events -- Kafka
```

Services use separate MongoDB databases:

- `userdb` for accounts and roles
- `productdb` for products and image references
- `mediadb` for media metadata; image files use the `media-uploads` Docker volume

## Requirements

- Docker and Docker Compose
- Node.js 20+ and npm for local Angular development
- Java 17 only when running a backend outside Docker

## Run With Docker

The repository expects a root `.env` file containing a shared JWT secret. It is ignored by Git.

```bash
printf 'JWT_SECRET=%s\n' "$(openssl rand -base64 48 | tr -d '\n')" > .env
docker compose up --build
```

Open the frontend through the Angular dev server:

```bash
cd frontend
npm install
npm start
```

The frontend runs at `http://localhost:4200` and proxies API calls to the Gateway at `http://localhost:8080`.

Public development endpoints:

- Gateway: `http://localhost:8080`
- Config Server: `http://localhost:8888`
- Eureka: `http://localhost:8761`
- Zipkin: `http://localhost:9411`
- MongoDB extension: `mongodb://localhost:27017/mediadb`

Internal application services and MongoDB are not published publicly by Compose. Containers communicate using Docker DNS names such as `mongodb`, `kafka`, and `media-service`.

## API Overview

Through the Gateway:

- `POST /api/auth/signup` and `POST /api/auth/login` are public.
- Authenticated users can read/update their profile with `GET` and `PUT /api/users/me`.
- Sellers can upload an avatar through the existing media upload endpoint without a product ID.
- `GET /api/products` and `GET /api/products/{id}` are public.
- Seller-only product writes use `POST`, `PUT`, and `DELETE` under `/api/products`.
- Seller-only media upload and deletion use `/api/media`.
- `GET /api/media/{id}` serves public product images.

The Gateway validates JWTs and forwards `userId` and `roles` headers to downstream services. Product and media services enforce ownership again in their service layers.

## Media and Kafka Behavior

Image upload is synchronous HTTP: MediaService validates magic bytes with Apache Tika, enforces a 2 MB limit, stores the file, and returns a media ID.

ProductService publishes `UPDATED` and `DELETED` events to Kafka topic `product-events`. MediaService consumes `UPDATED` events to remove detached image files and metadata, and consumes `DELETED` events to remove all media belonging to a deleted product asynchronously.

## Validation Commands

```bash
docker compose config
docker compose build

cd frontend
npm run build
npm test -- --watch=false
```

For local backend tests, install Java 17 and run `./gradlew test` from each service that includes a Gradle wrapper.

## Security Notes

- Never commit `.env` or production secrets.
- Use HTTPS and a managed secret store in production.
- Keep UserService, ProductService, MediaService, MongoDB, and Kafka on the private network.
- Replace the development CORS origin with the deployed frontend origin.