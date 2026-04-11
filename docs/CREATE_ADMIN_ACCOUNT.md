# Create Admin Account in Keycloak

## Step 1: Create User in Keycloak

1. Go to **Keycloak Admin Console**: `http://localhost:8080/admin`
2. Log in with your Keycloak admin credentials (default: `admin` / `admin`)
3. Select the realm: **`projetpidev`** (from dropdown at top left)
4. Go to **Users** (left sidebar)
5. Click **"Create new user"** button (top right)

## Step 2: Fill User Details

Fill in the form:
- **Username**: `admin` (or your preferred admin username)
- **Email**: `admin@example.com` (or your email)
- **Email verified**: Toggle **ON** (important!)
- **First name**: (optional)
- **Last name**: (optional)

Click **"Create"** button

## Step 3: Set Password

1. Go to **Credentials** tab (top of user page)
2. Click **"Set password"** button
3. Enter password: `password123` (or your preferred password)
4. **Temporary**: Toggle **OFF** (so password is permanent, not temporary)
5. Click **"Set password"** button

## Step 4: Assign ADMIN Role

1. Go to **Role mapping** tab (top of user page)
2. Click **"Assign role"** button
3. In the dialog, filter by **"Realm roles"** (use the filter/search)
4. Find and select **"ADMIN"** role
5. Click **"Assign"** button

## Step 5: Verify

1. Go back to **Users** list
2. Find your admin user
3. Click on it
4. Go to **Role mapping** tab
5. You should see **"ADMIN"** in the **"Assigned roles"** list

## Step 6: Sync User to Database

Users created in Keycloak are NOT automatically in MySQL. You need to sync them. Here are two ways:

### Option 1: Login (Automatic Sync) - EASIEST

1. Log out of Keycloak Admin Console (if logged in)
2. Go to your Angular app: `http://localhost:4200`
3. Click **Login**
4. Enter:
   - **Username**: `admin` (or the username you created)
   - **Password**: `password123` (or the password you set)
5. After login, the frontend automatically calls `/api/users/sync`
6. The user will be created in MySQL database
7. You should be redirected to `/admin/dashboard` (if you're an admin)

### Option 2: Manual Sync via API

If you want to sync without logging in:

1. **Get JWT Token**:
   - Use Postman or curl to get a token:
   ```bash
   curl -X POST http://localhost:8080/realms/projetpidev/protocol/openid-connect/token \
     -H "Content-Type: application/x-www-form-urlencoded" \
     -d "client_id=freelance-client" \
     -d "username=admin" \
     -d "password=password123" \
     -d "grant_type=password"
   ```
   - Copy the `access_token` from the response

2. **Call Sync Endpoint**:
   ```bash
   curl -X POST http://localhost:8081/api/users/sync \
     -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
   ```

3. **Verify in Database**:
   - Check MySQL database `freelance_db`
   - Table `users` should now have your admin user

## Step 7: Verify User in Database

1. Open phpMyAdmin (XAMPP) or MySQL client
2. Select database: `freelance_db`
3. Go to table: `users`
4. You should see your admin user with:
   - `username`: `admin`
   - `email`: `admin@example.com`
   - `role`: `ADMIN`
   - `keycloak_id`: (the Keycloak user ID)

## Alternative: Create Admin via Backend API

If you want to create an admin via your backend:

1. Use the registration endpoint: `POST /api/users/register`
2. Send:
```json
{
  "username": "admin",
  "email": "admin@example.com",
  "password": "password123",
  "role": "ADMIN"
}
```

3. Then assign the ADMIN role in Keycloak (as shown above)

## Notes

- The user must exist in **Keycloak** (for authentication)
- The user must have the **ADMIN** role assigned in Keycloak
- After first login, the user will be synced to your MySQL database via `/api/users/sync`
- The role in Keycloak determines what the user can do in your application

## Troubleshooting

**Issue**: User can't log in
- Check password is set correctly
- Check "Email verified" is ON
- Check "Temporary" password is OFF

**Issue**: User doesn't have admin access
- Verify ADMIN role is assigned in Role mapping tab
- Check the role appears in the JWT token (use jwt.io to decode)

**Issue**: User not synced to database
- After login, the frontend should call `/api/users/sync`
- Check backend logs for sync errors
- Verify JWT token contains correct role
