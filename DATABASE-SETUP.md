# Database Setup Guide

## Prerequisites
- XAMPP installed with MySQL running on port 3306
- Default credentials: username `root`, password `` (empty)

## Databases Used

| Service | Database Name |
|---|---|
| User Service | `freelance_db` |
| Project Service | `freelance_db` |
| Complaints Service | `freelancify-local` |

Both databases are **auto-created** on first run via `createDatabaseIfNotExist=true`.

## If your MySQL has a password

Edit these 3 files and set your password:

**backend/src/main/resources/application.yml**
```yaml
spring:
  datasource:
    password: "YOUR_PASSWORD"
```

**project-service/src/main/resources/application.yml**
```yaml
spring:
  datasource:
    password: "YOUR_PASSWORD"
```

**complaints-service/src/main/resources/application.yml**
```yaml
spring:
  datasource:
    password: "YOUR_PASSWORD"
```

## Verify MySQL is running

Open XAMPP Control Panel and make sure MySQL shows "Running" (green).

Or test connection:
```
mysql -u root -p -h localhost -P 3306
```

## After first run

Tables are auto-created by Hibernate (`ddl-auto: update`).
No manual SQL scripts needed.
