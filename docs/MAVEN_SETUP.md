# Maven Setup Guide for Windows

## Option 1: Install Maven (Recommended)

### Step 1: Download Maven
1. Go to: https://maven.apache.org/download.cgi
2. Download the **Binary zip archive** (e.g., `apache-maven-3.9.5-bin.zip`)
3. Extract it to a location like `C:\Program Files\Apache\maven` or `C:\maven`

### Step 2: Add Maven to PATH
1. Press `Win + X` and select **"System"**
2. Click **"Advanced system settings"**
3. Click **"Environment Variables"**
4. Under **"System variables"**, find and select **"Path"**, then click **"Edit"**
5. Click **"New"** and add the path to Maven's `bin` folder:
   - Example: `C:\Program Files\Apache\maven\apache-maven-3.9.5\bin`
6. Click **"OK"** on all dialogs

### Step 3: Verify Installation
1. Open a **new** Command Prompt or PowerShell window
2. Run:
   ```bash
   mvn --version
   ```
3. You should see Maven version information

### Step 4: Run the Backend
```bash
cd C:\Users\sahem\Desktop\projet\backend
mvn spring-boot:run
```

## Option 2: Use IntelliJ IDEA or Eclipse

If you have IntelliJ IDEA or Eclipse installed:

### IntelliJ IDEA:
1. Open the `backend` folder as a project
2. IntelliJ will automatically detect the `pom.xml` and download Maven
3. Right-click on `UserServiceApplication.java`
4. Select **"Run 'UserServiceApplication.main()'"**

### Eclipse:
1. Import the project: **File** → **Import** → **Maven** → **Existing Maven Projects**
2. Select the `backend` folder
3. Right-click on the project → **Run As** → **Spring Boot App**

## Option 3: Use Maven Wrapper (Alternative)

If you prefer not to install Maven globally, you can use the Maven wrapper. However, you'll need Maven installed first to generate it, or use an IDE.

## Quick Test After Installation

Once Maven is installed, test it:
```bash
cd C:\Users\sahem\Desktop\projet\backend
mvn clean install
mvn spring-boot:run
```

The backend should start on `http://localhost:8081`
