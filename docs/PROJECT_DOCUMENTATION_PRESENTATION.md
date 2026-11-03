# Project Documentation — Presentation Speech

*Suggested presentation length: 8–10 minutes. Text in square brackets is a
presentation cue; it is not meant to be read aloud.*

## 1. Introduction

Good [morning/afternoon]. Today I am going to explain the project documentation
for my Student Registration System. I will follow the document in order, from
the system's purpose and requirements through its design, database, development
plan, limitations, and instructions for running the application.

[Open `docs/PROJECT_DOCUMENTATION.md` and show its table of contents.]

The document describes the system as it is currently implemented. It is
intended for project analysis and learning, rather than as a specification for
a production student information system. I will explain what each part tells
us and how the parts connect.

## 2. Purpose and scope

[Show section 1, “Purpose and scope.”]

The purpose of the system is to bring student registration and related
academic activities into one Java desktop application. It covers student
records, academic programmes, lecturers, modules, assessment marks, personal
timetable plans, and campus announcements.

The scope is deliberately limited to a local demonstration. The documentation
also makes clear that authentication, data governance, backups, and operating
controls are not yet sufficient for production use. Stating this boundary
helps set realistic expectations about what the project does and what it does
not claim to do.

## 3. Project objectives

[Show section 2, “Project objectives.”]

The objectives describe what the project is intended to provide. These include
a central student register, administrator management of student and lecturer
records, student progress and timetable views, lecturer access to assigned
students and marks, and announcements with optional deadlines.

Another objective is to save application records in MySQL and provide a way to
import existing student and lecturer CSV data when initializing an empty
database. These objectives give us a simple way to understand the purpose of
the features shown in the application.

## 4. Stakeholders and user roles

[Show section 3 and its stakeholder table.]

The documentation identifies four stakeholder types. Administrators maintain
student and academic records. Lecturers see their assigned modules and students
and record assessment marks. Students view their own progress, modules,
results, timetable, profile, and announcements. The system operator prepares
Java, Maven, and MySQL and looks after the database configuration and backups.

This section also explains how demonstration accounts work. For example, a
student account in the normal MySQL-backed mode is provisioned by an
administrator, starts with a temporary password, and must change it at first
sign-in. Passwords are stored as salted PBKDF2 hashes. The document emphasizes
that these are demonstration arrangements and not a production identity
system.

## 5. System analysis

[Show section 4, “System analysis.”]

The system analysis explains what users can do and the rules the application
should follow.

First, the functional requirements use IDs such as FR-01 and FR-02 to identify
specific capabilities. They cover the overview, student registration and
maintenance, course and module setup, lecturer assignments, mark entry,
timetable planning, announcements, and student progress insights. The IDs
make requirements easier to refer to when discussing implementation and
testing.

Next, the quality attributes describe important characteristics and
constraints. Examples include a usable role-based interface, persistent MySQL
storage, data integrity checks, the Java and Maven setup, and security and
operational limitations.

Finally, the use-case flows explain tasks step by step. For example, an
administrator registers a student by entering their details and programme;
the application validates the information, assigns a student number, and
saves the record. Other flows describe curriculum management, capturing
marks, viewing results, adding a timetable entry, and publishing an
announcement.

Together, these requirements and flows connect the project goals to actions
that can be demonstrated and checked.

## 6. System design and architecture

[Show section 5, “System design and architecture,” and its component diagram.]

The application is a Java desktop program with a Swing interface. Its main
components have distinct responsibilities: `PortalApp` presents the interface
and workspaces, `PortalData` holds application data, `DatabaseStore` handles
MySQL persistence through JDBC, and `CsvStore` supports legacy CSV import.

The technology table records the tools and versions used, including the Java
compiler release, Maven, MySQL Server, and MySQL Connector/J. The data-flow
section then describes startup: the application reads its database settings,
initializes the schema, loads records, and presents the workspace for the
signed-in role. Changes are saved through the data and persistence layers.

The configuration section lists `MYSQL_URL`, `MYSQL_USER`, and
`MYSQL_PASSWORD`. It explains that the MySQL database must already exist,
even though the application can create its tables. It also documents
session-only demo mode, which uses fictional sample data and does not keep
changes after the application closes.

