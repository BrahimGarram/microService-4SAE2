# API Reference

## Base URL
```
http://localhost:8081/api/users
```

**Note:** Backend runs on port 8081 to avoid conflict with Keycloak (port 8080).

## Authentication

All endpoints (except `/sync`) require JWT authentication. Include the token in the Authorization header:

```
Authorization: Bearer <your-jwt-token>
```

## Endpoints

### 1. Get Current User

Get the profile of the currently authenticated user.

**Endpoint:** `GET /api/users/me`

**Authentication:** Required

**Response:**
```json
{
  "id": 1,
  "keycloakId": "123e4567-e89b-12d3-a456-426614174000",
  "username": "user1",
  "email": "user1@example.com",
  "role": "USER",
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

**Status Codes:**
- `200 OK` - Success
- `401 Unauthorized` - Missing or invalid token
- `404 Not Found` - User not found in database

---

### 2. Get All Users

Get a list of all users in the system.

**Endpoint:** `GET /api/users`

**Authentication:** Required

**Authorization:** ADMIN role required

**Response:**
```json
[
  {
    "id": 1,
    "keycloakId": "123e4567-e89b-12d3-a456-426614174000",
    "username": "user1",
    "email": "user1@example.com",
    "role": "USER",
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00"
  },
  {
    "id": 2,
    "keycloakId": "223e4567-e89b-12d3-a456-426614174001",
    "username": "admin1",
    "email": "admin1@example.com",
    "role": "ADMIN",
    "createdAt": "2024-01-15T11:00:00",
    "updatedAt": "2024-01-15T11:00:00"
  }
]
```

**Status Codes:**
- `200 OK` - Success
- `401 Unauthorized` - Missing or invalid token
- `403 Forbidden` - User doesn't have ADMIN role

---

### 3. Sync User

Sync user from Keycloak to database. This endpoint extracts user information from the JWT token and creates/updates the user profile in the database.

**Endpoint:** `POST /api/users/sync`

**Authentication:** Required (JWT token)

**Request Body:** None (user info extracted from JWT)

**Response:**
```json
{
  "id": 1,
  "keycloakId": "123e4567-e89b-12d3-a456-426614174000",
  "username": "user1",
  "email": "user1@example.com",
  "role": "USER",
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

**Status Codes:**
- `201 Created` - User created successfully
- `200 OK` - User updated successfully
- `401 Unauthorized` - Missing or invalid token

**Notes:**
- This endpoint is typically called after first login
- User role is extracted from JWT token (realm roles)
- If user already exists, profile is updated

---

### 4. Update User Profile

Update user profile information (username, email).

**Endpoint:** `PUT /api/users/{id}`

**Authentication:** Required

**Authorization:** Users can update their own profile, or ADMIN can update any profile

**Path Parameters:**
- `id` (Long) - User ID

**Request Body:**
```json
{
  "username": "newusername",
  "email": "newemail@example.com"
}
```

**Response:**
```json
{
  "id": 1,
  "keycloakId": "123e4567-e89b-12d3-a456-426614174000",
  "username": "newusername",
  "email": "newemail@example.com",
  "role": "USER",
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T12:00:00"
}
```

**Status Codes:**
- `200 OK` - Success
- `400 Bad Request` - Invalid request data
- `401 Unauthorized` - Missing or invalid token
- `403 Forbidden` - User trying to update another user's profile (not ADMIN)
- `404 Not Found` - User not found

**Notes:**
- Both fields are optional
- Only provided fields will be updated
- Role cannot be changed via this endpoint

---

### 5. Delete User

Delete a user from the system.

**Endpoint:** `DELETE /api/users/{id}`

**Authentication:** Required

**Authorization:** ADMIN role required

**Path Parameters:**
- `id` (Long) - User ID to delete

**Response:** No content

**Status Codes:**
- `204 No Content` - Success
- `401 Unauthorized` - Missing or invalid token
- `403 Forbidden` - User doesn't have ADMIN role
- `404 Not Found` - User not found

---

## Error Responses

All error responses follow this format:

```json
{
  "error": "Error message description"
}
```

For validation errors:

```json
{
  "username": "Username is required",
  "email": "Email must be valid"
}
```

## User Roles

The system supports three roles:

- **USER**: Regular user
- **FREELANCER**: Freelancer user
- **ADMIN**: Administrator

Roles are managed in Keycloak and extracted from JWT tokens.

## Example cURL Commands

### Get Current User
```bash
curl -X GET http://localhost:8081/api/users/me \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Sync User
```bash
curl -X POST http://localhost:8081/api/users/sync \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Update User
```bash
curl -X PUT http://localhost:8081/api/users/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "newusername",
    "email": "newemail@example.com"
  }'
```

### Get All Users (Admin)
```bash
curl -X GET http://localhost:8081/api/users \
  -H "Authorization: Bearer ADMIN_JWT_TOKEN"
```

### Delete User (Admin)
```bash
curl -X DELETE http://localhost:8081/api/users/1 \
  -H "Authorization: Bearer ADMIN_JWT_TOKEN"
```
