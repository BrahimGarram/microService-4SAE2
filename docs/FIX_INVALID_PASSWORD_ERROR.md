# Fix: "Invalid username or password" Error

This error usually means the flow configuration is broken. Here's how to fix it:

## Solution: Restore Proper Flow Configuration

### Step 1: Check Flow Requirements

In your "First Broker Login - Auto" flow, make sure these are set correctly:

1. **"Review Profile"** → Requirement: **"Disabled"** (this is OK)
2. **"User creation or linking"** → Requirement: **"Required"** (MUST be Required)
3. **"Create User If Unique"** → Requirement: **"Alternative"** (this is OK)
4. **"Handle Existing Account"** → Requirement: **"Alternative"** (this is OK)

### Step 2: Configure "Handle Existing Account" Sub-flow

Inside "Handle Existing Account" sub-flow:

1. **"Confirm link existing account"** → Requirement: **"Disabled"** ✅ (this is correct)
2. **"Account verification options"** → Requirement: **"Disabled"** ✅ (this is correct)

**BUT** - Make sure you didn't disable "User creation or linking" or other critical steps!

### Step 3: Verify Main Flow Steps

Go back to the main "First Broker Login - Auto" flow and check:

- ✅ "Review Profile" → **Disabled** (OK)
- ✅ "User creation or linking" → **Required** (MUST be Required - don't disable this!)
- ✅ "Create User If Unique" → **Alternative** (OK)
- ✅ "Handle Existing Account" → **Alternative** (OK)

### Step 4: Alternative - Recreate the Flow

If it's still not working, recreate the flow:

1. **Delete** "First Broker Login - Auto" (if you can)
2. **Copy** "First Broker Login" again
3. Name it: `First Broker Login - Auto`
4. **ONLY** disable these:
   - "Confirm link existing account" → **Disabled**
   - "Account verification options" → **Disabled**
5. **DON'T** disable "User creation or linking" or other required steps
6. Apply to Identity Provider

## Common Mistakes

❌ **DON'T disable:**
- "User creation or linking" (MUST be Required)
- "Create User If Unique" (should be Alternative)
- Main flow steps that are marked as Required

✅ **DO disable:**
- "Confirm link existing account" (inside Handle Existing Account)
- "Account verification options" (inside Handle Existing Account)
- "Verify existing account by Email" (optional, if you want)

## Test After Fix

1. Try logging in with Google/GitHub again
2. Should automatically link and redirect
3. No email verification
4. No password error

The error suggests a critical step was disabled. Make sure "User creation or linking" is set to **Required**!