## 7. Database design

[Show section 6 and point to the entity relationship diagram.]

The database design explains how the system's information is organized in
MySQL. The diagram shows the main tables and their relationships: students
belong to courses; courses and modules are linked through a join table; marks
connect students and modules; and timetable entries connect a student with a
scheduled module.

The data dictionary describes each table's purpose, important columns, and
constraints. Primary keys identify records, foreign keys connect related
records, and unique keys prevent duplicate values such as student numbers.
For example, `assessment_marks` identifies a student's mark for a particular
module and assessment, with checks for the permitted assessment number and
score range.

The relationship rules add meaning to the diagram. A course can share modules
with other courses, while each module can have at most one lecturer. Timetable
entries are personal planning information, not an official institutional
schedule. These rules help explain how the application avoids inconsistent
relationships.

## 8. Class diagram and design

[Show section 7 and point to the class diagram.]

The class diagram describes the main software components and how they relate.
`PortalApp` is responsible for the Swing interface. `PortalData` groups the
application's in-memory information, such as students, courses, modules, and
announcements. `DatabaseStore` handles persistent database access, while
`CsvStore` supports legacy file import.

The responsibility table gives a short explanation of each class and model.
This view is different from the database diagram: the database diagram
describes stored records and their relationships, while the class diagram
describes responsibilities in the Java program.

## 9. Software development life cycle

[Show section 8 and the validation checklist.]

The SDLC section describes a recommended process for developing and maintaining
the project. It covers planning, requirements analysis, design,
implementation, verification, deployment, and ongoing operation.

This is a suggested process, not a claim that every step has already been
automated. For example, the documentation recommends adding automated tests
for login, data changes, mark limits, schema initialization, and error
handling. The checklist also suggests trying each role and key workflow,
checking database persistence, and verifying timetable overlap rules.

This section is useful because it turns the design into a practical list of
activities for checking and improving the application.

## 10. Project plan

[Show section 9 and its scope, schedule, cost, and responsibilities.]

The project plan defines what is included and excluded. The included scope is
the local Java application, its role-specific workspaces, academic records,
database storage, demo mode, and verification. Production hosting, real
student data, institutional integrations, and formal compliance are outside
the current scope.

The schedule estimates a total of 17 working days across requirements,
design, implementation, and verification. The cost estimate lists the
software tools as having no planned licensing cost, while noting that labour,
equipment, and connectivity are not included. These are planning estimates,
not measured project results.

The responsibilities table identifies who could implement the project and
who could review the administrator, lecturer, student, database, and
presentation workflows. In a small classroom project, one person may cover
several of these roles.

## 11. Known limitations and future improvements

[Show section 10.]

This section identifies work that should be completed before the system is
used beyond a demonstration. The suggested improvements include replacing
built-in demo sign-ins with database-backed accounts and explicit
authorization, using a dedicated database account, adding automated tests,
completing course and module maintenance, and planning backups, audit history,
privacy controls, and database migrations.

The document also points out that the current persistence strategy is intended
for a small local demonstration and should be reviewed before multi-user use.
Presenting these limitations is important because it distinguishes existing
features from future recommendations.

## 12. Run instructions and reference

[Show section 11 and then the project README.]

The final section explains how to compile and run the application. From the
project directory, the normal command is:

```powershell
mvn compile exec:java
```

For a presentation that does not depend on MySQL, the session-only demo can be
started with:

```powershell
mvn compile exec:java "-Dexec.args=--demo"
```

The project README contains the database setup, configuration, demo-account,
and connection-test instructions. This gives a reader a starting point for
running the system after learning how it is designed.

## 13. Conclusion

To conclude, the project documentation takes the reader from the purpose and
requirements of the Student Registration System through the architecture,
database and class designs, development process, project plan, limitations,
and run instructions.

Its diagrams explain the relationships in both the application and its
database, while the requirements, use cases, and validation checklist explain
what should be implemented and checked. The document also clearly separates
the current local demonstration from the additional work needed for a
production system.

Thank you for listening. I am happy to answer any questions.
