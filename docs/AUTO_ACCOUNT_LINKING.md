# Auto Account Linking Configuration

## Problem

When a user tries to log in with a social provider (Google/GitHub) and an account with that username already exists, Keycloak shows a dialog asking "How do you want to continue?" with options:
- "Review profile"
- "Add to existing account"

This interrupts the user flow. We want to automatically link the account and redirect to the home page.

## Solution

Configure Keycloak's "First Broker Login" flow to automatically link accounts when they already exist.

## Step-by-Step Configuration

### 1. Access Keycloak Admin Console

1. Go to: `http://localhost:8080/admin`
2. Log in with admin credentials
3. Select the realm: `projetpidev`

### 2. Configure First Broker Login Flow

1. Go to **Authentication** (left sidebar)
2. Click on **Flows** (under Authentication)
3. Find the flow **"First Broker Login"**
4. Click **"Copy"** to create a copy
5. Name it: `First Broker Login - Auto`
6. Click **"OK"**

### 3. Configure Handle Existing Account Step

**IMPORTANT**: "Handle Existing Account" is a **sub-flow** (parent flow), so it doesn't have a direct Config icon. Here's how to configure it:

**Option 1: Configure via the sub-flow directly**
1. In the flow `First Broker Login - Auto`, find **"Handle Existing Account"** (step 4)
2. Click on the **"Handle Existing Account"** text itself (it should be a clickable link/blue text)
3. This will open the sub-flow configuration page
4. Look for **"Account linking strategy"** or **"Linking strategy"** setting
5. Change it from **"Ask user"** to **"Link account automatically"**
6. Click **"Save"**

**Option 2: Find the flow in the Flows list**
1. Go to **Authentication** → **Flows** (left sidebar)
2. Look for a flow named **"Handle Existing Account"** in the list
3. Click on it to open its configuration
4. Configure the linking strategy as described above

**Option 3: Disable confirmation step (Simpler approach)**
1. In `First Broker Login - Auto`, expand "Handle Existing Account" by clicking the dropdown arrow (▼)
2. Find **"Confirm link existing account"** (step 5) - this one SHOULD have a Config icon (⚙️)
3. Click the **Config icon** (⚙️) next to "Confirm link existing account"
4. In the configuration, look for options to disable confirmation or enable automatic linking
5. OR simply set its Requirement to **"Disabled"** in the Requirement dropdown
6. This will skip the confirmation dialog and automatically link accounts

### 3b. Disable Confirmation Step (Important)

1. In the same flow, find **"Confirm link existing account"** (step 5)
2. In the **"Requirement"** column dropdown, change it from **"Required"** to **"Disabled"**
3. This prevents showing a confirmation page and allows automatic linking
4. **Note**: If you want completely automatic linking without any verification, you can also disable the verification steps (steps 6-9) by setting their requirements to "Disabled"

### 4. Configure Review Profile (Optional but Recommended)

1. In the same flow, find **"Review Profile"**
2. Click the **Config** icon (gear) next to "Review Profile"
3. In the configuration:
   - **"Update profile on first login"**: OFF (disabled)
   - **"Update profile on first login (required)"**: OFF (disabled)
4. Click **"Save"**

### 5. Apply Flow to Identity Providers

For each Identity Provider (Google, GitHub, etc.):

1. Go to **Identity Providers** (left sidebar)
2. Click on the provider (e.g., **Google** or **GitHub**)
3. Go to the **Settings** tab
4. Scroll down to **Advanced settings**
5. Find **"First login flow"**
6. Select: `First Broker Login - Auto` (the flow you just created)
7. Click **"Save"**

### 6. Verify Flow Order

Make sure the flow has these steps in order:

1. **Review Profile** (configured with update disabled)
2. **User creation or linking**
3. **Create User If Unique**
4. **Handle Existing Account** (configured with "Link account automatically")

### 7. Restart Keycloak (if needed)

After making these changes, restart Keycloak to ensure they take effect.

## Expected Behavior

After configuration:

1. User clicks "Sign in with Google/GitHub"
2. User authenticates with the social provider
3. If account already exists:
   - Keycloak automatically links the social account to the existing account
   - User is redirected to the home page (no dialog shown)
4. If account doesn't exist:
   - Keycloak creates a new account
   - User is redirected to the home page

## Frontend Handling

The frontend (`app.component.ts`) has been updated to:

1. Detect `first-broker-login` redirects
2. Check if user is already logged in
3. Automatically redirect to home page if logged in
4. Handle OAuth errors gracefully

## Troubleshooting

### Issue: Dialog still appears

**Solution:**
- Verify that "Handle Existing Account" step exists in the flow
- Verify that "Account linking strategy" is set to "Link account automatically"
- Verify that the Identity Provider is using the correct flow
- Restart Keycloak

### Issue: "Page has expired" error

**Solution:**
- Make sure "Review Profile" is still in the flow (don't delete it)
- Configure "Review Profile" with update disabled (as shown above)
- Verify all required steps are present in the flow

### Issue: Account not linking automatically

**Solution:**
- Check that the flow is correctly assigned to the Identity Provider
- Verify the flow order (Handle Existing Account should come after User creation steps)
- Check Keycloak logs for errors

## Testing

1. Create a user in Keycloak with username: `testuser`
2. Try to log in with Google/GitHub using the same email
3. The account should automatically link and redirect to home page
4. No dialog should appear

## Notes

- This configuration applies to all Identity Providers that use this flow
- Users can still manually link accounts if needed (via Keycloak user settings)
- The automatic linking uses email matching by default
