# Fix: "Invalid username or password" Error in Keycloak

This error appears when Keycloak tries to verify/link an account using password authentication, but social logins don't have passwords.

## Problem

The flow is trying to verify the account with a password, which fails for Google/GitHub logins.

## Solution: Disable All Verification Steps

### Step 1: Check Your Flow Configuration

Go to **Authentication** → **Flows** → **"Copy of first broker login"** (or your custom flow name)

### Step 2: Disable Verification Steps

In the **"Handle Existing Account"** sub-flow, make sure these are **Disabled**:

1. **"Account verification options"** → Requirement: **"Disabled"**
   - This is the main one causing the password error

2. **"Verify existing account by Email"** → Requirement: **"Disabled"**
   - (Inside "Account verification options")

3. **"Verify Existing Account by Re-authentication"** → Requirement: **"Disabled"**
   - (Inside "Account verification options")
   - This is trying to ask for a password!

4. **"Username Password Form for identity provider reauthentication"** → Requirement: **"Disabled"**
   - (Inside "Verify Existing Account by Re-authentication")
   - This is the password form causing the error!
   - **NOTE**: If you can't change this directly (no dropdown), disable its parent step instead

### Step 3: Verify Main Steps

In the main flow, ensure:
- **"Confirm link existing account"** → **"Disabled"**
- **"Account verification options"** → **"Disabled"**

## Quick Fix

The error is likely coming from **"Verify Existing Account by Re-authentication"** which contains a password form.

1. Go to your flow
2. Expand **"Handle Existing Account"**
3. Expand **"Account verification options"**
4. Expand **"Verify Existing Account by Re-authentication"**
5. Find **"Username Password Form for identity provider reauthentication"**
6. Set its Requirement to **"Disabled"**
7. **OR** disable the entire **"Verify Existing Account by Re-authentication"** step

## If Still Not Working - Complete Reset

If you've disabled everything but still get the error:

1. **Delete your custom flow** completely
2. **Recreate it from scratch** (see COMPLETE_FLOW_RESET.md)
3. **Restart Keycloak** after making changes (this is important!)
4. Make sure you're using the correct flow name in Identity Provider settings

The issue might be that Keycloak cached the old flow configuration. Restarting Keycloak forces it to reload the flow.

## Alternative: Disable Entire Verification Flow

If you want completely automatic linking:

1. In **"Handle Existing Account"** sub-flow
2. Set **"Account verification options"** → **"Disabled"**
3. This disables ALL verification (email, password, etc.)

## Test After Fix

1. Try logging in with Google (new user)
2. Should automatically create account and redirect
3. No "Invalid username or password" error

## Why This Happens

When a new user logs in with Google:
- Keycloak creates the user
- But if "Account verification options" is enabled, it tries to verify
- The verification asks for a password
- Social logins don't have passwords → Error!

**Solution**: Disable all verification steps for automatic account creation.
