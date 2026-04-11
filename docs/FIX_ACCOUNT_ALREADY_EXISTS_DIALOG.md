# Fix: "Account already exists" Dialog

Now that new users work, you need to configure automatic account linking for existing users.

## Solution: Disable "Confirm link existing account"

### Step 1: Find the Step

1. Go to **Authentication** → **Flows** → Your custom flow
2. Expand **"Handle Existing Account"** (click the dropdown arrow)
3. Find **"Confirm link existing account"**

### Step 2: Disable It

1. Find **"Confirm link existing account"** in the list
2. In the **"Requirement"** column, there should be a dropdown
3. Change it from **"Required"** to **"Disabled"**
4. This removes the confirmation dialog

### Step 3: Verify

Make sure these are set:
- ✅ **"Confirm link existing account"** → **"Disabled"**
- ✅ **"Account verification options"** → **"Disabled"**

## Alternative: Configure Auto-Linking Strategy

If you can access the "Handle Existing Account" sub-flow configuration:

1. Click on **"Handle Existing Account"** (the text itself, not the dropdown)
2. This opens the sub-flow configuration page
3. Look for **"Account linking strategy"** or **"Linking strategy"**
4. Change it from **"Ask user"** to **"Link account automatically"**
5. Save

## Quick Fix Summary

In your flow, under "Handle Existing Account":
- **"Confirm link existing account"** → **"Disabled"** ✅
- **"Account verification options"** → **"Disabled"** ✅

This will automatically link existing accounts without showing any dialog!

## Test

1. Try logging in with Google using an email that already exists in your database
2. Should automatically link and redirect to home
3. No "Account already exists" dialog
