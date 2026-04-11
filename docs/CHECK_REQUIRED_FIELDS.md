# Check Required Fields in Database

If you added new fields to the database and made them NOT NULL, they need to be set during user sync.

## Step 1: Check Database Schema

1. Open **phpMyAdmin** (XAMPP)
2. Select database: `freelance_db`
3. Go to table: `users`
4. Click **Structure** tab
5. Look for columns with **Null = NO** (these are required)

## Step 2: Check Which Fields Are Required

In the Structure view, check each column:
- If **Null = NO** → Field is required (must have a value)
- If **Null = YES** → Field is optional (can be NULL)

## Step 3: Common Fields That Might Be Required

Based on your table, check if these are required:
- `username` - Should be required (already handled in sync)
- `email` - Should be required (already handled in sync)
- `keycloak_id` - Should be required (already handled in sync)
- `role` - Should be required (already handled in sync)
- `created_at` - Should be required (auto-set by @PrePersist)
- `verified` - Should be required (auto-set to false)

**Check if you made any of these required:**
- `profile_picture` - If required, needs default value
- `bio` - If required, needs default value
- `city` - If required, needs default value
- `timezone` - If required, needs default value
- `hourly_rate` - If required, needs default value

## Step 4: Fix Required Fields

### Option 1: Make Field Nullable (Recommended)

If the field doesn't need to be required:

```sql
ALTER TABLE users MODIFY profile_picture VARCHAR(500) NULL;
ALTER TABLE users MODIFY bio TEXT NULL;
ALTER TABLE users MODIFY city VARCHAR(100) NULL;
ALTER TABLE users MODIFY timezone VARCHAR(50) NULL;
ALTER TABLE users MODIFY hourly_rate DECIMAL(10,2) NULL;
```

### Option 2: Set Default Values

If the field must be required, set a default:

```sql
ALTER TABLE users MODIFY profile_picture VARCHAR(500) NOT NULL DEFAULT '';
ALTER TABLE users MODIFY bio TEXT NOT NULL DEFAULT '';
ALTER TABLE users MODIFY city VARCHAR(100) NOT NULL DEFAULT '';
ALTER TABLE users MODIFY timezone VARCHAR(50) NOT NULL DEFAULT '';
ALTER TABLE users MODIFY hourly_rate DECIMAL(10,2) NOT NULL DEFAULT 0.00;
```

### Option 3: Update Entity and Sync Method

If you want to keep fields required, update the code:

1. Update `User.java` entity to set defaults
2. Update `UserService.syncUser()` to set values

## Step 5: Quick SQL Check

Run this SQL to see which fields are NOT NULL:

```sql
SELECT 
    COLUMN_NAME,
    IS_NULLABLE,
    COLUMN_DEFAULT
FROM 
    INFORMATION_SCHEMA.COLUMNS
WHERE 
    TABLE_SCHEMA = 'freelance_db'
    AND TABLE_NAME = 'users'
    AND IS_NULLABLE = 'NO'
ORDER BY 
    ORDINAL_POSITION;
```

This will show all required fields that don't allow NULL.

## Most Likely Issue

If you recently added fields and made them NOT NULL without defaults, the INSERT will fail. The solution is either:
1. Make them nullable (allow NULL)
2. Set default values in the database
3. Set default values in the Java entity

Tell me which fields you made required, and I'll help you fix them!
