# Mindful Product Management Backend

A production-ready, multi-tenant Product Management REST API built with Spring Boot, featuring role-based access control, organization-scoped data isolation, and comprehensive monitoring.

## 🚀 Features

- **Multi-Tenant Architecture**: Organization-scoped data isolation
- **JWT Authentication**: Secure token-based authentication
- **Role-Based Access Control**: Hierarchical admin structure
- **Production Optimizations**: Caching, connection pooling, performance monitoring
- **Comprehensive Monitoring**: Prometheus metrics, structured logging
- **Rate Limiting**: API protection with configurable limits
- **Docker Support**: Production-ready containerization
- **API Documentation**: Interactive Swagger UI

## 📋 Table of Contents

- [Quick Start](#quick-start)
- [API Documentation](#api-documentation)
- [Configuration](#configuration)
- [Production Deployment](#production-deployment)
- [Monitoring & Metrics](#monitoring--metrics)
- [Performance Features](#performance-features)
- [Security Features](#security-features)
- [Development](#development)

## 🏁 Quick Start

### Prerequisites

- Java 17+
- Maven 3.6+
- MySQL 8.0+
- Docker & Docker Compose (optional)

### Local Development

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd mindful-product-backend
   ```

2. **Configure environment**
   ```bash
   cp .env.example .env
   # Edit .env with your database credentials
   ```

3. **Start MySQL database**
   ```bash
   # Using Docker
   docker run -d --name mysql-dev \
     -e MYSQL_ROOT_PASSWORD=rootpassword \
     -e MYSQL_DATABASE=productdb \
     -e MYSQL_USER=mindful \
     -e MYSQL_PASSWORD=mindful2025 \
     -p 3306:3306 mysql:8.0
   ```

4. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

5. **Access the application**
   - API Base URL: `http://localhost:8080/api`
   - Swagger UI: `http://localhost:8080/swagger-ui.html`
   - Health Check: `http://localhost:8080/actuator/health`

## 📚 API Documentation

### Swagger UI
- **Local**: `http://localhost:8080/swagger-ui.html`
- **Production**: `https://api.mindful.com/swagger-ui.html`

### OpenAPI Specification
- **JSON**: `http://localhost:8080/v3/api-docs`

### Authentication
All API endpoints (except auth) require JWT Bearer token:
```bash
# Get token
POST /api/auth/login
{
  "email": "user@example.com",
  "password": "password"
}

# Use token in requests
Authorization: Bearer <your-jwt-token>
```

### Key Endpoints
- `POST /api/auth/login` - User authentication
- `POST /api/auth/signup` - User registration
- `GET /api/products` - List products
- `POST /api/products` - Create product
- `GET /api/organizations` - List organizations
- `GET /api/actuator/health` - Health check

## ⚙️ Configuration

### Environment Variables

Create a `.env` file based on `.env.example`:

```env
# Database Configuration
DATABASE_URL=jdbc:mysql://localhost:3306/productdb
DATABASE_USERNAME=mindful
DATABASE_PASSWORD=mindful2025
DB_POOL_SIZE=30
DB_POOL_MIN_IDLE=10

# JWT Configuration
JWT_SECRET=your-super-secure-jwt-secret-key-here-min-256-bits
JWT_EXPIRATION=86400000

# Server Configuration
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=prod

# Cache Configuration
CACHE_MAX_SIZE=5000
CACHE_EXPIRE_AFTER_WRITE=30m
CACHE_EXPIRE_AFTER_ACCESS=15m

# Security Configuration
CORS_ALLOWED_ORIGINS=http://localhost:3000,https://yourdomain.com

# Rate Limiting
RATE_LIMIT_PER_MINUTE=100
RATE_LIMIT_PER_HOUR=1000
```

### Profiles

- **dev**: Development profile with debug logging
- **prod**: Production profile with optimizations
- **test**: Testing profile with minimal logging

## 🐳 Production Deployment

### Docker Compose (Recommended)

1. **Start production services**
   ```bash
   docker-compose -f docker-compose.prod.yml up -d
   ```

2. **Services included:**
   - Application server (Port 8080)
   - MySQL database (Port 3306)
   - Prometheus monitoring (Port 9090)

### Manual Docker Build

```bash
# Build image
docker build -t mindful-backend .

# Run container
docker run -d \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DATABASE_URL=jdbc:mysql://host:3306/productdb \
  --name mindful-backend \
  mindful-backend
```

### Production Checklist

- [ ] Set strong JWT secret (min 256 bits)
- [ ] Configure production database
- [ ] Set up SSL/TLS certificates
- [ ] Configure CORS origins
- [ ] Set up monitoring alerts
- [ ] Configure log aggregation
- [ ] Set resource limits
- [ ] Enable backup strategy

## 📊 Monitoring & Metrics

### Health Checks
- **Application**: `GET /actuator/health`
- **Database**: Included in health endpoint
- **Disk Space**: Included in health endpoint

### Prometheus Metrics
- **Endpoint**: `GET /actuator/prometheus`
- **Port**: 9090 (Prometheus UI)

### Custom Metrics
- `api_requests_total` - Total API requests
- `api_requests_duration` - Request duration
- `api_requests_errors` - Error count
- `cache_hits` - Cache hit count
- `cache_misses` - Cache miss count
- `rate_limit_exceeded` - Rate limit violations

### Logging
- **Console**: Structured JSON logs
- **File**: `/app/logs/application.log`
- **Security**: `/app/logs/security.log`
- **Rotation**: 100MB files, 30 days retention

## ⚡ Performance Features

### Caching Strategy
- **Implementation**: Caffeine Cache
- **Capacity**: 5000 entries
- **TTL**: 30min write / 15min access
- **Named Caches**: products, users, organizations, themes, initiatives, modules

### Database Optimizations
- **Connection Pool**: HikariCP (30 max, 10 min idle)
- **Batch Processing**: Enabled with size 25
- **Query Caching**: Hibernate second-level cache
- **Connection Validation**: Health checks enabled

### JVM Optimizations
- **Garbage Collector**: G1GC with 100ms pause target
- **Heap Size**: 512MB - 2GB (auto-scaling)
- **Memory Management**: Container-aware settings
- **Dump on OOM**: Heap dumps for debugging

### Threading
- **Async Processing**: 5-20 thread pool
- **Scheduling**: 5 thread pool for scheduled tasks
- **Tomcat**: 200 max threads, 20 min spare

## 🛡️ Security Features

### Authentication & Authorization
- **JWT Tokens**: Stateless authentication
- **Role-Based Access**: Multi-level permissions
- **Organization Scoping**: Data isolation

### Rate Limiting
- **Per IP**: 100 requests/minute
- **Headers**: Rate limit info in response
- **Monitoring**: Prometheus metrics

### Security Headers
- **HSTS**: 1 year max-age with subdomains
- **Frame Options**: SAMEORIGIN
- **Referrer Policy**: strict-origin-when-cross-origin

### CORS Configuration
- **Origins**: Environment configurable
- **Methods**: GET, POST, PUT, DELETE, OPTIONS, HEAD
- **Headers**: Authorization, Content-Type, etc.
- **Credentials**: Supported

### Input Validation
- **Bean Validation**: JSR-303 annotations
- **Global Exception Handling**: Consistent error responses
- **SQL Injection Protection**: JPA/Hibernate parameterized queries

## 👨‍💻 Development

### Build Commands

```bash
# Compile
mvn clean compile

# Run tests
mvn test

# Package
mvn clean package

# Run locally
mvn spring-boot:run

# Run with profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Code Quality
- **Checkstyle**: Code style enforcement
- **Security**: Input validation, SQL injection protection
- **Error Handling**: Comprehensive exception management
- **Logging**: Structured logging with correlation IDs

### Database Migration
- **Tool**: Hibernate DDL (validate mode in production)
- **Schema**: Auto-managed by JPA entities
- **Data**: Initialize via @PostConstruct methods

## 🤝 API Usage Examples

### Authentication
```bash
# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password"}'

# Response
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "email": "user@example.com"
}
```

### API Requests
```bash
# Get products (authenticated)
curl -X GET http://localhost:8080/api/products \
  -H "Authorization: Bearer <your-token>"

# Create product
curl -X POST http://localhost:8080/api/products \
  -H "Authorization: Bearer <your-token>" \
  -H "Content-Type: application/json" \
  -d '{"name":"New Product","description":"Description"}'
```

## 🔧 Troubleshooting

### Common Issues

1. **Connection Refused**
   - Check if MySQL is running
   - Verify database credentials in .env

2. **JWT Errors**
   - Ensure JWT_SECRET is properly set (min 256 bits)
   - Check token expiration

3. **Rate Limiting**
   - Check rate limit headers in response
   - Wait for reset time or adjust limits

4. **Memory Issues**
   - Monitor heap usage via actuator
   - Adjust JVM memory settings
   - Check for memory leaks in logs

### Logs Location
- **Docker**: `./logs/application.log`
- **Local**: `logs/application.log`
- **Container**: `/app/logs/`

### Health Checks
```bash
# Application health
curl http://localhost:8080/actuator/health

# Detailed health (requires admin role)
curl -H "Authorization: Bearer <admin-token>" \
  http://localhost:8080/actuator/health
```

## 📞 Support

- **Documentation**: Swagger UI for API details
- **Monitoring**: Prometheus + Grafana dashboards
- **Logs**: Centralized logging with correlation IDs
- **Health Checks**: Automated monitoring endpoints

## 📄 License

This project is licensed under the Apache 2.0 License - see the LICENSE file for details.

---

**Built with ❤️ using Spring Boot, optimized for production scale.**