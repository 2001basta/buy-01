                   ┌──────────────────────────┐
                   │        Angular Frontend  │
                   │  - Sign Up / Login       │
                   │  - Product List          │
                   │  - Seller Dashboard      │
                   │  - Media Manager         │
                   └───────────┬──────────────┘
                               │ HTTP (REST / JWT)
                               ▼
┌──────────────────────────────────────────────────────────────┐
│                     API Gateway / Reverse Proxy              │
│  - Routes requests to appropriate microservices              │
│  - Handles authentication & authorization                    │
│  - Rate limiting, logging (optional)                         │
└───────────────┬───────────────────┬──────────────────────────┘
                │                   │
                │                   │
                ▼                   ▼
       ┌────────────────┐   ┌───────────────────┐
       │  User Service  │   │ Product Service   │
       │----------------│   │-------------------│
       │ - Register user│   │ - CRUD Products   │
       │ - Login        │   │ - Only seller     │
       │ - JWT auth     │   │   can modify      │
       │ - Profile      │   │ - Store media IDs │
       │ - Avatar upload│   │ - Publish events  │
       │ - MongoDB      │   │ - MongoDB         │
       └─────┬──────────┘   └─────┬─────────────┘
             │                    │
             │ Kafka Events       │ Kafka Events
             ▼                    ▼
       ┌────────────────────────────────┐
       │       Media Service            │
       │--------------------------------│
       │ - Upload product images        │
       │ - Validate file type & size    │
       │ - Store images (local / cloud) │
       │ - Return URL for Product       │
       │ - MongoDB / File Storage       │
       └────────────────────────────────┘
                   ┌───────────────────────────┐
                   │        Angular Frontend   │
                   │  - Sign Up / Login        │
                   │  - Product List           │
                   │  - Seller Dashboard       │
                   │  - Media Manager          │
                   └───────────┬───────────────┘
                               │ HTTP (REST / JWT)
                               ▼
┌──────────────────────────────────────────────────────────────┐
│                     API Gateway / Reverse Proxy              │
│  - Routes requests to appropriate microservices              │
│  - Handles authentication & authorization                    │
│  - Rate limiting, logging (optional)                         │
└───────────────┬───────────────────┬──────────────────────────┘
                │                   │
                │                   │
                ▼                   ▼
       ┌────────────────┐    ┌───────────────── ┐
       │  User Service  │    │ Product Service  │
       │----------------│    │----------------- │
       │ - Register user│    │ - CRUD Products  │
       │ - Login        │    │ - Only seller    │
       │ - JWT auth     │    │   can modify     │
       │ - Profile      │    │ - Store media IDs│
       │ - Avatar upload│    │ - Publish events │
       │ - MongoDB      │    │ - MongoDB        │
       └─────┬──────────┘    └─────┬────────────┘
             │                     │
             │ Kafka Events        │ Kafka Events
             ▼                     ▼
       ┌──────────────────────────────┐
       │       Media Service          │
       │------------------------------│
       │ - Upload product images      │
       │ - Validate file type & size  │
       │ - Store images (local / cloud)│
       │ - Return URL for Product     │
       │ - MongoDB / File Storage     │
       └──────────────────────────────┘


the architecture will implement -----------------------------------------------------------------------


                   ┌───────────────────────────┐
                   │        Angular Frontend   │
                   │  - Sign Up / Login        │
                   │  - Product List           │
                   │  - Seller Dashboard       │
                   │  - Media Manager          │
                   └───────────┬───────────────┘
                               │ HTTP (REST)
                               ▼
                   ┌───────────────────────────┐
                   │   Spring Cloud Gateway    │
                   │---------------------------│
                   │ - Route requests to       │
                   │   microservices           │
                   │ - OAuth2 token validation │
                   │ - Role-based routing      │
                   └───────┬──────────┬────────┘
                           │          │
                           │          │
                           ▼          ▼
                ┌─────────────────┐   ┌─────────────────┐
                │  User Service   │   │ Product Service │
                │-----------------│   │-----------------│
                │ - Register user │   │ - CRUD Products │
                │ - Login         │   │ - Only seller   │
                │ - OAuth2        │   │   can modify    │
                │   Authorization │   │ - Store media IDs │
                │ - Profile       │   │ - Publish events │
                │ - Avatar upload │   │ - MongoDB      │
                │ - MongoDB       │   └─────┬──────────┘
                └─────┬───────────┘         │
                      │ Kafka Events       │ Kafka Events
                      ▼                    ▼
                ┌──────────────────────────────┐
                │       Media Service          │
                │------------------------------│
                │ - Upload product images      │
                │ - Validate file type & size  │
                │ - Store images (local/cloud) │
                │ - Return URL for Product     │
                │ - MongoDB / File Storage     │
                └──────────────────────────────┘




we will use this architecture -------------------------
All services (User, Product, Media) communicate through API Gateway.

User Service handles authentication, including social login, and issues JWTs.

