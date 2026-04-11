# Complete Flow Reset - Fix "Invalid Password" Error

If you're still getting the error, let's completely reset the flow with minimal configuration.

## Step 1: Delete Current Custom Flow

1. Go to **Authentication** → **Flows**
2. Find **"Copy of first broker login"** (or your custom flow name)
3. Delete it (click the delete icon or use Actions → Delete)

## Step 2: Create Fresh Flow

1. Find **"First Broker Login"** (the built-in one)
2. Click **Copy**
3. Name it: `First Broker Login - Simple`
4. Click **OK**

## Step 3: Minimal Configuration (ONLY These Changes)

### In the Main Flow:

1. **"Review Profile"** → Click **Config icon** (⚙️)
   - **"Update profile on first login"**: OFF
   - **"Update profile on first login (required)"**: OFF
   - Click **Save**

### In "Handle Existing Account" Sub-flow:

1. Click on **"Handle Existing Account"** (the text itself) to open the sub-flow
2. Set these to **"Disabled"**:
   - **"Confirm link existing account"** → **Disabled**
   - **"Account verification options"** → **Disabled**

3. **IMPORTANT**: Expand "Account verification options" and disable ALL sub-steps:
   - **"Verify existing account by Email"** → **Disabled**
   - **"Verify Existing Account by Re-authentication"** → **Disabled**
     - Inside this, also disable:
     - **"Username Password Form for identity provider reauthentication"** → **Disabled**

### DON'T Change These:

- ✅ **"User creation or linking"** → Keep as **"Required"**
- ✅ **"Create User If Unique"** → Keep as **"Alternative"**
- ✅ **"Handle Existing Account"** → Keep as **"Alternative"**

## Step 4: Apply to Identity Provider

1. Go to **Identity Providers** → **Google** → **Settings**
2. Scroll to **Advanced settings**
3. Set **"First login flow"** to **"First Broker Login - Simple"**
4. Verify **"Sync mode"** is **"Import"**
5. Verify **"Trust Email"** is **ON**
6. Click **Save**

## Step 5: Restart Keycloak (Important!)

After making flow changes, restart Keycloak:
1. Stop Keycloak
2. Start Keycloak again
3. Wait for it to fully start

## Step 6: Test

1. Try logging in with a NEW Google account
2. Should work without password error

## If Still Not Working

### Check Flow Execution Order

The flow should execute in this order:
1. Review Profile (skip - disabled)
2. User creation or linking (Required)
   - If user doesn't exist → Create User If Unique
   - If user exists → Handle Existing Account
     - Skip confirmation (disabled)
     - Skip verification (disabled)

### Alternative: Use Default Flow with Trust Email

If custom flow still doesn't work:

1. In **Identity Providers** → **Google** → **Settings**
2. Set **"First login flow"** back to **"first broker login"** (default)
3. Make sure **"Trust Email"** is **ON**
4. Make sure **"Sync mode"** is **"Import"**
5. This might work with default flow if Trust Email is enabled

### Check Keycloak Logs

1. Check Keycloak server logs
2. Look for errors related to authentication flows
3. The logs will show which step is failing

## Verification Checklist

Before testing, verify:
- [ ] Custom flow is created and assigned to Identity Provider
- [ ] "Account verification options" is **Disabled**
- [ ] "Confirm link existing account" is **Disabled**
- [ ] "Verify Existing Account by Re-authentication" is **Disabled**
- [ ] "Username Password Form" is **Disabled**
- [ ] "Create User If Unique" is **Alternative** (NOT Disabled)
- [ ] "User creation or linking" is **Required** (NOT Disabled)
- [ ] "Sync mode" is **Import**
- [ ] "Trust Email" is **ON**
- [ ] Keycloak has been restarted

Try the complete reset and let me know if it works!
