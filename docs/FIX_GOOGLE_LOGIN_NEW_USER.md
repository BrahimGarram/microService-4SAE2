# Fix: Google Login Error for New Users

If Google login works for existing users but fails for new users, the issue is in the "First Broker Login" flow configuration.

## Problem

The flow can link existing accounts but can't create new users. This usually means:
- "Create User If Unique" step is disabled or misconfigured
- User creation flow is broken

## Solution: Fix Flow Configuration

### Step 1: Check Main Flow Steps

Go to **Authentication** → **Flows** → **"First Broker Login - Auto"**

Verify these steps are configured correctly:

1. **"Review Profile"** → Requirement: **"Disabled"** ✅ (OK)
2. **"User creation or linking"** → Requirement: **"Required"** ✅ (MUST be Required!)
3. **"Create User If Unique"** → Requirement: **"Alternative"** ✅ (MUST be Alternative, not Disabled!)
4. **"Handle Existing Account"** → Requirement: **"Alternative"** ✅ (OK)

### Step 2: Verify "Create User If Unique" Configuration

1. In the flow, find **"Create User If Unique"** (step 3)
2. Click the **Config icon** (⚙️) next to it
3. Check the configuration:
   - Should allow creating new users
   - Should not be disabled
4. Make sure its **Requirement** is set to **"Alternative"** (not Disabled)

### Step 3: Check "User creation or linking" Sub-flow

1. Expand **"User creation or linking"** by clicking the dropdown arrow (▼)
2. You should see:
   - **"Create User If Unique"** → **Alternative**
   - **"Handle Existing Account"** → **Alternative**
3. Both should be **Alternative** (not Disabled)

### Step 4: Verify Identity Provider Settings

1. Go to **Identity Providers** → **Google** → **Settings**
2. Scroll to **Advanced settings**
3. Check:
   - **"First login flow"**: Should be `First Broker Login - Auto`
   - **"Sync mode"**: Should be **"Import"** (this creates users automatically)
4. Click **Save**

## Common Issues

### Issue 1: "Create User If Unique" is Disabled

**Symptom**: Works for existing users, fails for new users

**Fix**:
1. In "First Broker Login - Auto" flow
2. Find "Create User If Unique"
3. Change Requirement from "Disabled" to **"Alternative"**

### Issue 2: Sync Mode is Wrong

**Symptom**: User not created in Keycloak

**Fix**:
1. Identity Providers → Google → Settings → Advanced settings
2. Set **"Sync mode"** to **"Import"**
3. This tells Keycloak to automatically create users from social providers

### Issue 3: Flow Order is Wrong

**Symptom**: Flow doesn't execute properly

**Fix**: Make sure the flow has these steps in order:
1. Review Profile (Disabled)
2. User creation or linking (Required)
   - Create User If Unique (Alternative)
   - Handle Existing Account (Alternative)
     - Confirm link existing account (Disabled)
     - Account verification options (Disabled)

## Test After Fix

1. **Test with NEW user** (user that doesn't exist):
   - Log in with Google using an email that's not in your database
   - Should create new user and redirect to home
   - No errors

2. **Test with EXISTING user** (user that exists):
   - Log in with Google using an email that's already in your database
   - Should automatically link and redirect to home
   - No errors

## If Still Not Working

### Recreate Flow from Scratch

1. Delete "First Broker Login - Auto"
2. Copy "First Broker Login" again
3. Name it: `First Broker Login - Auto`
4. **ONLY** make these changes:
   - "Review Profile" → Config → "Update profile on first login": OFF
   - "Confirm link existing account" → Disabled
   - "Account verification options" → Disabled
5. **DON'T** disable "Create User If Unique" or "User creation or linking"
6. Apply to Identity Provider

## Verify Configuration

Your flow should look like this:

```
First Broker Login - Auto
├── Review Profile (Disabled)
├── User creation or linking (Required) ← MUST be Required!
│   ├── Create User If Unique (Alternative) ← MUST be Alternative!
│   └── Handle Existing Account (Alternative)
│       ├── Confirm link existing account (Disabled)
│       └── Account verification options (Disabled)
```

The key is: **"Create User If Unique"** must be **Alternative** (not Disabled)!
