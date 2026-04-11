# Recreate Custom Flow - Step by Step

Since your "First Broker Login - Auto" flow is missing, here's how to recreate it:

## Step 1: Create the Custom Flow

1. Go to **Authentication** → **Flows** (left sidebar)
2. Find **"First Broker Login"** in the list (the built-in one)
3. Click the **"Copy"** button (or right-click → Copy)
4. In the dialog that appears, enter the name: `First Broker Login - Auto`
5. Click **OK** or **Save**

You should now see "First Broker Login - Auto" in your flows list.

## Step 2: Configure the Flow

1. Click on **"First Broker Login - Auto"** to open it
2. You'll see a table with steps

### ⚠️ IMPORTANT - Don't Disable These:

- **"User creation or linking"** → MUST stay as **"Required"** (don't change this!)
- **"Create User If Unique"** → Should be **"Alternative"** (don't disable this!)

### Disable "Confirm link existing account":

1. Expand **"Handle Existing Account"** by clicking the dropdown arrow (▼)
2. Find **"Confirm link existing account"** (step 5, under "Handle Existing Account")
3. In the **"Requirement"** column (right side), click the dropdown
4. Change it from **"Required"** or **"Alternative"** to **"Disabled"**
5. This removes the confirmation dialog

### IMPORTANT - Disable Email Verification (to skip email verification):

1. Still in the expanded "Handle Existing Account" section
2. Find **"Account verification options"** (step 6)
3. Change its **"Requirement"** from **"Required"** to **"Disabled"**
4. This skips email verification and makes linking completely automatic
5. **OR** expand "Account verification options" and disable the sub-steps:
   - Find **"Verify existing account by Email"** (step 7)
   - Change its **"Requirement"** from **"Alternative"** to **"Disabled"**
   - This specifically disables email verification

### What NOT to Disable:

❌ **"User creation or linking"** - MUST be **Required** (critical step!)
❌ **"Create User If Unique"** - Should be **Alternative** (needed for new users)
✅ **"Confirm link existing account"** - Can be **Disabled** (this is what we want)
✅ **"Account verification options"** - Can be **Disabled** (this is what we want)

## Step 3: Apply to Identity Provider

1. Go to **Identity Providers** → **Google** (or your provider)
2. Click **Settings** tab
3. Scroll down to **Advanced settings**
4. Find **"First login flow"** dropdown
5. Change it from **"first broker login"** to **"First Broker Login - Auto"**
6. Click **Save** at the bottom

## Step 4: Do the Same for Other Providers

If you have GitHub or other providers:
1. Go to **Identity Providers** → **GitHub** (or other)
2. **Settings** → **Advanced settings**
3. Set **"First login flow"** to **"First Broker Login - Auto"**
4. Click **Save**

## That's It!

Now when users log in with social providers and an account already exists, it will automatically link and redirect to home - no dialog!

## Test It

1. Try logging in with Google/GitHub
2. If account exists, it should automatically link and go to home page
3. No "Account already exists" dialog should appear