Frontend calls API Gateway only, never directly the services.

Here’s how you can structure it.

Architecture Diagram
Angular Frontend
       │
       ▼
API Gateway (Validation + Role Enforcement)
       │
       ├───────────────┐───────────────┐
       ▼               ▼               ▼
User Service       Product Service   Media Service
- Login / Signup   - CRUD Products   - File Upload
- Social Login     - Role Checks     - Role Checks
- Issue JWT        - MongoDB         - File Storage
- Refresh Tokens
Flow Explanation
1️⃣ Normal Login / Social Login

User clicks login or login with Google/Facebook

Frontend sends login request to API Gateway

API Gateway forwards request to User Service

User Service:

Validates username/password OR

Handles social login (exchanges authorization code for user profile)

Creates or links local user

Issues JWT access token + refresh token

2️⃣ API Gateway Token Validation

Frontend sends requests with:

Authorization: Bearer <JWT>

API Gateway:

Validates JWT signature

Checks exp (expiry)

Checks roles claim

Rejects unauthorized requests

If valid → forwards request to appropriate service (User / Product / Media)

3️⃣ Microservices

Each service trusts API Gateway for JWT validation (defense in depth: optional JWT verification inside services)

Use claims for business logic:

Product Service: only SELLER can create products

Media Service: only SELLER can upload images

User Service: only ADMIN can see other users

4️⃣ Refresh Token

Access token expires → frontend calls User Service via API Gateway

User Service validates refresh token → issues new access token

5️⃣ Social Login Details

User Service handles OAuth2 with providers (Google, Facebook):

Receives authorization code from frontend

Exchanges code with provider → gets user profile

Creates / links local user

Issues JWT token for your system

API Gateway does not care which login method was used — it validates JWT the same way.

project-root/
│
├── angular-frontend/
│   ├── src/
│   │   ├── app/
│   │   │   ├── auth/
│   │   │   │   ├── login/
│   │   │   │   ├── signup/
│   │   │   │   ├── social-login/
│   │   │   │   ├── auth.service.ts
│   │   │   │   └── auth.guard.ts
│   │   │   │
│   │   │   ├── product/
│   │   │   │   ├── product-list/
│   │   │   │   ├── product-create/
│   │   │   │   └── product.service.ts
│   │   │   │
│   │   │   ├── media/
│   │   │   │   ├── media-upload/
│   │   │   │   └── media.service.ts
│   │   │   │
│   │   │   ├── shared/
│   │   │   │   ├── components/
│   │   │   │   ├── directives/
│   │   │   │   ├── models/
│   │   │   │   └── services/
│   │   │   │
│   │   │   ├── app-routing.module.ts
│   │   │   └── app.module.ts
│   │   └── assets/
│   │       └── images/
│   └── package.json
│
├── api-gateway/
│   ├── src/main/java/com/example/apigateway/
│   │   ├── config/
│   │   │   ├── SecurityConfig.java        # JWT validation, role-based routing
│   │   │   └── RouteConfig.java           # Spring Cloud Gateway routes
│   │   ├── filters/
│   │   │   └── JwtAuthFilter.java
│   │   └── ApiGatewayApplication.java
│   └── pom.xml
│
├── user-service/
│   ├── src/main/java/com/example/userservice/
│   │   ├── config/
│   │   │   ├── SecurityConfig.java        # JWT signing, CORS
│   │   │   └── OAuth2Config.java          # Google/Facebook OAuth
│   │   ├── controller/
│   │   │   ├── AuthController.java        # login, signup, refresh token
│   │   │   └── UserController.java       # profile, user management
│   │   ├── service/
│   │   │   ├── AuthService.java
│   │   │   ├── JwtService.java
│   │   │   └── SocialLoginService.java
│   │   ├── repository/
│   │   │   └── UserRepository.java
│   │   ├── model/
│   │   │   ├── User.java
│   │   │   └── Role.java
│   │   └── UserServiceApplication.java
│   └── pom.xml
│
├── product-service/
│   ├── src/main/java/com/example/productservice/
│   │   ├── controller/
│   │   │   └── ProductController.java
│   │   ├── service/
│   │   │   └── ProductService.java
│   │   ├── repository/
│   │   │   └── ProductRepository.java
│   │   ├── model/
│   │   │   └── Product.java
│   │   └── ProductServiceApplication.java
│   └── pom.xml
│
├── media-service/
│   ├── src/main/java/com/example/mediaservice/
│   │   ├── controller/
│   │   │   └── MediaController.java
│   │   ├── service/
│   │   │   └── MediaService.java
│   │   ├── repository/
│   │   │   └── MediaRepository.java      # if storing metadata
│   │   ├── model/
│   │   │   └── MediaFile.java
│   │   └── MediaServiceApplication.java
│   └── pom.xml
│
├── docker-compose.yml
├── README.md
└── scripts/
    ├── start-dev.sh
    └── stop-dev.sh