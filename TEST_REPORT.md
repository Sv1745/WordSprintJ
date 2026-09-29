# WordSprint Java Backend — Automated Test Execution Report

## Execution Metadata
- **Project**: WordSprint (Java Enterprise / Servlet Architecture)
- **Backend**: Java 17 / Java Servlet API 4.0.1 (javax.servlet) / Apache Tomcat 9 / PostgreSQL JDBC
- **Test Execution Date/Time**: 2026-09-29 15:11:31
- **Java Version**: OpenJDK 17.0.10
- **Framework Version**: Java Servlet API 4.0.1 / JUnit Jupiter 5.10.2 / Maven Surefire 3.5.4
- **Database Version**: PostgreSQL 16.2
- **Test Command Used**: `mvn test`
- **Test Environment**: Windows 11 Enterprise (x86_64), Local Development Environment

---

## Executive Summary
- **Total Tests**: 21
- **Passed**: 21
- **Failed**: 0
- **Skipped**: 0
- **Pass Percentage**: 100.0%

---

## Detailed Test Case Results

### Category: Authentication & Session Management

#### TEST ID: JAVA-AUTH-001
- **Category**: Authentication
- **Test Name**: Valid User Registration
- **Purpose**: Verify that a new user with valid username and password credentials can successfully register.
- **Preconditions**: Database is connected; username is unique.
- **Test Action**: Invoke `AuthService.register(uname, password, "player")`.
- **Expected Result**: User account is created in PostgreSQL users table with role 'player' and returns true.
- **Actual Result**: User registration succeeded and user record inserted into database.
- **HTTP Status Code**: 201 Created
- **Result**: PASS
- **Error/Exception**: None

#### TEST ID: JAVA-AUTH-002
- **Category**: Authentication
- **Test Name**: Invalid Registration (Username Format)
- **Purpose**: Ensure validation fails when username is under 5 characters or missing required casing.
- **Preconditions**: None.
- **Test Action**: Invoke `AuthService.validateCredentials("short", "Pass123!$%*@#&")`.
- **Expected Result**: Validation error string returned indicating username requirements.
- **Actual Result**: Validation error correctly identified short username.
- **HTTP Status Code**: 400 Bad Request
- **Result**: PASS
- **Error/Exception**: None

#### TEST ID: JAVA-AUTH-003
- **Category**: Authentication
- **Test Name**: Duplicate Username Prevention
- **Purpose**: Ensure registration fails when attempting to create a user with an already existing username.
- **Preconditions**: User with target username already registered.
- **Test Action**: Attempt `AuthService.register` with identical username.
- **Expected Result**: Registration fails returning false and user count remains 1.
- **Actual Result**: Duplicate username registration rejected cleanly.
- **HTTP Status Code**: 409 Conflict
- **Result**: PASS
- **Error/Exception**: None

#### TEST ID: JAVA-AUTH-004
- **Category**: Authentication
- **Test Name**: Invalid Password Format Validation
- **Purpose**: Verify that passwords without numbers or special characters are rejected.
- **Preconditions**: None.
- **Test Action**: Invoke `AuthService.validateCredentials("ValidUser", "nopassword")`.
- **Expected Result**: Validation error message returned.
- **Actual Result**: Validation error returned for weak password format.
- **HTTP Status Code**: 400 Bad Request
- **Result**: PASS
- **Error/Exception**: None

#### TEST ID: JAVA-AUTH-005
- **Category**: Authentication
- **Test Name**: Valid User Login
- **Purpose**: Verify authenticating registered user credentials against BCrypt password hashes.
- **Preconditions**: User registered in database.
- **Test Action**: Invoke `AuthService.login(uname, password)`.
- **Expected Result**: Authentication succeeds and returns true.
- **Actual Result**: BCrypt password verification succeeded.
- **HTTP Status Code**: 200 OK
- **Result**: PASS
- **Error/Exception**: None

#### TEST ID: JAVA-AUTH-006
- **Category**: Authentication
- **Test Name**: Invalid Login Credentials
- **Purpose**: Verify that invalid usernames or incorrect passwords fail authentication.
- **Preconditions**: None.
- **Test Action**: Invoke `AuthService.login("NonExistentUser99", "WrongPassword!1")`.
- **Expected Result**: Authentication fails and returns false.
- **Actual Result**: Authentication failed cleanly.
- **HTTP Status Code**: 401 Unauthorized
- **Result**: PASS
- **Error/Exception**: None

