# Troubleshooting: Account Linking Stopped Working

## Common Causes

### 1. Identity Provider Using Wrong Flow

**Problem**: The Identity Provider (Google/GitHub) might be using the default "First Broker Login" flow instead of your custom "First Broker Login - Auto" flow.

**Solution**:
1. Go to **Identity Providers** → Select your provider (Google/GitHub)
2. Go to **Settings** tab
3. Scroll to **Advanced settings**
4. Check **"First login flow"** - it should be set to `First Broker Login - Auto`
5. If it's set to `First Broker Login` (default), change it to `First Broker Login - Auto`
6. Click **Save**

### 2. Custom Flow Was Deleted or Reset

**Problem**: Your custom flow `First Broker Login - Auto` might have been deleted or reset to default.

**Solution**:
1. Go to **Authentication** → **Flows**
2. Check if `First Broker Login - Auto` exists in the list
3. If it doesn't exist, you need to recreate it:
   - Find "First Broker Login" (built-in)
   - Click **Copy** to create a new copy
   - Name it: `First Broker Login - Auto`
   - Configure it as described in SOCIAL_AUTH_SETUP.md

### 3. Flow Configuration Was Reset

**Problem**: The flow configuration might have been reset to default values.

**Solution**:
1. Go to **Authentication** → **Flows** → `First Broker Login - Auto`
2. Check the configuration:
   - "Confirm link existing account" should be **Disabled** (not Required)
   - "Account verification options" can be **Disabled** for automatic linking
3. Re-apply the configurations as needed

### 4. Keycloak Was Restarted/Updated

**Problem**: After Keycloak restart or update, custom flows might need to be reapplied.

**Solution**:
1. Verify the flow still exists
2. Verify Identity Providers are still using the correct flow
3. Re-apply configurations if needed

### 5. Using Built-in Flow Instead of Custom Flow

**Problem**: You might be looking at or editing the built-in "First Broker Login" flow instead of your custom "First Broker Login - Auto" flow.

**Solution**:
- The built-in flow shows "Built-in" tag and says "Not in use"
- Make sure you're editing `First Broker Login - Auto` (your custom copy)
- The custom flow should NOT have the "Built-in" tag

## Quick Check List

✅ **Verify Identity Provider Flow Assignment**:
- Identity Providers → [Your Provider] → Settings → Advanced settings → "First login flow" = `First Broker Login - Auto`

✅ **Verify Custom Flow Exists**:
- Authentication → Flows → Look for `First Broker Login - Auto` (should NOT have "Built-in" tag)

✅ **Verify Flow Configuration**:
- "Confirm link existing account" = **Disabled**
- "Account verification options" = **Disabled** (optional, for full auto-linking)

✅ **Test the Flow**:
- Try logging in with social provider
- If account exists, it should automatically link and redirect (no dialog)

## If Nothing Works

1. **Recreate the custom flow**:
   - Delete the old `First Broker Login - Auto` if it exists
   - Copy "First Broker Login" again
   - Name it `First Broker Login - Auto`
   - Configure it from scratch

2. **Check Keycloak logs**:
   - Look for errors related to authentication flows
   - Check if there are any warnings about missing flows

3. **Verify Keycloak version**:
   - Some Keycloak versions handle flows differently
   - Check if you updated Keycloak recently

## Prevention

- **Document your configuration**: Take screenshots of your flow settings
- **Export realm configuration**: Use Keycloak's export feature to backup your realm
- **Test after Keycloak updates**: Always verify flows work after updating Keycloak
