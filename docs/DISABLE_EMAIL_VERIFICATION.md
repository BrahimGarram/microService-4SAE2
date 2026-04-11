# Disable Email Verification for Account Linking

You're seeing an email verification step. Here's how to disable it for automatic account linking:

## Quick Fix

### Option 1: Disable "Account verification options" (Easiest)

1. Go to **Authentication** → **Flows**
2. Click on **"First Broker Login - Auto"** (your custom flow)
3. Find **"Account verification options"** (step 6, under "Handle Existing Account")
4. In the **"Requirement"** column, change it from **"Required"** to **"Disabled"**
5. This disables all verification steps

### Option 2: Disable Email Verification Specifically

1. Go to **Authentication** → **Flows**
2. Click on **"First Broker Login - Auto"**
3. Expand **"Handle Existing Account"** by clicking the dropdown arrow (▼)
4. Expand **"Account verification options"** by clicking its dropdown arrow (▼)
5. Find **"Verify existing account by Email"** (step 7)
6. In the **"Requirement"** column, change it from **"Alternative"** to **"Disabled"**
7. This specifically disables email verification while keeping other options

### Option 3: Disable All Verification Steps

1. In **"First Broker Login - Auto"** flow:
2. Expand **"Account verification options"**
3. Disable these steps:
   - **"Verify existing account by Email"** → Requirement: **"Disabled"**
   - **"Verify Existing Account by Re-authentication"** → Requirement: **"Disabled"**
4. This makes linking completely automatic with no verification

## After Making Changes

1. **Save** the flow (if there's a save button)
2. **Test** by trying to log in with Google/GitHub again
3. If account exists, it should now automatically link without asking for email verification

## What You Should See After Fix

- ✅ Account automatically links
- ✅ Redirects to home page
- ✅ No email verification dialog
- ✅ No "Account already exists" dialog

## Current Flow Steps (What Should Be Disabled)

For automatic linking without any prompts:

1. ✅ "Confirm link existing account" → **Disabled**
2. ✅ "Account verification options" → **Disabled** (or just "Verify existing account by Email" → **Disabled**)

This will make account linking completely automatic!
