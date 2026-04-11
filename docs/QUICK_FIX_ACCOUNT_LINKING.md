# Quick Fix: Restore Automatic Account Linking

If your configuration was working before but stopped, follow these steps to restore it:

## Step 1: Check Your Custom Flow Exists

1. Go to **Authentication** → **Flows** (left sidebar)
2. Look for **"First Broker Login - Auto"** in the list
3. If it exists, note it down
4. If it doesn't exist, you'll need to recreate it (see Step 3)

## Step 2: Set Identity Provider to Use Custom Flow

1. Go to **Identity Providers** → **Google** (or your provider)
2. Click on **Settings** tab
3. Scroll down to **Advanced settings** section
4. Find **"First login flow"** dropdown
5. Change it from **"first broker login"** to **"First Broker Login - Auto"**
6. Click **Save**

**This is usually the fix!** Most of the time, the Identity Provider just switched back to the default flow.

## Step 3: If Custom Flow Doesn't Exist - Recreate It

If "First Broker Login - Auto" doesn't exist in the Flows list:

1. Go to **Authentication** → **Flows**
2. Find **"First Broker Login"** (the built-in one)
3. Click **Copy** button
4. Name it: `First Broker Login - Auto`
5. Click **OK**

### Configure the New Flow:

1. In `First Broker Login - Auto`, find **"Confirm link existing account"** (step 5)
2. In the **Requirement** column, change it from **"Required"** to **"Disabled"**
3. This disables the confirmation dialog and allows automatic linking

### Optional - Disable Verification:

1. Find **"Account verification options"** (step 6)
2. Change its **Requirement** from **"Required"** to **"Disabled"**
3. This makes linking completely automatic (no email verification needed)

4. Go back to **Identity Providers** → **Google** → **Settings**
5. Set **"First login flow"** to **"First Broker Login - Auto"**
6. Click **Save**

## Step 4: Test It

1. Try logging in with Google/GitHub
2. If an account already exists, it should automatically link and redirect to home
3. No dialog should appear

## What Probably Happened

Most likely one of these:
- ✅ Identity Provider switched back to default flow (most common)
- ✅ Keycloak was restarted and configuration was lost
- ✅ Custom flow was accidentally deleted
- ✅ Keycloak was updated and reset some settings

## Still Not Working?

If it's still not working after these steps:

1. **Check Keycloak version** - Some versions handle flows differently
2. **Check logs** - Look for errors in Keycloak server logs
3. **Try a different approach** - Use "Handle Existing Account" sub-flow configuration (see SOCIAL_AUTH_SETUP.md for details)

## Prevention

To prevent this from happening again:
- **Document your settings** - Take a screenshot of your flow configuration
- **Export your realm** - Use Keycloak's export feature to backup your configuration
- **Test after updates** - Always verify flows work after updating Keycloak
