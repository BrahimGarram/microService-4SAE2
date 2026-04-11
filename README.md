# Freelance Platform - User Microservice

A complete Freelance Platform implementation with Spring Boot microservice architecture, Keycloak authentication, and Angular frontend.

## 🏗 Architecture

```
Angular Frontend (Port 4200)
    ↓
Keycloak (Port 8080) - Authentication
    ↓
Spring Boot User Service (Port 8081)
    ↓
MySQL Database (Port 3306) - XAMPP
```

## 📋 Tech Stack

- **Frontend**: Angular 17
- **Backend**: Spring Boot 3.2.0
- **Authentication**: Keycloak (OAuth2/OIDC)
- **Database**: MySQL (XAMPP)
- **Security**: JWT (OAuth2 Resource Server)
- **Communication**: REST API (JSON)

## 📁 Project Structure

```
projet/
├── backend/                    # Spring Boot User Microservice
│   ├── src/
│   │   └── main/
│   │       ├── java/com/freelance/userservice/
│   │       │   ├── config/     # Security configuration
│   │       │   ├── controller/ # REST controllers
│   │       │   ├── dto/        # Data Transfer Objects
│   │       │   ├── exception/  # Exception handlers
│   │       │   ├── model/      # JPA entities
│   │       │   ├── repository/ # JPA repositories
│   │       │   ├── service/    # Business logic
│   │       │   └── util/       # Utility classes
│   │       └── resources/
│   │           └── application.yml
│   └── pom.xml
├── frontend/                   # Angular Application
│   ├── src/
│   │   ├── app/
│   │   │   ├── components/     # Angular components
│   │   │   ├── guards/         # Route guards
│   │   │   ├── interceptors/   # HTTP interceptors
│   │   │   └── services/       # Angular services
│   │   ├── assets/
│   │   └── styles.css
│   ├── angular.json
│   └── package.json
└── docs/
    └── KEYCLOAK_SETUP.md      # Keycloak configuration guide
```

## 🚀 Setup Instructions

### Prerequisites

1. **Java 17+** installed
2. **Maven 3.6+** installed
3. **Node.js 18+** and **npm** installed
4. **XAMPP** installed and running (MySQL on port 3306)
5. **Keycloak** server running (default port 8080)

### Step 1: Database Setup

1. Start **XAMPP** and ensure MySQL is running
2. MySQL should be accessible at:
   - **Host**: `localhost`
   - **Port**: `3306`
   - **Username**: `root`
   - **Password**: `` (empty)

3. The database `freelance_db` will be created automatically on first run (via `createDatabaseIfNotExist=true`)

### Step 2: Keycloak Setup

1. Download and start Keycloak server
2. Access Admin Console: http://localhost:8080/admin
3. Follow the detailed guide in `docs/KEYCLOAK_SETUP.md` to:
   - Create realm: `projetpidev`
   - Create client: `freelance-client`
   - Create roles: `USER`, `FREELANCER`, `ADMIN`
   - Create test users

### Step 3: Backend Setup

1. Navigate to backend directory:
   ```bash
   cd backend
   ```

2. Build the project:
   ```bash
   mvn clean install
   ```

3. Run the application:
   ```bash
   mvn spring-boot:run
   ```

   Or run the JAR:
   ```bash
   java -jar target/user-service-1.0.0.jar
   ```

4. The backend will start on **http://localhost:8081**

5. Verify it's running:
   ```bash
   curl http://localhost:8081/actuator/health
   ```

### Step 4: Frontend Setup

1. Navigate to frontend directory:
   ```bash
   cd frontend
   ```

2. Install dependencies:
   ```bash
   npm install
   ```

3. Start the development server:
   ```bash
   npm start
   # or
   ng serve
   ```

4. The frontend will start on **http://localhost:4200**

### Step 5: Verify Setup

1. Open browser: http://localhost:4200
2. Click **"Login"**
3. Login with Keycloak credentials (e.g., `user1` / `password123`)
4. After login, you should see your profile information

## 🔐 API Endpoints

### User Endpoints

| Method | Endpoint | Description | Auth Required | Role Required |
|--------|----------|-------------|---------------|---------------|
| GET | `/api/users/me` | Get current user | ✅ | - |
| GET | `/api/users` | Get all users | ✅ | ADMIN |
| POST | `/api/users/sync` | Sync user from Keycloak | ✅ | - |
| PUT | `/api/users/{id}` | Update user profile | ✅ | - |
| DELETE | `/api/users/{id}` | Delete user | ✅ | ADMIN |

