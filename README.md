# WordSprint — Java Backend

Guess the Word is a word-guessing game implemented as part of the WordSprint project. WordSprint is a web-based word guessing application built with **Java 17**, **Java Servlet API 4.0.1 (`javax.servlet`)**, **Apache Tomcat 9**, **JDBC**, and a **PostgreSQL** relational database. Players guess a secret 5-letter word within a configurable number of attempts (default: 5) and receive letter-by-letter visual feedback (Green = Correct position, Yellow = Present in word, Gray = Absent).

---

## 📋 Features

### Player Features
- **User Authentication**: Secure registration and login with salted BCrypt password hashing (`$2b$12$`) and HTTP session management.
- **Word Evaluation Engine**: 2-pass feedback computation (Green / Yellow / Gray) for 5-letter uppercase guesses.
- **Daily Games Remaining Tracker**: Real-time dashboard tracker showing remaining games allowed for the day (`X / Y Daily Games Left`).
- **Date & Status Match Filtering**: Interactive filtering in player dashboard to filter active & historical games by specific date and status (`ALL`, `WON`, `LOST`, `IN_PROGRESS`).
- **Resumable Game Sessions**: Seamlessly resume in-progress games with dynamic attempt ceiling adjustment and exceeded limit detection.
- **Interactive Gameplay UI**: Floating rules & color-code helper modal positioned directly on the game canvas.
- **Player Profile & History**: Summary statistics (total games played, wins, losses, win rate) and complete historical game logs.

### Admin Features
- **Control Panel**: Administrative dashboard restricted to users with the `admin` role.
- **Dynamic Configuration Tuning**: Real-time adjustment of `maxAttempts` (default: 5, range: 1–20), `maxDailyGames` (default: 3, range: 1–50), and global `gameEnabled` toggle without server restarts.
- **Advanced Report Filtering**: Multi-dimensional report filtering by date, match status (`ALL`, `WON`, `LOST`, `IN_PROGRESS`), and live player username search across Match Activity, Daily System Activity, User Daily Activity, and Player Directory tables.
- **Granular Match Auditing**: Detailed match-level logs with secret target word, guesses sequence, timestamps, and outcome.

---

## 🛠️ Technology Stack

- **Backend**: Java 17, Java Servlet API 4.0.1 (`javax.servlet`), Apache Tomcat 9, JDBC
- **Database**: PostgreSQL 16
- **Frontend**: HTML5, Vanilla CSS3, JavaScript (ES6+ Fetch API)
- **Security**: jBCrypt (`$2b$12$`), Role-Based Access Control (RBAC), Parameterized SQL PreparedStatements
- **Build & Testing**: Maven, JUnit Jupiter 5 (21 Test Scenarios, 100% Pass Rate)

---

## 📂 Project Structure

```text
WordSprint/
├── README.md                   # Primary project documentation
├── TEST_REPORT.md              # Automated test execution report (21 tests, 100% pass)
├── database/
│   ├── create_tables.sql       # PostgreSQL DDL schema definition
│   ├── seed_data.sql           # Initial words & default admin account
│   └── seed_words.sql          # Additional word bank seeds
├── docs/
│   └── database-design.md      # Database entity column reference
├── src/
│   ├── main/java/com/wordsprint/
│   │   ├── dao/                # Data Access Objects (UserDAO, GameDAO, WordDAO, AdminDAO, StatsDAO)
│   │   ├── filter/             # Auth & RBAC Security Filters
│   │   ├── model/              # Domain Models (User, Game, Guess, Word, GameConfig)
│   │   ├── service/            # Core Business Services (AuthService, GameService, AdminService)
│   │   └── servlet/            # HttpServlet Endpoints (Login, Register, Logout, Profile, Game, Guess, Admin)
│   └── test/java/com/wordsprint/ # JUnit 5 Test Suite (21 Test Scenarios)
└── pom.xml                     # Maven build configuration
```

---

## 🔌 API Endpoint Specifications (Tomcat Base URL: `http://localhost:8080/wordsprint`)

| Endpoint Path | HTTP Method | Auth Level | Purpose / Description |
| :--- | :---: | :---: | :--- |
| `/register` | `POST` | Public | Register new player account (`role = 'player'`). |
| `/login` | `POST` | Public | Authenticate user credentials against BCrypt hash. |
| `/logout` | `POST` | Authenticated | Invalidate active HTTP session. |
| `/profile` | `GET` | Authenticated | Verify session status, retrieve user stats & game history array. |
| `/profile/update` | `POST` | Authenticated | Update player username or password. |
| `/game?action=start` | `POST` | Authenticated | Create a new game session (assigns target word). |
| `/game?action=details&gameId=1` | `GET` | Authenticated | Get details and guesses for a specific game (e.g. `gameId=2`). |
| `/guess` | `POST` | Authenticated | Submit 5-letter guess (`{"gameId": 1, "guess": "APPLE"}`). |
| `/admin/config` | `GET` / `POST` | Admin Only | Get or update dynamic system settings (`maxAttempts`, `maxDailyGames`, `gameEnabled`). |
| `/admin/reports` | `GET` | Admin Only | Generate platform analytics & player directory reports. |

---

## 💾 Database Schema Overview

The database uses PostgreSQL 16 with five core tables:
- **`users`**: Account credentials, salted BCrypt hashes, and security roles (`player`, `admin`).
- **`words`**: 5-letter secret word bank dictionary.
- **`games`**: Game sessions tracking user, assigned target word, timestamps, and status (`IN_PROGRESS`, `WON`, `LOST`).
- **`guesses`**: Recorded guesses per game with feedback result strings (`GGYBB`) and attempt sequence numbers.
- **`game_config`**: Dynamic configuration singleton (`max_attempts` default: 5, `max_daily_games` default: 3, `game_enabled`).

Detailed column reference is available in [`docs/database-design.md`](docs/database-design.md).

---

## 🧪 Testing Summary

Automated testing was executed via JUnit 5 against a live PostgreSQL database:
- **Total Tests**: **21**
- **Passed**: **21 (100.0% Pass Rate)**
- **Failed / Skipped**: **0 / 0**
- **Defect Resolution (`DEF-JAVA-001`)**: Updated `chk_guesses_number` constraint from `1..6` to `1..20` in PostgreSQL and DDL script to support dynamic attempt scaling up to 20 attempts.

Complete test execution log is available in [`TEST_REPORT.md`](TEST_REPORT.md).

---

## 🚀 Setup & Execution

### 1. Database Setup
```bash
psql -U postgres -d wordsprint -f database/create_tables.sql
psql -U postgres -d wordsprint -f database/seed_data.sql
```

### 2. Build & Deploy
```bash
mvn clean package
```
Deploy the resulting WAR file to Apache Tomcat.

### 3. Run Automated Tests
```bash
mvn test
```
