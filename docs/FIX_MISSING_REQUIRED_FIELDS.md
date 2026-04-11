# Fix: Missing Required Fields Error

If you added new fields to the database and they're required (NOT NULL), you need to ensure they're set during user sync.

## Step 1: Identify Missing Fields

### Check Database Schema

1. Open **phpMyAdmin** (XAMPP) or MySQL client
2. Select database: `freelance_db`
3. Go to table: `users`
4. Click **Structure** tab
5. Look for columns with **Null = NO** (these are required)
6. Note which fields are required but might not be set

### Check User Entity

1. Open: `backend/src/main/java/com/freelance/userservice/model/User.java`
2. Look for fields with `@Column(nullable = false)`
3. Check if these fields are set in `syncUser()` method

## Step 2: Fix Missing Fields

### Option 1: Set Default Values in Entity

If the field can have a default value, add it to the entity:

```java
@Column(nullable = false)
private String newField = "defaultValue"; // Add default value

@PrePersist
protected void onCreate() {
    // ... existing code ...
    if (newField == null) {
        newField = "defaultValue";
    }
}
```

### Option 2: Set Values in syncUser() Method

If the field needs to be set during sync, update `UserService.syncUser()`:

```java
// In UserService.java, syncUser() method
user.setNewField("someValue"); // Set the new field
```

### Option 3: Make Field Nullable

If the field doesn't need to be required, make it nullable in the database:

```sql
ALTER TABLE users MODIFY new_field VARCHAR(255) NULL;
```

And update the entity:

```java
@Column(nullable = true) // Change to nullable
private String newField;
```

## Step 3: Common Required Fields

Based on your User entity, these fields are required and should be set:

✅ **Already Set in syncUser():**
- `keycloakId` - Set from JWT
- `username` - Set from JWT
- `email` - Set from JWT
- `role` - Set from JWT

✅ **Set in @PrePersist:**
- `createdAt` - Auto-set
- `updatedAt` - Auto-set
- `availability` - Defaults to OFFLINE
- `verified` - Defaults to false

## Step 4: Check Backend Logs

When a new user tries to log in, check backend logs for:

```
Error saving user to database - Username: ..., Email: ..., Error: ...
```

This will tell you exactly which field is missing.

## Step 5: Test

1. Try logging in with a new Google account
2. Check backend logs for errors
3. If error mentions a specific field, add it to the sync process

## Example: If You Added a "phone" Field

If you added a required `phone` field:

### Make it Optional (Recommended):
```java
@Column(nullable = true) // Allow null
private String phone;
```

### Or Set Default:
```java
@Column(nullable = false)
private String phone = ""; // Empty string default

@PrePersist
protected void onCreate() {
    // ... existing code ...
    if (phone == null) {
        phone = "";
    }
}
```

### Or Set During Sync:
```java
// In UserService.syncUser()
user.setPhone(""); // Or extract from JWT if available
```

## Quick Fix

**Tell me which fields you added**, and I can help you:
1. Update the User entity
2. Update the syncUser() method
3. Set appropriate default values

What fields did you add to the database?