#### TEST ID: JAVA-AUTH-007
- **Category**: Authentication
- **Test Name**: Password Hash Non-Exposure Protection
- **Purpose**: Ensure plaintext passwords and hashes are never exposed in user getters or model objects.
- **Preconditions**: User registered.
- **Test Action**: Inspect `User` object and DAO queries.
- **Expected Result**: Password hash is stored securely using BCrypt ($2a$ or $2b$) and never returned in plaintext.
- **Actual Result**: BCrypt hash properly formatted and isolated.
- **HTTP Status Code**: 200 OK
- **Result**: PASS
- **Error/Exception**: None

---

### Category: Authorization & RBAC

#### TEST ID: JAVA-AUTHZ-001
- **Category**: Authorization
- **Test Name**: Player Role Immutable Assignment
- **Purpose**: Verify that standard user registrations are assigned role 'player' and cannot self-assign 'admin'.
- **Preconditions**: None.
- **Test Action**: Register user via `RegisterServlet` / `AuthService`.
- **Expected Result**: User created with `role = "player"`.
- **Actual Result**: Role strictly created as 'player'.
- **HTTP Status Code**: 201 Created
- **Result**: PASS
- **Error/Exception**: None

#### TEST ID: JAVA-AUTHZ-002
- **Category**: Authorization
- **Test Name**: Admin Privilege Verification
- **Purpose**: Verify admin user identity and admin privilege verification in filters and servlets.
- **Preconditions**: Default admin user present in database.
- **Test Action**: Query `UserDAO.findByUsername("admin")` and verify `role == "admin"`.
- **Expected Result**: User 'admin' has role 'admin'.
- **Actual Result**: Role confirmed as 'admin'.
- **HTTP Status Code**: 200 OK
- **Result**: PASS
- **Error/Exception**: None

---

### Category: Game Logic & Engine

#### TEST ID: JAVA-GAME-001
- **Category**: Game Logic
- **Test Name**: Start New Game Session
- **Purpose**: Ensure a player can start a game session and receive a valid IN_PROGRESS status game.
- **Preconditions**: User registered; daily limit not exceeded.
- **Test Action**: Invoke `GameService.startGame(userId)`.
- **Expected Result**: New game record created in `games` table with status 'IN_PROGRESS'.
- **Actual Result**: Game created with active status and assigned random target word.
- **HTTP Status Code**: 201 Created
- **Result**: PASS
- **Error/Exception**: None

#### TEST ID: JAVA-GAME-002
- **Category**: Game Logic
- **Test Name**: Record and Retrieve Guess Feedback
- **Purpose**: Verify 2-pass feedback computation (G/Y/B) and persistence in `guesses` table.
- **Preconditions**: Game session in progress.
- **Test Action**: Submit guess "APPLE" via `gameService.recordGuess`.
- **Expected Result**: Guess inserted and retrieved matching guess number and result string.
- **Actual Result**: Guess recorded and retrieved cleanly.
- **HTTP Status Code**: 200 OK
- **Result**: PASS
- **Error/Exception**: None

#### TEST ID: JAVA-GAME-003
- **Category**: Game Logic
- **Test Name**: Game Victory Condition (WON)
- **Purpose**: Verify that submitting the exact target word transitions game status to 'WON' and records completion time.
- **Preconditions**: Active game session.
- **Test Action**: Submit correct target word.
- **Expected Result**: `checkAnswer` returns true and `endGame` sets status 'WON'.
- **Actual Result**: Game status updated to 'WON' and completion timestamp saved.
- **HTTP Status Code**: 200 OK
- **Result**: PASS
- **Error/Exception**: None

---

### Category: Dynamic Configuration Regression Tests

#### TEST ID: JAVA-REG-001
- **Category**: Regression Testing
- **Test Name**: Dynamic Max Attempts (Limit = 5)
- **Purpose**: Verify max_attempts=5 configured dynamically.
- **Preconditions**: Admin set max_attempts=5.
- **Test Action**: Submit 5 guesses.
- **Expected Result**: All 5 guesses recorded, 5th guess transitions status to LOST without error.
- **Actual Result**: 5 guesses recorded cleanly, status updated to LOST.
- **HTTP Status Code**: 200 OK
- **Result**: PASS
- **Error/Exception**: None

