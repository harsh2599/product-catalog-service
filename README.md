# Product Catalog Service

A Spring Boot REST API for managing a product catalog with reviews, built as a learning project covering JPA relationships, Flyway migrations, transaction handling, unit/integration testing, and Docker.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.4.5 |
| Persistence | Spring Data JPA + H2 (in-memory) |
| Migrations | Flyway |
| Validation | Jakarta Bean Validation |
| Boilerplate reduction | Lombok |
| Testing | JUnit 5 + Mockito + MockMvc |
| Containerisation | Docker (multi-stage build) |

---

## Project Structure

```
src/
├── main/
│   ├── java/com/example/
│   │   ├── Main.java                          # Spring Boot entry point
│   │   ├── controller/
│   │   │   └── ProductController.java         # REST endpoints
│   │   ├── service/
│   │   │   └── ProductService.java            # Business logic + transactions
│   │   ├── repository/
│   │   │   ├── ProductRepository.java         # JPA repository for Product
│   │   │   └── ReviewRepository.java          # JPA repository for Review
│   │   ├── model/
│   │   │   ├── Product.java                   # @Entity — One-to-Many with Review
│   │   │   └── Review.java                    # @Entity — Many-to-One with Product
│   │   ├── dto/
│   │   │   ├── ProductDTO.java                # Request body for creating a product
│   │   │   ├── ReviewDTO.java                 # Request body for creating a review
│   │   │   └── ProductWithReviewsDTO.java     # Request body for atomic product+reviews creation
│   │   └── exception/
│   │       ├── GlobalExceptionHandler.java    # @RestControllerAdvice — maps exceptions to HTTP status
│   │       ├── ProductNotFoundException.java  # 404 when product ID is not found
│   │       ├── EmptyReviewsException.java     # 400 when reviews list is empty
│   │       └── ErrorResponse.java             # Standard error response shape
│   └── resources/
│       ├── application.properties             # App config (datasource, Flyway, actuator)
│       └── db/migration/
│           ├── V1__init.sql                   # Creates product table + seeds 5 rows
│           └── V2__reviews.sql                # Creates review table with FK to product
└── test/
    ├── java/com/example/
    │   ├── service/
    │   │   └── ProductServiceTest.java        # Unit tests (Mockito — no Spring context)
    │   └── controller/
    │       └── ProductIntegrationTest.java    # Integration tests (MockMvc + full context)
    └── resources/
        └── application.properties             # Test-specific datasource (testdb)
```

---

## Running Locally

**Prerequisites:** Java 21, Maven 3.9+

```bash
# Run the application
mvn spring-boot:run

# The API is available at http://localhost:8080
```

---

## Maven Commands

```bash
# Compile the project
mvn compile

# Run all tests (unit + integration)
mvn test

# Run only unit tests
mvn test -Dtest=ProductServiceTest

# Run only integration tests
mvn test -Dtest=ProductIntegrationTest

# Build a runnable JAR (skips tests)
mvn clean package -DskipTests

# Build a runnable JAR (includes tests)
mvn clean package

# Clean build output
mvn clean
```

---

## Docker Commands

```bash
# Build the Docker image (compiles inside Docker — no local Maven needed)
docker build -t product-service .

# Run the container on port 8080
docker run -p 8080:8080 product-service

# Run in detached (background) mode
docker run -d -p 8080:8080 --name product-service product-service

# View logs of the running container
docker logs product-service

# Stop the container
docker stop product-service

# Remove the container
docker rm product-service

# Stop and remove in one step
docker stop product-service && docker rm product-service

# List built images
docker images

# Remove the image
docker rmi product-service
```

---

## API Endpoints

Base URL: `http://localhost:8080`

### Products

| Method | Path | Description | Status |
|---|---|---|---|
| GET | `/products` | List all products (paginated) | 200 |
| GET | `/products/{id}` | Get a single product by ID | 200 / 404 |
| POST | `/products` | Create a new product | 201 / 400 |
| POST | `/products/{id}/reviews` | Add a review to a product | 201 / 400 / 404 |
| POST | `/products/withReviews` | Create a product with reviews atomically | 201 / 400 |

---

### GET `/products`

Returns a paginated list of all products. Defaults to page 0, size 10.

**Query params:**

| Param | Default | Description |
|---|---|---|
| `page` | `0` | Zero-based page number |
| `size` | `10` | Number of items per page |

```bash
curl http://localhost:8080/products
curl "http://localhost:8080/products?page=0&size=2"
```

**Response:**
```json
{
  "content": [
    { "id": 1, "name": "Laptop", "price": 1200.0, "reviews": [] }
  ],
  "totalElements": 5,
  "totalPages": 1,
  "size": 10,
  "number": 0
}
```

---

### GET `/products/{id}`

```bash
curl http://localhost:8080/products/1
```

**Response (200):**
```json
{ "id": 1, "name": "Laptop", "price": 1200.0, "reviews": [] }
```

**Response (404):**
```json
{ "status": 404, "message": "Product not found with id: 99", "timestamp": "..." }
```

---

### POST `/products`

Creates a new product.

```bash
curl -X POST http://localhost:8080/products \
  -H "Content-Type: application/json" \
  -d '{"name": "Webcam", "price": 79.99}'
```

**Request body:**
```json
{ "name": "Webcam", "price": 79.99 }
```

**Response (201):**
```json
{ "id": 6, "name": "Webcam", "price": 79.99, "reviews": [] }
```

**Response (400) — validation failure:**
```json
{
  "status": 400,
  "message": "Validation failed",
  "errors": ["name: must not be blank", "price: must be greater than 0"]
}
```

---

### POST `/products/{id}/reviews`

Adds a review to an existing product.

```bash
curl -X POST http://localhost:8080/products/1/reviews \
  -H "Content-Type: application/json" \
  -d '{"comment": "Great build quality"}'
```

**Request body:**
```json
{ "comment": "Great build quality" }
```

**Response (201):**
```json
{ "id": 1, "comment": "Great build quality" }
```

---

### POST `/products/withReviews`

Creates a product and its reviews in a single `@Transactional` operation. If `reviews` is empty or missing, the entire operation is rolled back — no product is saved.

```bash
curl -X POST http://localhost:8080/products/withReviews \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Headphones",
    "price": 149.99,
    "reviews": [
      {"comment": "Great sound"},
      {"comment": "Comfortable fit"}
    ]
  }'
```

**Response (201):**
```json
{
  "id": 7,
  "name": "Headphones",
  "price": 149.99,
  "reviews": [
    { "id": 1, "comment": "Great sound" },
    { "id": 2, "comment": "Comfortable fit" }
  ]
}
```

**Response (400) — empty reviews triggers rollback:**
```json
{ "status": 400, "message": "At least one review is required to create a product" }
```

---

## Actuator Endpoints

| Path | Description |
|---|---|
| `/actuator/health` | Application health (UP/DOWN), DB status, disk space |
| `/actuator/info` | Build info (if configured) |

```bash
curl http://localhost:8080/actuator/health
```

---

## H2 Console

The in-memory database can be browsed at **http://localhost:8080/h2-console**

| Field | Value |
|---|---|
| JDBC URL | `jdbc:h2:mem:productdb` |
| Username | `sa` |
| Password | *(leave blank)* |

---

## Database Migrations

Flyway manages the schema. Migration files live in `src/main/resources/db/migration/`.

| File | Description |
|---|---|
| `V1__init.sql` | Creates `product` table and seeds 5 rows (Laptop, Mouse, Keyboard, Monitor, USB Cable) |
| `V2__reviews.sql` | Creates `review` table with a foreign key to `product` |
