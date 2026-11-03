# Student Registration System — Java Desktop

A standalone Java Swing student registration system with separate administrator, lecturer, and student workspaces. It stores records in a local MySQL database. This is a separate project from the browser-based student portal.

For the system analysis, database schema and ERD, class diagram, architecture, and SDLC overview, see [Project Documentation](docs/PROJECT_DOCUMENTATION.md).

## Requirements

- JDK 25 (latest LTS)
- MySQL Server 8 or later, running locally
- Apache Maven
- Windows, macOS, or Linux with a graphical desktop

## Database setup

Create the `jarateng_portal` database in MySQL Workbench while connected as a MySQL administrator:

```sql
CREATE DATABASE IF NOT EXISTS jarateng_portal
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

```

The app defaults to the MySQL username `root` and prompts for its password when it starts. For better security, it is preferable to use a dedicated MySQL account with privileges only on `jarateng_portal`; set its username in the `MYSQL_USER` environment variable rather than using `root`.

## Run on Windows

Maven is installed in `C:\Users\PKhoza\tools\apache-maven-3.10.0` on this computer. For another Windows computer, download the **Binary zip archive** from the [official Apache Maven downloads](https://maven.apache.org/download.cgi), extract it, and add its `bin` folder to `PATH`.

Open PowerShell in the project folder and run:

```powershell
mvn compile exec:java
```

The app prompts for the MySQL password on launch. It is not stored in the source code. You can optionally configure `MYSQL_USER` or `MYSQL_URL` as environment variables if your account name, server, or port differ from the defaults (`root` and `localhost:3306`).

For a presentation without a MySQL connection, start the in-memory sample-data demo:

```powershell
mvn compile exec:java "-Dexec.args=--demo"
```

Demo mode opens the login screen directly and supports all three demo accounts. Changes stay in memory for that run only; the app marks this clearly in the login and workspace screens. Use the normal command above when you need MySQL persistence.

On the first launch, the app creates its tables—including `students`, `lecturers`, `administrators`, `timetable_entries`, and `announcements`—and imports existing demo CSV records from `.jarateng-portal` in your user home directory. If there are no existing records, it loads the fictional sample data. Later changes are stored in MySQL. The database and MySQL account in the SQL above must exist first; the app creates tables inside that database.

To test the configured MySQL connection without opening the portal, run:

```powershell
mvn compile exec:java "-Dexec.mainClass=edu.jarateng.portal.DatabaseConnection"
```

The test uses the same `MYSQL_URL` and `MYSQL_USER` settings as the portal and prompts for the MySQL password unless `MYSQL_PASSWORD` is set. This project connects to `jarateng_portal`; the configured MySQL user must have permissions on that database.

The app creates a demo administrator account (`admin.demo`) on first launch if the `administrators` table is empty. Its password is stored as a salted PBKDF2 hash. In the normal MySQL-backed app, an administrator can select a student on the **Students** page and choose **Create / reset login**. The student's student number is their username. Student accounts start with the shared temporary password `Welcome@1234`; they must choose a private password of at least 12 characters on first sign-in. Students can change their password later from **My profile**. Only password hashes are stored in MySQL. The shared temporary password is for this local demonstration only; do not use it for real student accounts or production.

## Demo sign-in

These built-in credentials are for the session-only demo mode, not the normal MySQL-backed app:

| Workspace | Username | Password |
| --- | --- | --- |
| Student | `student.demo` | `Student2026!` |
| Lecturer | `lecturer.demo` | `Lecturer2026!` |
| Administrator | `admin.demo` | `Admin2026!` |

## Workspaces

- **Administrator:** registration overview, student directory, create/edit/remove student records, add courses and modules, assign shared modules to courses, assign one lecturer to each module, manage the faculty directory, and publish campus announcements with optional deadlines.
- **Lecturer:** assigned modules, students in those modules, four assessment marks per module, and campus announcements.
- **Student:** a study dashboard with recorded-mark progress, assessment averages and trends, suggested next steps, a personal weekly timetable with overlap checking, campus announcements and deadline reminders, profile, modules, and results.

The starter curriculum includes a three-year Fashion Design course with modules in illustration, textiles, pattern drafting, garment construction, fashion history, digital design, merchandising, and sustainability. New or uninitialized databases receive the starter curriculum; existing curricula can be managed from the Administrator workspace.

Student, lecturer, and administrator records, course/module setup, marks, timetable entries, and announcements are stored in the `jarateng_portal` MySQL database. The student timetable is a personal weekly planner, not an official university timetable. The portal rejects overlapping entries for the same student and day; it allows adjacent classes. Assessment insights and reminders are based only on marks, registration status, and optional announcement deadlines entered in the portal. A course can include multiple modules, and a module can be shared by multiple courses. Each module has at most one lecturer; a lecturer may teach multiple modules. Existing databases are migrated from the former many-to-many lecturer allocation table; if a module had multiple lecturers, the lowest lecturer ID is retained as its single assignment. Existing CSV files are kept as-is after their one-time import. The lecturer demo sign-in remains a built-in account; adding a directory entry does not create login credentials. Deletions are permanent for this local demo; do not use real student information.

## Limitations

This is a local demonstration application, not a production student information system. The student and lecturer demo credentials are built into the application, and the demo administrator account is provisioned automatically. Back up your MySQL database separately; do not use real student information.