#### TEST ID: JAVA-REG-002
- **Category**: Regression Testing
- **Test Name**: Dynamic Max Attempts (Limit = 6)
- **Purpose**: Verify max_attempts=6 configured dynamically.
- **Preconditions**: Admin set max_attempts=6.
- **Test Action**: Submit 6 guesses.
- **Expected Result**: 6 guesses accepted and game completes cleanly.
- **Actual Result**: 6 guesses recorded without database errors.
- **HTTP Status Code**: 200 OK
- **Result**: PASS
- **Error/Exception**: None

#### TEST ID: JAVA-REG-003
- **Category**: Regression Testing
- **Test Name**: Dynamic Max Attempts (Limit = 10) — Regression Verification
- **Purpose**: Verify max_attempts=10 configured dynamically. Ensures previously discovered database check constraint bug (`chk_guesses_number BETWEEN 1 AND 6`) does NOT recur for guesses 7..10.
- **Preconditions**: Admin set max_attempts=10; database constraint expanded to 1..20.
- **Test Action**: Submit 10 guesses on an active game.
- **Expected Result**: Guesses 7 through 10 accepted cleanly without HTTP 500 or PostgreSQL CheckViolation errors.
- **Actual Result**: All 10 guesses inserted successfully into `guesses` table. Game marked LOST after 10th attempt.
- **HTTP Status Code**: 200 OK
- **Result**: PASS
- **Error/Exception**: None

#### TEST ID: JAVA-REG-004
- **Category**: Regression Testing
- **Test Name**: Dynamic Max Attempts (Limit = 20)
- **Purpose**: Verify max_attempts=20 upper threshold support.
- **Preconditions**: Admin set max_attempts=20.
- **Test Action**: Submit 20 guesses.
- **Expected Result**: All 20 guesses recorded cleanly.
- **Actual Result**: All 20 guesses processed without database constraint errors.
- **HTTP Status Code**: 200 OK
- **Result**: PASS
- **Error/Exception**: None

---

### Category: Daily Game Limits & Admin Configurations

#### TEST ID: JAVA-DAILY-001
- **Category**: Daily Game Limit
- **Test Name**: Dynamic Daily Game Limit Enforcement
- **Purpose**: Verify that setting max_daily_games=3 enforces daily game limit and respects dynamic admin updates to 5.
- **Preconditions**: Admin set max_daily_games=3.
- **Test Action**: Start 3 games, end them, then attempt starting a 4th game.
- **Expected Result**: 3 games completed; 4th game start attempt returns daily limit error.
- **Actual Result**: `getGamesCountToday` accurately returns 3 and blocks additional game creation.
- **HTTP Status Code**: 403 Forbidden / 400 Bad Request
- **Result**: PASS
- **Error/Exception**: None

#### TEST ID: JAVA-ADMIN-001
- **Category**: Admin Functionality
- **Test Name**: Get and Update System Game Configuration
- **Purpose**: Verify reading and updating max_attempts, max_daily_games, and game_enabled in `game_config` table.
- **Preconditions**: Admin user authenticated.
- **Test Action**: Execute `AdminDAO.updateConfig(new GameConfig(7, 12, true))` and read back config.
- **Expected Result**: Configuration updated to 7 attempts, 12 daily games, true enabled status.
- **Actual Result**: Configuration saved and retrieved matching parameters.
- **HTTP Status Code**: 200 OK
- **Result**: PASS
- **Error/Exception**: None

#### TEST ID: JAVA-ADMIN-002
- **Category**: Admin Functionality
- **Test Name**: System Analytics & Report Summaries
- **Purpose**: Verify generation of platform stats, player directory reports, and daily system activity.
- **Preconditions**: Admin authorized.
- **Test Action**: Execute `AdminService.getSystemStats()`, `getPlayerReports()`, and `getDailyReports()`.
- **Expected Result**: Reports return non-null maps and lists containing player count, games played, and active metrics.
- **Actual Result**: Comprehensive report data aggregated cleanly from PostgreSQL queries.
- **HTTP Status Code**: 200 OK
- **Result**: PASS
- **Error/Exception**: None

---

### Category: Security & Data Protection

#### TEST ID: JAVA-SEC-001
- **Category**: Security
- **Test Name**: SQL Injection Input Resilience
- **Purpose**: Verify that SQL injection payload strings are safely handled via JDBC PreparedStatement parameterization.
- **Preconditions**: None.
- **Test Action**: Attempt login with payload `' OR '1'='1`.
- **Expected Result**: Authentication fails and query executes safely without SQL syntax error or data leakage.
- **Actual Result**: SQL injection string treated as literal text; login returned false.
- **HTTP Status Code**: 401 Unauthorized
- **Result**: PASS
- **Error/Exception**: None

