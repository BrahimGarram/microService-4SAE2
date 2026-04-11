# Sync Keycloak User to MySQL Database

When you create a user in Keycloak, it's NOT automatically in your MySQL database. You need to sync it.

## Why?

- **Keycloak** = Authentication (handles login, passwords, tokens)
- **MySQL** = Your application database (stores user profiles, data)

Users need to exist in BOTH places.

## How to Sync

### Method 1: Login (Automatic) - RECOMMENDED

The easiest way is to just log in:

1. Go to your app: `http://localhost:4200`
2. Click **Login**
3. Enter your admin credentials:
   - Username: `admin`
   - Password: `password123`
4. After successful login, the frontend automatically calls `/api/users/sync`
5. Your user is now in the MySQL database!

**This is the normal flow** - users are synced on first login.

### Method 2: Manual Sync via API

If you want to sync without logging in:

#### Step 1: Get JWT Token

Using **Postman** or **curl**:

```bash
POST http://localhost:8080/realms/projetpidev/protocol/openid-connect/token

Headers:
Content-Type: application/x-www-form-urlencoded

Body (x-www-form-urlencoded):
client_id: freelance-client
username: admin
password: password123
grant_type: password
```

**Response** will contain:
```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refresh_token": "...",
  ...
}
```

Copy the `access_token`.

#### Step 2: Call Sync Endpoint

```bash
POST http://localhost:8081/api/users/sync

Headers:
Authorization: Bearer YOUR_ACCESS_TOKEN_HERE
```

**Response** will be the user data:
```json
{
  "id": 1,
  "keycloakId": "123e4567-e89b-12d3-a456-426614174000",
  "username": "admin",
  "email": "admin@example.com",
  "role": "ADMIN",
  ...
}
```

### Method 3: Using Postman

1. **Get Token**:
   - Method: `POST`
   - URL: `http://localhost:8080/realms/projetpidev/protocol/openid-connect/token`
   - Body → x-www-form-urlencoded:
     - `client_id`: `freelance-client`
     - `username`: `admin`
     - `password`: `password123`
     - `grant_type`: `password`
   - Click **Send**
   - Copy the `access_token` from response

2. **Sync User**:
   - Method: `POST`
   - URL: `http://localhost:8081/api/users/sync`
   - Headers:
     - `Authorization`: `Bearer YOUR_ACCESS_TOKEN`
   - Click **Send**
   - User should be created in database

## Verify User in Database

1. Open **phpMyAdmin** (XAMPP) or MySQL client
2. Select database: `freelance_db`
3. Go to table: `users`
4. You should see your user:
   - `username`: `admin`
   - `email`: `admin@example.com`
   - `role`: `ADMIN`
   - `keycloak_id`: (Keycloak user ID)

## Troubleshooting

**Issue**: Sync endpoint returns 401 Unauthorized
- **Solution**: Make sure you're using a valid JWT token
- Get a fresh token if it expired

**Issue**: User not created in database
- **Solution**: Check backend logs for errors
- Verify backend is running on port 8081
- Check MySQL connection in `application.yml`

**Issue**: User created but role is wrong
- **Solution**: The role comes from Keycloak JWT token
- Verify the user has the correct role assigned in Keycloak
- Check Role mapping tab in Keycloak

## Notes

- Users are synced **on first login** automatically
- The sync endpoint extracts user info from the JWT token
- Role is determined by Keycloak roles in the token
- Country is detected from IP address during sync
