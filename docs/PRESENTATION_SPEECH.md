# Student Registration System — Presentation Speech

*Suggested presentation length: 7–9 minutes. Text in square brackets is a
presentation cue; it is not meant to be read aloud.*

## 1. Welcome and introduction

Good [morning/afternoon]. My name is [your name], and today I am presenting my
Student Registration System.

This is a Java desktop application for managing student and academic
information. It brings several everyday tasks—such as maintaining student
records, organizing courses and modules, recording marks, and checking a
student's timetable—into one role-based system.

I will first explain the purpose of the project, then introduce the system's
users and main features. After that, I will demonstrate the workspaces, briefly
explain how the application is built and stores information, and finish with
its limitations and possible future improvements.

## 2. Problem and purpose

Student information can become difficult to manage when records and academic
activities are spread across different places. This project demonstrates how
those activities can be organized in one application, with access and
information tailored to the user's role.

The main purpose is to make common registration and academic-record tasks
straightforward: administrators maintain the academic setup and student
records, lecturers work with their assigned students and marks, and students
view their own academic information.

## 3. Users and objectives

The system has three workspaces:

- The **administrator** manages student records, courses, modules, lecturers,
  and announcements.
- The **lecturer** views students connected to assigned modules and records
  their assessment marks.
- The **student** views their modules, results, timetable, announcements, and
  profile.

The project objectives are to provide clear role-based access, keep related
academic information together, and make the common tasks available through a
graphical interface.

## 4. Technology and overall design

The application is written in Java and uses Swing to build its desktop
interface. It uses MySQL to store information between runs, and JDBC to connect
the Java application to the database. Maven is used to compile and run the
project.

The interface presents different pages depending on the signed-in role. The
application code handles the interface and user actions, while the data layer
loads and saves records through the database connection.

## 5. Demonstration

[Open the application and show the sign-in screen.]

The sign-in screen is the entry point. After a successful sign-in, the user
enters the workspace for their role. I will start with the administrator
workspace because it shows how the academic information is set up.

### 5.1 Administrator workspace

[Sign in as the administrator and show Overview, Students, Courses, Lecturers,
and Announcements.]

The administrator overview provides a summary of the registration information.
On the Students page, an administrator can add, edit, and remove student
records, and create or reset a student's login. The student's number is used
as their username.

The Courses page is used to manage courses and modules, including which
modules belong to each course. The Lecturers page manages lecturer records and
module assignments. The Announcements page lets the administrator publish
campus notices, with an optional deadline.

This workspace is where the core information used by the other roles is
maintained.

### 5.2 Lecturer workspace

[Sign out, sign in as a lecturer, and show Overview, My students, Capture marks,
and Announcements.]

The lecturer workspace focuses on teaching activities. A lecturer can view
students in their assigned modules and capture up to four assessment marks per
module. Lecturers can also view campus announcements.

### 5.3 Student workspace

[Sign out, sign in as a student, and show Overview, My modules, Timetable,
Academic results, Announcements, and My profile.]

The student overview summarizes academic progress using the marks recorded in
the system. My modules shows the student's course modules, and Academic results
displays assessment marks, averages, and trends when enough marks are
available.

The Timetable page is a personal weekly planner. It checks for overlapping
entries on the same day while allowing classes that are next to each other.
Students can also read announcements and deadline reminders, and view their
profile. On first sign-in, a student must replace the temporary password with a
private password.

The text fields support normal keyboard copy and paste shortcuts, including
password fields.

## 6. Data storage and security

In normal use, the application connects to a MySQL database. It creates the
tables it needs when the database has been prepared, and stores the student,
lecturer, course, module, mark, timetable, and announcement information there.
Passwords are stored as salted PBKDF2 hashes rather than plain text.

For demonstrations, the application also has a session-only demo mode that
uses sample data without requiring a MySQL connection. Changes made in demo
mode are only kept for the current run and are lost when the application
closes.

## 7. Limitations and future improvements

This project is a local demonstration application, not a production student
information system. The demo accounts and sample data are intended for
demonstration only, and real student information should not be entered. The
student timetable is a personal planner rather than an official university
timetable. The database also needs to be backed up separately.

Possible future improvements include stronger production authentication,
automated database backups, more administrative reporting, and integration
with official university systems.

## 8. Conclusion

To conclude, the Student Registration System demonstrates how administrators,
lecturers, and students can use one application for different academic tasks.
It combines student and course management, lecturer assignments, assessment
marks, timetable planning, and announcements in a role-based Java desktop
application backed by MySQL.

Thank you for listening. I am happy to answer any questions.
