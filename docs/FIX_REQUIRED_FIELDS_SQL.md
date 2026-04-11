# Fix Required Fields - SQL Commands

If you added fields like `profile_picture` and made them NOT NULL, here's how to fix it.

## Step 1: Check Which Fields Are Required

Run this SQL in phpMyAdmin:

```sql
SELECT 
    COLUMN_NAME,
    IS_NULLABLE,
    COLUMN_DEFAULT,
    DATA_TYPE
FROM 
    INFORMATION_SCHEMA.COLUMNS
WHERE 
    TABLE_SCHEMA = 'freelance_db'
    AND TABLE_NAME = 'users'
    AND IS_NULLABLE = 'NO'
ORDER BY 
    ORDINAL_POSITION;
```

This shows all fields that are NOT NULL (required).

## Step 2: Make Fields Nullable (Easiest Fix)

If you don't need these fields to be required, make them nullable:

```sql
-- Make profile_picture nullable
ALTER TABLE users MODIFY profile_picture VARCHAR(500) NULL;

-- Make bio nullable
ALTER TABLE users MODIFY bio TEXT NULL;

-- Make city nullable
ALTER TABLE users MODIFY city VARCHAR(100) NULL;

-- Make timezone nullable
ALTER TABLE users MODIFY timezone VARCHAR(50) NULL;

-- Make hourly_rate nullable
ALTER TABLE users MODIFY hourly_rate DECIMAL(10,2) NULL;
```

## Step 3: Or Set Default Values

If you want to keep them required, set defaults:

```sql
-- Set default empty string for profile_picture
ALTER TABLE users MODIFY profile_picture VARCHAR(500) NOT NULL DEFAULT '';

-- Set default empty string for bio
ALTER TABLE users MODIFY bio TEXT NOT NULL DEFAULT '';

-- Set default empty string for city
ALTER TABLE users MODIFY city VARCHAR(100) NOT NULL DEFAULT '';

-- Set default empty string for timezone
ALTER TABLE users MODIFY timezone VARCHAR(50) NOT NULL DEFAULT '';

-- Set default 0 for hourly_rate
ALTER TABLE users MODIFY hourly_rate DECIMAL(10,2) NOT NULL DEFAULT 0.00;
```

## Step 4: Test

After running the SQL:
1. Restart your backend
2. Try logging in with a new Google account
3. Should work now!

## Quick Fix (Recommended)

Run this to make all optional fields nullable:

```sql
ALTER TABLE users 
    MODIFY profile_picture VARCHAR(500) NULL,
    MODIFY bio TEXT NULL,
    MODIFY city VARCHAR(100) NULL,
    MODIFY timezone VARCHAR(50) NULL,
    MODIFY hourly_rate DECIMAL(10,2) NULL;
```

This allows these fields to be NULL, which is fine since they're optional profile fields.

## Why This Fixes It

When you try to INSERT a new user:
- If a field is NOT NULL and has no default, MySQL requires a value
- If no value is provided, INSERT fails
- The error might show as "Invalid username or password" in Keycloak

Making fields nullable or setting defaults fixes this!
