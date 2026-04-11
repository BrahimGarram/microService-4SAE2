# Complete Setup Guide

This guide walks you through setting up the entire Freelance Platform from scratch.

## Prerequisites Checklist

- [ ] Java 17 or higher installed
- [ ] Maven 3.6+ installed
- [ ] Node.js 18+ and npm installed
- [ ] XAMPP installed
- [ ] Keycloak server downloaded

## Step-by-Step Setup

### Step 1: Start MySQL (XAMPP)

1. Open **XAMPP Control Panel**
2. Start **MySQL** service
3. Verify MySQL is running on port **3306**
4. Default credentials:
   - Username: `root`
   - Password: `` (empty)

### Step 2: Setup Keycloak

1. **Download Keycloak**
   - Download from: https://www.keycloak.org/downloads
   - Extract to a folder (e.g., `C:\keycloak`)

2. **Start Keycloak**
   ```bash
   cd C:\keycloak\bin
   standalone.bat
   ```
   Or on Linux/Mac:
   ```bash
   ./standalone.sh
   ```

3. **Access Admin Console**
   - Open browser: http://localhost:8080
   - Click "Administration Console"
   - First time: Create admin user (e.g., `admin` / `admin`)

4. **Configure Realm**
   - Follow instructions in `docs/KEYCLOAK_SETUP.md`
   - Create realm: `projetpidev`
   - Create client: `freelance-client`
   - Create roles: `USER`, `FREELANCER`, `ADMIN`
   - Create test users

### Step 3: Build and Run Backend

1. **Navigate to backend directory**
   ```bash
   cd backend
   ```

2. **Build the project**
   ```bash
   mvn clean install
   ```

3. **Update configuration** (if needed)
   - Edit `src/main/resources/application.yml`
   - Verify MySQL connection settings
   - Verify Keycloak issuer-uri

4. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

5. **Verify backend is running**
   - Check console for: "Started UserServiceApplication"
   - Backend should be on: http://localhost:8081
   - Database `freelance_db` should be created automatically

### Step 4: Setup Frontend

1. **Navigate to frontend directory**
   ```bash
   cd frontend
   ```

2. **Install dependencies**
   ```bash
   npm install
   ```

3. **Update Keycloak configuration** (if needed)
   - Edit `src/app/app.module.ts`
   - Verify Keycloak URL, realm, and client ID

4. **Start development server**
   ```bash
   npm start
   # or
   ng serve
   ```

5. **Verify frontend is running**
   - Open browser: http://localhost:4200
   - You should see the login page

### Step 5: Test the Application

1. **Login**
   - Click "Login" button
   - Use Keycloak credentials (e.g., `user1` / `password123`)
   - You'll be redirected to Keycloak login page

2. **First Login Flow**
   - After login, you'll be redirected back to Angular app
   - The app will automatically call `/api/users/sync`
   - Your user profile will be created in the database
   - You should see your profile information

3. **Test Features**
   - View profile on home page
   - Navigate to Profile page
   - Update username/email
   - Logout and login again

## Verification Checklist

- [ ] MySQL is running and accessible
- [ ] Keycloak is running on port 8080
- [ ] Keycloak realm `projetpidev` is created
- [ ] Keycloak client `freelance-client` is configured
- [ ] Test users are created in Keycloak
- [ ] Backend starts without errors
- [ ] Database `freelance_db` is created
- [ ] Frontend starts without errors
- [ ] Login works and redirects properly
- [ ] User profile is synced to database
- [ ] Profile update works

## Common Issues and Solutions

### Issue: Port 8080 Already in Use

**Problem:** Keycloak and Spring Boot both use port 8080

**Solution:** ✅ **Already fixed!** The backend is configured to run on port 8081 by default. If you need to change it:
- Update `application.yml`: `server.port: 8081`
- Update frontend `user.service.ts`: `apiUrl = 'http://localhost:8081/api/users'`

### Issue: MySQL Connection Failed

**Problem:** Cannot connect to MySQL

**Solutions:**
1. Verify XAMPP MySQL is running
2. Check MySQL port (should be 3306)
3. Verify username/password in `application.yml`
4. Check if MySQL allows connections from localhost

### Issue: Keycloak Login Redirect Fails

**Problem:** After login, redirect doesn't work

**Solutions:**
1. Check client redirect URIs in Keycloak:
   - Should include: `http://localhost:4200/*`
2. Check Web origins:
   - Should include: `http://localhost:4200`
3. Verify client is public (not confidential)

### Issue: JWT Token Validation Fails

**Problem:** Backend rejects JWT tokens

**Solutions:**
1. Verify `issuer-uri` in `application.yml`:
   - Should be: `http://localhost:8080/realms/projetpidev`
2. Check Keycloak realm name matches
3. Verify Keycloak is accessible from backend
4. Check token expiration (default is 5 minutes)

### Issue: CORS Errors

**Problem:** Browser shows CORS errors

**Solutions:**
1. Verify CORS configuration in `SecurityConfig.java`
2. Check allowed origins includes `http://localhost:4200`
3. Verify `allowCredentials` is set to `true`
4. Check Keycloak Web origins configuration

### Issue: User Not Found After Login

**Problem:** Login works but user profile not found

**Solutions:**
1. Call `/api/users/sync` endpoint manually
2. Check if user exists in database
3. Verify JWT token contains correct claims
4. Check backend logs for errors

## Development Tips

### Backend Development

1. **Enable SQL logging**: Already enabled in `application.yml`
2. **Check logs**: Look for JWT validation errors
3. **Test endpoints**: Use Postman or curl with JWT tokens
4. **Database inspection**: Use phpMyAdmin (XAMPP) to view data

### Frontend Development

1. **Check browser console**: Look for errors
2. **Network tab**: Verify JWT token is sent in requests
3. **Keycloak token**: Decode at https://jwt.io to inspect claims
4. **Angular DevTools**: Use browser extension for debugging

### Keycloak Development

1. **Token inspection**: Use https://jwt.io
2. **Admin console**: Check user roles and assignments
3. **Client settings**: Verify redirect URIs and Web origins
4. **Realm settings**: Check token lifespans

## Next Steps

After successful setup:

1. **Add more features**:
   - Project management
   - Payment processing
   - Messaging system

2. **Enhance security**:
   - Enable HTTPS
   - Configure proper token lifespans
   - Add rate limiting

3. **Improve UI**:
   - Add more components
   - Improve styling
   - Add loading states

4. **Add testing**:
   - Unit tests
   - Integration tests
   - E2E tests

5. **Deploy**:
   - Containerize with Docker
   - Deploy to cloud
   - Set up CI/CD

## Support

If you encounter issues:

1. Check the logs (backend and frontend)
2. Verify all services are running
3. Check configuration files
4. Review error messages carefully
5. Consult documentation in `docs/` folder

---

**Happy Coding! 🚀**