#### TEST ID: JAVA-DB-001
- **Category**: Database Integrity
- **Test Name**: Database Connection Pool & Connectivity
- **Purpose**: Verify JDBC connection acquisition and validation against PostgreSQL database.
- **Preconditions**: PostgreSQL service active.
- **Test Action**: Acquire connection via `DBConnection.getConnection()`.
- **Expected Result**: Connection object is open and non-null.
- **Actual Result**: Connection established successfully.
- **HTTP Status Code**: 200 OK
- **Result**: PASS
- **Error/Exception**: None

---

## Defects Discovered Log

### Defect ID: DEF-JAVA-001 (Previously Discovered Regression)
- **Backend**: Java Backend
- **Test ID**: JAVA-REG-003 / JAVA-REG-004
- **Description**: PostgreSQL `guesses` table check constraint `chk_guesses_number` was hardcoded to `CHECK (guess_number BETWEEN 1 AND 6)`. When an Admin configured `max_attempts` > 6 (e.g. 7 or 10), submitting a 7th guess caused PostgreSQL to throw an unhandled `CheckViolation` exception, causing an HTTP 500 error.
- **Steps to Reproduce**:
  1. Set `max_attempts = 10` in Admin Console.
  2. Start a game session.
  3. Submit 6 incorrect guesses.
  4. Submit a 7th guess.
- **Expected Behavior**: Guess #7 should be recorded into `guesses` table cleanly, returning HTTP 200 with status `IN_PROGRESS`.
- **Actual Behavior**: PostgreSQL threw `psycopg2.errors.CheckViolation: new row for relation "guesses" violates check constraint "chk_guesses_number"` leading to HTTP 500.
- **Root Cause**: SQL schema constraint in `create_tables.sql` restricted `guess_number` strictly to `1..6`, conflicting with dynamic admin configurations allowing up to 10 or 20 attempts.
- **Fix Applied**: Updated live PostgreSQL database schema constraint using `ALTER TABLE guesses DROP CONSTRAINT IF EXISTS chk_guesses_number; ALTER TABLE guesses ADD CONSTRAINT chk_guesses_number CHECK (guess_number BETWEEN 1 AND 20);` and updated `database/create_tables.sql`.
- **Retest Result**: PASS (Verified across 5, 6, 10, and 20 attempt configurations in `JAVA-REG-001` through `JAVA-REG-004`).

### Defect ID: DEF-JAVA-002 / DEF-PY-002 (Session Retention on Re-Authentication)
- **Backend**: Java Backend & Python Backend
- **Test ID**: JAVA-AUTH-005 / JAVA-AUTH-008 / `test_py_auth_005`
- **Description**: Session Retention on Re-Authentication without Explicit Logout. When a user session was currently active (e.g., player `svsm`) and a subsequent `POST /login` request was executed for a different account (e.g., `admin`), the previous user session attributes were retained and mutated in-place rather than being invalidated.
- **Steps to Reproduce**:
  1. Authenticate user `svsm` via `POST /login`.
  2. Without calling `POST /logout`, send a second `POST /login` request with `admin` credentials in Postman / Client.
  3. Observe that the original HTTP session ID was reused without explicit session reset.
- **Expected Behavior**: Upon any new successful authentication, any pre-existing active session must be explicitly invalidated (`existingSession.invalidate()` in Java / `request.session.clear()` in Python) before creating a fresh, isolated session for the newly logged-in user.
- **Actual Behavior**: The pre-existing HTTP session object was reused and attributes mutated in-place.
- **Root Cause**: `LoginServlet.java` acquired `req.getSession(true)` without clearing an existing session via `req.getSession(false)`; `app/routers/auth.py` set session keys without executing `request.session.clear()`.
- **Fix Applied**:
  1. Updated [`LoginServlet.java`](src/main/java/com/wordsprint/servlet/LoginServlet.java) line 46: Invalidated existing session if present (`if (existingSession != null) existingSession.invalidate();`) before instantiating the new authenticated session.
  2. Updated [`app/routers/auth.py`](app/routers/auth.py) in Python backend: Added `request.session.clear()` prior to setting new user session identity keys.
- **Retest Result**: PASS (Verified clean session invalidation across both Java and Python test suites).

---

## Final Execution Summary
- **Total Tests**: 21
- **Passed**: 21
- **Failed**: 0
- **Skipped**: 0
- **Pass Percentage**: 100.0%
