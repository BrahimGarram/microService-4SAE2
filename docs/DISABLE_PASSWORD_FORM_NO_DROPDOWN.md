# Fix: Can't Change Password Form Requirement (No Dropdown)

If you see "Username Password Form for identity provider reauthentication" with "Required" but no dropdown to change it, here's how to fix it:

## Solution: Disable the Parent Step

You can't change nested steps directly. Disable the parent step instead:

### Option 1: Disable "Verify Existing Account by Re-authentication" (Easiest)

1. In your flow, find **"Verify Existing Account by Re-authentication"** (the parent step)
2. This should have a dropdown in the "Requirement" column
3. Change it from **"Alternative"** to **"Disabled"**
4. This will disable the entire re-authentication flow, including the password form

### Option 2: Disable "Account verification options" (Complete Fix)

1. Find **"Account verification options"** (the top-level parent)
2. Change its Requirement from **"Required"** to **"Disabled"**
3. This disables ALL verification (email, password, re-authentication)
4. This is the cleanest solution

### Option 3: Navigate to the Sub-flow

If you want to change the password form directly:

1. Click on **"Verify Existing Account by Re-authentication"** (the text itself, not the dropdown)
2. This opens the sub-flow configuration page
3. Find **"Username Password Form for identity provider reauthentication"**
4. You should now be able to change its Requirement to **"Disabled"**
5. Save the sub-flow
6. Go back to the main flow

## Recommended: Disable "Account verification options"

The easiest and most reliable fix:

1. In your main flow, find **"Account verification options"**
2. Change its Requirement to **"Disabled"**
3. This automatically disables all nested verification steps:
   - Email verification
   - Re-authentication
   - Password form
   - Everything inside it

## Step-by-Step

1. Go to **Authentication** → **Flows** → Your custom flow
2. Expand **"Handle Existing Account"**
3. Find **"Account verification options"**
4. In the **"Requirement"** column, click the dropdown
5. Select **"Disabled"**
6. Save

This should fix the "Invalid username or password" error!

## Why This Works

When you disable a parent step, all its child steps are also disabled. So disabling "Account verification options" disables:
- Email verification
- Re-authentication  
- Password form
- All nested steps

This prevents Keycloak from trying to verify accounts with passwords that don't exist for social logins.
