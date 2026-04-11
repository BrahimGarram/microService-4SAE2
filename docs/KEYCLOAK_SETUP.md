# Keycloak Configuration Guide

This guide explains how to set up Keycloak for the Freelance Platform.

## Prerequisites

- Keycloak server running (default: http://localhost:8080)
- Access to Keycloak Admin Console

## Step 1: Create Realm

1. Log in to Keycloak Admin Console: http://localhost:8080/admin
2. Default credentials: `admin` / `admin` (change in production!)
3. Hover over the realm dropdown (top left) and click **"Create Realm"**
4. Enter realm name: `projetpidev`
5. Click **"Create"**

## Step 2: Create Roles

1. In the `projetpidev`, go to **Realm roles** (left sidebar)
2. Click **"Create role"** and create the following roles:
   - `USER`
   - `FREELANCER`
   - `ADMIN`

3. Click **"Save"** for each role

## Step 3: Create Client

1. Go to **Clients** (left sidebar)
2. Click **"Create client"**
3. Configure:
   - **Client type**: OpenID Connect
   - **Client ID**: `freelance-client`
   - Click **"Next"**

4. **Capability config**:
   - ✅ **Client authentication**: OFF (Public client)
   - ✅ **Authorization**: OFF
   - ✅ **Standard flow**: ON (Authorization Code Flow)
   - ✅ **Direct access grants**: ON (for testing)
   - Click **"Next"**

5. **Login settings**:
   - **Root URL**: `http://localhost:4200`
   - **Home URL**: `http://localhost:4200`
   - **Valid redirect URIs**: 
     - `http://localhost:4200/*`
     - `http://localhost:4200`
   - **Web origins**: `http://localhost:4200`
   - **Valid post logout redirect URIs**: `http://localhost:4200/*`
   - Click **"Save"**

## Step 4: Configure Token Settings

1. Go to **Clients** (left sidebar)
2. Click on the client **`freelance-client`** (the one you created in Step 3)
3. Click on the **"Advanced settings"** tab (at the top of the client configuration page)
4. Set **Access Token Lifespan**: `5 minutes` (or as needed)
5. Set **Access Token Lifespan For Implicit Flow**: `15 minutes`
6. Click **"Save"** at the bottom of the page

## Step 5: Configure Realm Roles in Token

1. Go to **Clients** (left sidebar)
2. Click on the client **`freelance-client`**
3. Click on the **"Client scopes"** tab (at the top of the client configuration page)
4. In the **"Assigned client scopes"** table, find the row for **`freelance-client-dedicated`** (this is the dedicated scope for your client)
5. Click on the blue link **`freelance-client-dedicated`** (it's clickable)
6. This will take you to the client scope details page
7. Click on the **"Mappers"** tab (at the top of the page, next to Settings, etc.)
8. Click the **"Create mapper"** button (usually at the top right, or you may see **"Add mapper"**)
9. In the **"Add mapper"** dialog, you have two options:
   - **Option A**: Click **"By configuration"** → Select **"Realm roles"** from the list
   - **Option B**: If you see a search/filter box, type "realm roles" and select **"Realm roles"**

10. Configure the mapper:
    - **Name**: `realm-roles`
    - **Token Claim Name**: `realm_access.roles`
    - **Add to access token**: ON (toggle switch)
    - **Add to ID token**: ON (toggle switch)
    - **Add to userinfo**: ON (toggle switch)
    - Click **"Save"** at the bottom

**Note**: The `freelance-client-dedicated` scope is automatically created for your client and is where you should add client-specific mappers.

## Step 6: Create Test Users

### Create User 1 (Regular User)

1. Go to **Users** (left sidebar)
2. Click **"Create new user"**
3. Fill in:
   - **Username**: `user1`
   - **Email**: `user1@example.com`
   - **Email verified**: ON
   - Click **"Create"**

4. Go to **Credentials** tab:
   - Set **Password**: `password123`
   - **Temporary**: OFF
   - Click **"Set password"**

5. Go to **Role mapping** tab:
   - Click **"Assign role"**
   - Filter by **Realm roles**
   - Select `USER`
   - Click **"Assign"**

### Create User 2 (Freelancer)

1. Create user: `freelancer1` / `freelancer1@example.com` / `password123`
2. Assign role: `FREELANCER`

### Create User 3 (Admin)

1. Create user: `admin1` / `admin1@example.com` / `password123`
2. Assign role: `ADMIN`

## Step 7: Verify Configuration

### Test Token Endpoint

You can test the token endpoint using curl or Postman:

#### Using cURL:

```bash
curl -X POST http://localhost:8080/realms/projetpidev/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=freelance-client" \
  -d "username=user1" \
  -d "password=password123" \
  -d "grant_type=password"
```

#### Using Postman:

1. **Method**: `POST`
2. **URL**: `http://localhost:8080/realms/projetpidev/protocol/openid-connect/token`
3. **Headers**: 
   - `Content-Type`: `application/x-www-form-urlencoded`
4. **Body** (select `x-www-form-urlencoded`):
   - `client_id`: `freelance-client`
   - `username`: `user1` (or any test user you created)
   - `password`: `password123`
   - `grant_type`: `password`

**Important**: Make sure all four parameters (`client_id`, `username`, `password`, `grant_type`) are included in the body!

This should return an access token. Decode it at https://jwt.io to verify it contains:
- `sub`: User ID
- `preferred_username`: Username
- `email`: Email
- `realm_access.roles`: Array of roles

## Step 8: Update Backend Configuration

Make sure your `application.yml` has the correct Keycloak URL:

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://localhost:8080/realms/projetpidev
```

**Note**: If Keycloak runs on a different port, update the URL accordingly.

## Troubleshooting

### Issue: "Invalid client or Invalid client credentials" (401 Unauthorized)

If you get this error when testing the token endpoint, check the following:

1. **Verify Direct Access Grants is enabled:**
   - Go to **Clients** → `freelance-client` → **Settings** tab
   - Scroll down to **"Access settings"**
   - Make sure **"Direct access grants"** is **ON** (enabled)
   - Click **"Save"** if you made changes

2. **Verify Client Authentication is OFF:**
   - In the same **Settings** tab, check **"Client authentication"**
   - For a public client, this should be **OFF**
   - If it's ON, turn it OFF and click **"Save"**

3. **Verify the client exists:**
   - Go to **Clients** (left sidebar)
   - Make sure `freelance-client` appears in the list
   - If not, create it following Step 3

4. **Verify the user exists and password is correct:**
   - Go to **Users** (left sidebar)
   - Check if `user1` exists
   - Click on the user → **Credentials** tab
   - Verify the password is set correctly
   - If needed, reset the password

5. **Check realm settings:**
   - Go to **Realm settings** → **Login** tab
   - Make sure **"User registration"** and **"Forgot password"** don't block access
   - **"Direct grant flow"** should be enabled (default)

6. **Try with a different grant type (if testing from browser):**
   - The `password` grant type requires Direct access grants
   - Make sure it's enabled in client settings

### Issue: CORS errors in browser
- **Solution**: Make sure `Web origins` in client settings includes `http://localhost:4200`

### Issue: Invalid redirect URI
- **Solution**: Add the exact redirect URI to **Valid redirect URIs** in client settings

### Issue: Roles not in token
- **Solution**: Verify the realm roles mapper is configured correctly and roles are assigned to users

### Issue: Backend can't validate token
- **Solution**: Check that `issuer-uri` in `application.yml` matches your Keycloak realm URL exactly

## Production Considerations

1. **Change default admin password**
2. **Use HTTPS** for Keycloak
3. **Configure proper token lifespans**
4. **Set up proper CORS origins**
5. **Use environment variables** for configuration
6. **Enable audit logging**
7. **Set up proper user registration flows**
