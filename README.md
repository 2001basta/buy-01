# Buy 01 Marketplace

Buy 01 is an e-commerce marketplace built with Spring Boot microservices and an Angular single-page application. Clients can browse products, while sellers can create, update, delete, and attach images to their own products.

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

## Install Angular Without `sudo`

Use `nvm` to install Node.js and keep global npm packages in your user account. This avoids changing system directories with `sudo`.

```bash
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.40.3/install.sh | bash
source ~/.bashrc  # use source ~/.zshrc when your shell is zsh
nvm install 20
nvm use 20
```

The project already declares the Angular CLI locally. Install dependencies from the frontend directory:

```bash
cd frontend
npm install
npx ng version
```

You can run all Angular commands with `npx ng ...` or the project scripts, so a global Angular installation is optional. If you also want the `ng` command globally, install it after `nvm` is active:

```bash
npm install --global @angular/cli
ng version
```

Do not use `sudo npm install -g @angular/cli`.

## Run With Docker

The repository expects a root `.env` file containing a shared JWT secret. It is ignored by Git.

```bash
printf 'JWT_SECRET=%s\n' "$(openssl rand -base64 48 | tr -d '\n')" > .env
docker compose up --build
```

Open the frontend through the Angular HTTPS development server:

```bash
cd frontend
npm install
npm start
```

The frontend runs at `https://localhost:4200` and proxies API calls to the HTTPS Gateway at `https://localhost:8443`. Angular CLI creates a development certificate automatically. Your browser will show a certificate warning on the first visit; accept it for local development only.

The proxy accepts the Gateway's local self-signed certificate. This is configured in `frontend/proxy.conf.json` and must not be copied as a production TLS configuration.

## Create a New Local HTTPS Certificate

The Angular development server creates its own temporary certificate automatically. The API Gateway uses a separate Java PKCS12 keystore at `certs/gateway-keystore.p12`. That keystore is ignored by Git because it contains a private key, so each developer can create it locally.

Make sure Java 17 and `keytool` are installed, then run this from the repository root:

```bash
rm -f certs/gateway-keystore.p12
keytool -genkeypair \
    -alias gateway \
    -keyalg RSA \
    -keysize 2048 \
    -validity 365 \
    -storetype PKCS12 \
    -keystore certs/gateway-keystore.p12 \
    -storepass changeit \
    -keypass changeit \
    -dname "CN=localhost, OU=Buy01, O=Buy01, L=Local, ST=Local, C=US" \
    -ext "SAN=dns:localhost,ip:127.0.0.1"
```

The alias and password must match the Gateway configuration. The default local password is `changeit`; for a different password, use the same value when generating the keystore and add it to the root `.env` file:

```dotenv
SERVER_SSL_KEY_STORE_PASSWORD=your-local-password
```

Docker mounts this file automatically through `docker-compose.yml`. You can optionally export the public certificate for inspection or browser trust configuration:

```bash
keytool -exportcert \
    -alias gateway \
    -keystore certs/gateway-keystore.p12 \
    -storepass changeit \
    -rfc \
    -file certs/gateway.crt
```

Do not commit the `.p12` keystore or private key. This self-signed certificate is for local development only; use a certificate issued by a trusted authority in production.

Public development endpoints:

- Gateway: `https://localhost:8443`
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

To verify the HTTPS development server directly:

```bash
cd frontend
npm start
curl -kI https://localhost:4200
```

For local backend tests, install Java 17 and run `./gradlew test` from each service that includes a Gradle wrapper.

## Security Notes

- Never commit `.env` or production secrets.
- Use a trusted certificate, HTTPS, and a managed secret store in production.
- Keep UserService, ProductService, MediaService, MongoDB, and Kafka on the private network.
- Replace the development CORS origin with the deployed frontend origin.
