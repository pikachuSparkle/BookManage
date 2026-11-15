# BookManage — Library Management

[简体中文](README.md) | [English](README.en.md)

A traditional Java web application built with Spring MVC 6, Spring Security 6, MyBatis, and Thymeleaf.
It supports login, adding and deleting books, student listings, borrowing, and returns.
Return history is retained in the database, and books use soft deletion.
`USER` accounts have read-only access; `ADMIN` accounts can manage books and loans.

Demo book titles and student names are in English. The application interface is currently in Chinese.

## Requirements

- JDK **17+** and Maven 3.9+
- MySQL 8 or MariaDB 10.11 (the local setup uses a standalone MariaDB 10.11 instance)
- Embedded Tomcat 10.1, or an external Tomcat 10.1 server for WAR deployment

Local tools and database files are stored in `~/.local/share/bookmanage-runtime/`; no sudo is required.
Paths are defined in `scripts/local-env.sh` and can be overridden with `BOOKMANAGE_RUNTIME`.
The local installation does not register system services or start automatically after a reboot.

On Ubuntu 24.04 amd64, install the local toolchain with:

```bash
bash scripts/install-local-runtime.sh
```

To initialize a **new** standalone database instance:

```bash
source scripts/local-env.sh
"$BOOKMANAGE_RUNTIME/local/usr/bin/mariadb-install-db" --no-defaults \
  --basedir="$BOOKMANAGE_RUNTIME/local/usr" --datadir="$BOOKMANAGE_RUNTIME/data" \
  --auth-root-authentication-method=socket --auth-root-socket-user="$(id -un)" --skip-test-db
bash scripts/start-local.sh --database-only
```

Connect using the client at `$BOOKMANAGE_RUNTIME/local/usr/bin/mariadb` with
`--socket="$BOOKMANAGE_RUNTIME/mysql.sock"`, then follow the fresh environment setup below to create the database account and application tables.

## Start and Stop an Existing Local Setup

Run these commands from the repository root after configuring the database and building the application:

```bash
bash scripts/start-local.sh
# Open http://127.0.0.1:8080/login

# Stop the application and standalone database while preserving data
bash scripts/stop-local.sh
```

The administrator username is `admin`. On the preconfigured local machine, the initial password is stored in
`.runtime/credentials.txt` (readable only by the current user and excluded from Git).
Configuration and database credentials are stored in `.env.local`; keep these files out of version control.

Logs: `.runtime/application.log` and `~/.local/share/bookmanage-runtime/mysql.log`.
The default bindings are `127.0.0.1:8080` for the application and `127.0.0.1:3307` for the local database.

## Build and Test

```bash
# When using the locally installed JDK and Maven
source scripts/local-env.sh
bash scripts/build.sh
```

The build runs integration tests and generates `target/BookManage-1.0-SNAPSHOT.war` and the local runtime classpath.
Tests use an isolated H2 in-memory database in MySQL compatibility mode; they do not connect to or clear the real database.
Coverage includes loan field mapping, history retention, monetary precision, invalid and duplicate loans,
concurrent requests, soft deletion, role permissions, CSRF, password authentication, remember-me,
page rendering, non-root context paths, and input validation.

Rebuild and restart the application after changing source code.

For HTTP smoke tests against the real database (**demo environments only**; these create books,
borrow and return them, then soft-delete them while retaining history):

```bash
ALLOW_SMOKE_WRITES=1 python3 scripts/smoke-local.py
```

Results are written to `.runtime/smoke-results.txt`. Checks include concurrent requests, Chinese form submissions,
and CSRF rejection. Use `SMOKE_BASE` and `SMOKE_PASSWORD` to override the test URL and administrator password.

## Fresh Environment Setup

With JDK, Maven, and a database server already installed:

1. Create a dedicated empty database and an initialization account:

   ```sql
   CREATE DATABASE bookmanage CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   CREATE USER 'bookmanage'@'127.0.0.1' IDENTIFIED BY 'replace-with-a-random-password';
   GRANT ALL PRIVILEGES ON bookmanage.* TO 'bookmanage'@'127.0.0.1';
   ```

2. Copy the example configuration, then set the connection URL, database password, and an administrator password of at least 12 characters:

   ```bash
   cp .env.example .env.local
   chmod 600 .env.local
   # Edit .env.local before continuing
   bash scripts/build.sh
   bash scripts/init-db.sh
   ```

   Initialization only accepts a database with **no existing application tables** and refuses to overwrite existing data.
   `schema.sql` is for fresh installations, not automatic migrations; rerunning its index definitions will fail.

3. After initialization, use a database administrator account to revoke DDL permissions. The application only needs data access permissions:

   ```sql
   REVOKE ALL PRIVILEGES ON bookmanage.* FROM 'bookmanage'@'127.0.0.1';
   GRANT SELECT, INSERT, UPDATE, DELETE ON bookmanage.* TO 'bookmanage'@'127.0.0.1';
   ```

4. Remove `ADMIN_PASSWORD` from `.env.local`, then start the application:

   ```bash
   bash scripts/run.sh
   ```

   `APP_PORT` controls the HTTP port. Set `APP_CONTEXT_PATH=/library` to use a non-root context path.
   For external WAR deployment, provide the `DB_URL`, `DB_USER`, and `DB_PASSWORD` environment variables.

## Data Model and Legacy Databases

- `book`: `DECIMAL(10,2)` prices and a `deleted` soft-delete flag.
- `student`: unified student table with string-valued gender.
- `borrow`: `sid` and `bid` foreign keys; `returned_at` is NULL for active loans.
- `user`: unique usernames, BCrypt password hashes, and `ADMIN` / `USER` roles.
- `persistent_logins`: Spring Security remember-me tokens.

**Do not directly import the legacy `study.sql` from the NOTES archive.** It contains DROP TABLE statements and uses a different schema.
Back up legacy databases and review migrations separately: reconcile `student2` / `student`, convert gender values,
adjust monetary precision, add soft-delete and return timestamps, and resolve duplicate loans, orphaned foreign keys,
and duplicate usernames before adding constraints.
The local setup uses a fresh `bookmanage` database and does not access the legacy `study` database.

## Key Improvements

- Explicit query columns and aliases prevent loan IDs from being overwritten by student IDs.
- Books and their loan status are fetched together; aggregate statistics use COUNT.
- Transactions and shared book-row locks serialize borrowing, returns, and deletion, with server-side state validation.
- CSRF protection is enabled; mutations and logout use POST, with tokens supplied by Thymeleaf forms.
- Administrative endpoints enforce the ADMIN role; maintenance controls are hidden from read-only users.
- Server-side validation, shared error pages, UTF-8 filtering, and context-path-aware links.
- Externalized database credentials, connection pooling, consistent dependency versions, and automated tests.

This is an educational application. Public deployments should add HTTPS, secure cookies, login rate limiting,
database backups, auditing, pagination, and operational monitoring rather than exposing the local demo configuration directly.

## Original Project

Original tutorial (Chinese): https://www.bilibili.com/video/BV1fV411M7aS?p=19

The repository's original [LICENSE](LICENSE) is retained.