### Example Requests

#### Get Current User
```bash
curl -X GET http://localhost:8081/api/users/me \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### Sync User
```bash
curl -X POST http://localhost:8081/api/users/sync \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### Update User
```bash
curl -X PUT http://localhost:8081/api/users/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"username": "newusername", "email": "newemail@example.com"}'
```

## 🧪 Testing

### Test Users

After Keycloak setup, you can use these test users:

| Username | Password | Role |
|----------|----------|------|
| `user1` | `password123` | USER |
| `freelancer1` | `password123` | FREELANCER |
| `admin1` | `password123` | ADMIN |

### Manual Testing Flow

1. **Login**: Use Keycloak login page
2. **First Login**: User is automatically synced to database
3. **View Profile**: See user information on home page
4. **Edit Profile**: Update username/email via profile page
5. **Logout**: Clear session and redirect to home

## 🔧 Configuration

### Backend Configuration

Edit `backend/src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/freelance_db
    username: root
    password: ""  # Change if you have MySQL password
  
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://localhost:8080/realms/projetpidev
```

### Frontend Configuration

Edit `frontend/src/app/app.module.ts`:

```typescript
config: {
  url: 'http://localhost:8080',  // Keycloak URL
  realm: 'projetpidev',
  clientId: 'freelance-client'
}
```

## 🐛 Troubleshooting

### Backend Issues

**Issue**: Cannot connect to MySQL
- **Solution**: Ensure XAMPP MySQL is running and credentials are correct

**Issue**: JWT validation fails
- **Solution**: Check `issuer-uri` in `application.yml` matches Keycloak realm URL

**Issue**: CORS errors
- **Solution**: Verify CORS configuration in `SecurityConfig.java` includes Angular URL

### Frontend Issues

**Issue**: Keycloak login redirect fails
- **Solution**: Check client redirect URIs in Keycloak admin console

**Issue**: JWT token not sent in requests
- **Solution**: Verify `AuthInterceptor` is properly configured in `app.module.ts`

**Issue**: User profile not loading
- **Solution**: Ensure user is synced by calling `/api/users/sync` endpoint

### Keycloak Issues

**Issue**: Roles not in JWT token
- **Solution**: Verify realm roles mapper is configured and roles are assigned

**Issue**: Invalid redirect URI
- **Solution**: Add exact redirect URI to client settings in Keycloak

## 📝 Development Notes

### Database Schema

The `users` table is automatically created with:
- `id` (BIGINT, Primary Key, Auto-increment)
- `keycloak_id` (VARCHAR, Unique, Not Null)
- `username` (VARCHAR, Not Null)
- `email` (VARCHAR, Unique, Not Null)
- `role` (ENUM: USER, FREELANCER, ADMIN)
- `created_at` (TIMESTAMP)
- `updated_at` (TIMESTAMP)

### Security Flow

1. User logs in via Keycloak
2. Keycloak issues JWT token
3. Angular stores token and attaches to requests
4. Spring Boot validates JWT with Keycloak
5. Spring Security extracts roles and authorizes requests

### Key Files

- **SecurityConfig.java**: OAuth2 Resource Server configuration
- **JwtUtil.java**: JWT token parsing utilities
- **UserController.java**: REST API endpoints
- **AuthInterceptor**: Angular HTTP interceptor for JWT
- **AuthGuard**: Angular route guard for protected routes

## 🚀 Next Steps

1. Add more microservices (Project Service, Payment Service, etc.)
2. Implement API Gateway (Spring Cloud Gateway)
3. Add service discovery (Eureka/Consul)
4. Implement message queue (RabbitMQ/Kafka)
5. Add monitoring (Prometheus, Grafana)
6. Containerize with Docker
7. Deploy to cloud (AWS, Azure, GCP)

## 📄 License

This project is created for educational purposes as a university project.

## 👥 Contributors

- Your Name - Initial work

---

**Note**: This is a development setup. For production, ensure:
- HTTPS is enabled
- Strong passwords are used
- Database credentials are secured
- Keycloak is properly configured
- CORS is restricted to specific origins
- Token lifespans are appropriate
