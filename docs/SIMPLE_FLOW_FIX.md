# Simple Fix: Recreate Flow Safely

The "Invalid username or password" error means the flow is broken. Let's recreate it with minimal changes:

## Step 1: Delete and Recreate

1. Go to **Authentication** → **Flows**
2. Find **"First Broker Login - Auto"**
3. Delete it (if possible) or just create a new one
4. Go to **"First Broker Login"** (built-in)
5. Click **Copy**
6. Name it: `First Broker Login - Auto`
7. Click **OK**

## Step 2: Make ONLY These Changes

**In the main "First Broker Login - Auto" flow:**

1. Find **"Review Profile"** (step 1)
   - Click the **Config icon** (⚙️) if visible
   - Set **"Update profile on first login"** to **OFF**
   - Click **Save**
   - OR just leave it as **"Disabled"** in Requirement column

2. **DON'T TOUCH** these steps (leave them as default):
   - ✅ "User creation or linking" → **Required** (MUST stay Required!)
   - ✅ "Create User If Unique" → **Alternative** (leave as is)
   - ✅ "Handle Existing Account" → **Alternative** (leave as is)

## Step 3: Configure Handle Existing Account Sub-flow

1. Click on **"Handle Existing Account"** (the text itself, or expand it)
2. This opens the sub-flow configuration
3. Find **"Confirm link existing account"**
4. Change Requirement from **"Required"** to **"Disabled"**
5. Find **"Account verification options"**
6. Change Requirement from **"Required"** to **"Disabled"**
7. **Save** the sub-flow

## Step 4: Apply to Identity Provider

1. Go to **Identity Providers** → **Google** → **Settings**
2. Scroll to **Advanced settings**
3. Set **"First login flow"** to **"First Broker Login - Auto"**
4. Click **Save**

## Step 5: Test

Try logging in with Google again. It should:
- ✅ Automatically link existing accounts
- ✅ Skip email verification
- ✅ Redirect to home page
- ✅ No password error

## If Still Getting Password Error

The error might be because Keycloak is trying to verify the account with a password. Try this:

1. In **Identity Providers** → **Google** → **Settings**
2. Make sure **"Trust Email"** is **ON** (enabled)
3. This tells Keycloak to trust the email from Google without verification

## Alternative: Use Default Flow with Trust Email

If recreating doesn't work, try this simpler approach:

1. In **Identity Providers** → **Google** → **Settings**
2. Set **"First login flow"** back to **"first broker login"** (default)
3. Make sure **"Trust Email"** is **ON**
4. This might allow automatic linking without custom flow

Try the recreation first - it should fix the password error!
