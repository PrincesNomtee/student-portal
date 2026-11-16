package edu.jarateng.portal;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

final class DatabaseStore {
    private static final String DEFAULT_URL =
            "jdbc:mysql://localhost:3306/jarateng_portal?connectionTimeZone=LOCAL";
    static final String DEMO_ADMIN_USERNAME = "admin.demo";
    static final String DEMO_ADMIN_PASSWORD = "Admin2026!";
    static final String DEFAULT_TEMPORARY_PASSWORD = "Welcome@1234";
    private static final int PASSWORD_ITERATIONS = 120000;
    private static final int PASSWORD_SALT_BYTES = 16;
    private static final int PASSWORD_HASH_BITS = 256;
    private final String url;
    private final String username;
    private final String password;

    DatabaseStore(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    static String configuredUrl() {
        String configured = System.getenv("MYSQL_URL");
        return configured == null || configured.trim().isEmpty() ? DEFAULT_URL : configured.trim();
    }

    static String configuredUsername() {
        String configured = System.getenv("MYSQL_USER");
        return configured == null || configured.trim().isEmpty() ? "root" : configured.trim();
    }

    private Connection connect() throws SQLException {
        Properties credentials = new Properties();
        credentials.setProperty("user", username);
        credentials.setProperty("password", password);
        return DriverManager.getConnection(url, credentials);
    }

    void initialize() throws SQLException {
        String[] statements = {
            "CREATE TABLE IF NOT EXISTS portal_metadata (metadata_key VARCHAR(64) PRIMARY KEY, metadata_value VARCHAR(255) NOT NULL)",
            "CREATE TABLE IF NOT EXISTS courses (course_code VARCHAR(24) PRIMARY KEY, course_name VARCHAR(180) NOT NULL, duration VARCHAR(40) NOT NULL)",
            "CREATE TABLE IF NOT EXISTS modules (module_code VARCHAR(24) PRIMARY KEY, module_name VARCHAR(180) NOT NULL)",
            "CREATE TABLE IF NOT EXISTS course_modules (course_code VARCHAR(24) NOT NULL, module_code VARCHAR(24) NOT NULL, PRIMARY KEY (course_code, module_code), CONSTRAINT fk_course_modules_course FOREIGN KEY (course_code) REFERENCES courses(course_code), CONSTRAINT fk_course_modules_module FOREIGN KEY (module_code) REFERENCES modules(module_code))",
            "CREATE TABLE IF NOT EXISTS students (id INT PRIMARY KEY, student_number VARCHAR(32) NOT NULL UNIQUE, first_name VARCHAR(100) NOT NULL, surname VARCHAR(100) NOT NULL, email VARCHAR(254) NOT NULL UNIQUE, course_code VARCHAR(24) NOT NULL, status VARCHAR(24) NOT NULL, enrollment_date DATE NOT NULL, CONSTRAINT fk_students_course FOREIGN KEY (course_code) REFERENCES courses(course_code))",
            "CREATE TABLE IF NOT EXISTS lecturers (id INT PRIMARY KEY, first_name VARCHAR(100) NOT NULL, surname VARCHAR(100) NOT NULL, email VARCHAR(254) NOT NULL UNIQUE)",
            "CREATE TABLE IF NOT EXISTS module_lecturers (module_code VARCHAR(24) NOT NULL, lecturer_id INT NOT NULL, PRIMARY KEY (module_code, lecturer_id), KEY idx_module_lecturers_lecturer (lecturer_id), CONSTRAINT fk_module_lecturers_module FOREIGN KEY (module_code) REFERENCES modules(module_code) ON DELETE CASCADE, CONSTRAINT fk_module_lecturers_lecturer FOREIGN KEY (lecturer_id) REFERENCES lecturers(id) ON DELETE CASCADE)",
            "CREATE TABLE IF NOT EXISTS administrators (id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, username VARCHAR(64) NOT NULL UNIQUE, first_name VARCHAR(100) NOT NULL, surname VARCHAR(100) NOT NULL, password_hash VARCHAR(255) NOT NULL, created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP)",
            "CREATE TABLE IF NOT EXISTS student_accounts (student_id INT PRIMARY KEY, password_hash VARCHAR(255) NOT NULL, must_change_password BOOLEAN NOT NULL DEFAULT TRUE, updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, CONSTRAINT fk_student_accounts_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE)",
            "CREATE TABLE IF NOT EXISTS lecturer_accounts (lecturer_id INT PRIMARY KEY, password_hash VARCHAR(255) NOT NULL, must_change_password BOOLEAN NOT NULL DEFAULT TRUE, updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, CONSTRAINT fk_lecturer_accounts_lecturer FOREIGN KEY (lecturer_id) REFERENCES lecturers(id) ON DELETE CASCADE)",
            "CREATE TABLE IF NOT EXISTS assessment_marks (student_id INT NOT NULL, module_code VARCHAR(24) NOT NULL, assessment_number TINYINT NOT NULL, score TINYINT UNSIGNED NOT NULL, PRIMARY KEY (student_id, module_code, assessment_number), CONSTRAINT chk_assessment_number CHECK (assessment_number BETWEEN 1 AND 4), CONSTRAINT chk_assessment_score CHECK (score BETWEEN 0 AND 100), CONSTRAINT fk_marks_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE, CONSTRAINT fk_marks_module FOREIGN KEY (module_code) REFERENCES modules(module_code))",
            "CREATE TABLE IF NOT EXISTS timetable_entries (id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, student_id INT NOT NULL, module_code VARCHAR(24) NOT NULL, day_of_week TINYINT NOT NULL, start_time TIME NOT NULL, end_time TIME NOT NULL, location VARCHAR(120) NOT NULL, CONSTRAINT chk_timetable_day CHECK (day_of_week BETWEEN 1 AND 7), CONSTRAINT chk_timetable_time CHECK (end_time > start_time), CONSTRAINT fk_timetable_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE, CONSTRAINT fk_timetable_module FOREIGN KEY (module_code) REFERENCES modules(module_code) ON DELETE CASCADE, UNIQUE KEY uq_timetable_slot (student_id, day_of_week, start_time))",
            "CREATE TABLE IF NOT EXISTS announcements (id INT PRIMARY KEY, title VARCHAR(160) NOT NULL, message VARCHAR(1000) NOT NULL, published_on DATE NOT NULL, due_date DATE NULL)",
            "CREATE TABLE IF NOT EXISTS assessments (id INT PRIMARY KEY, module_code VARCHAR(24) NOT NULL, assessment_type VARCHAR(16) NOT NULL, title VARCHAR(160) NOT NULL, due_date DATE NOT NULL, details VARCHAR(1000) NOT NULL, CONSTRAINT chk_assessment_type CHECK (assessment_type IN ('Assignment', 'Exam')), CONSTRAINT fk_assessments_module FOREIGN KEY (module_code) REFERENCES modules(module_code) ON DELETE CASCADE)",
            "CREATE TABLE IF NOT EXISTS student_assessment_progress (assessment_id INT NOT NULL, student_id INT NOT NULL, status VARCHAR(16) NOT NULL, PRIMARY KEY (assessment_id, student_id), CONSTRAINT chk_assessment_progress_status CHECK (status IN ('Not started', 'In progress', 'Completed')), CONSTRAINT fk_assessment_progress_assessment FOREIGN KEY (assessment_id) REFERENCES assessments(id) ON DELETE CASCADE, CONSTRAINT fk_assessment_progress_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE)",
            "CREATE TABLE IF NOT EXISTS student_tasks (id INT PRIMARY KEY, student_id INT NOT NULL, module_code VARCHAR(24) NULL, task_type VARCHAR(16) NOT NULL, title VARCHAR(160) NOT NULL, due_date DATE NOT NULL, details VARCHAR(1000) NOT NULL, status VARCHAR(16) NOT NULL, CONSTRAINT chk_student_task_type CHECK (task_type IN ('Assignment', 'Exam')), CONSTRAINT chk_student_task_status CHECK (status IN ('Not started', 'In progress', 'Completed')), CONSTRAINT fk_student_tasks_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE, CONSTRAINT fk_student_tasks_module FOREIGN KEY (module_code) REFERENCES modules(module_code) ON DELETE SET NULL)"
        };
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            for (String sql : statements) statement.executeUpdate(sql);
            migrateModuleLecturers(connection);
            seedDemoAdministrator(connection);
        }
    }

    void provisionStudentAccount(int studentId, String passwordHash) throws SQLException {
        try (Connection connection = connect();
                PreparedStatement statement = connection.prepareStatement(
                        "INSERT INTO student_accounts (student_id, password_hash, must_change_password) "
                                + "VALUES (?, ?, TRUE) ON DUPLICATE KEY UPDATE "
                                + "password_hash = VALUES(password_hash), must_change_password = TRUE")) {
            statement.setInt(1, studentId);
            statement.setString(2, passwordHash);
            statement.executeUpdate();
        }
    }

    void changeStudentPassword(int studentId, String passwordHash) throws SQLException {
        try (Connection connection = connect();
                PreparedStatement statement = connection.prepareStatement(
                        "UPDATE student_accounts SET password_hash = ?, must_change_password = FALSE "
                                + "WHERE student_id = ?")) {
            statement.setString(1, passwordHash);
            statement.setInt(2, studentId);
            if (statement.executeUpdate() != 1) {
                throw new SQLException("The student account no longer exists. Ask the administrator to create it again.");
            }
        }
    }

    void provisionLecturerAccount(int lecturerId, String passwordHash) throws SQLException {
        try (Connection connection = connect();
                PreparedStatement statement = connection.prepareStatement(
                        "INSERT INTO lecturer_accounts (lecturer_id, password_hash, must_change_password) "
                                + "VALUES (?, ?, TRUE) ON DUPLICATE KEY UPDATE "
                                + "password_hash = VALUES(password_hash), must_change_password = TRUE")) {
            statement.setInt(1, lecturerId);
            statement.setString(2, passwordHash);
            statement.executeUpdate();
        }
    }

    void changeLecturerPassword(int lecturerId, String passwordHash) throws SQLException {
        try (Connection connection = connect();
                PreparedStatement statement = connection.prepareStatement(
                        "UPDATE lecturer_accounts SET password_hash = ?, must_change_password = FALSE "
                                + "WHERE lecturer_id = ?")) {
            statement.setString(1, passwordHash);
            statement.setInt(2, lecturerId);
            if (statement.executeUpdate() != 1) {
                throw new SQLException("The lecturer account no longer exists. Ask the administrator to create it again.");
            }
        }
    }

    static char[] defaultTemporaryPassword() {
        return DEFAULT_TEMPORARY_PASSWORD.toCharArray();
    }

    private void deleteRemovedStudents(Connection connection, List<PortalData.Student> students)
            throws SQLException {
        StringBuilder sql = new StringBuilder("DELETE FROM students");
        if (!students.isEmpty()) {
            sql.append(" WHERE id NOT IN (");
            for (int i = 0; i < students.size(); i++) {
                if (i > 0) sql.append(", ");
                sql.append("?");
            }
            sql.append(")");
        }
        try (PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < students.size(); i++) statement.setInt(i + 1, students.get(i).id);
            statement.executeUpdate();
        }
    }

    private void migrateModuleLecturers(Connection connection) throws SQLException {
        boolean hasLegacyLecturerColumn;
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT COUNT(*) FROM information_schema.COLUMNS "
                        + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'modules' AND COLUMN_NAME = 'lecturer_id'");
                ResultSet results = statement.executeQuery()) {
            hasLegacyLecturerColumn = results.next() && results.getInt(1) > 0;
        }
        if (hasLegacyLecturerColumn) {
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("INSERT IGNORE INTO module_lecturers (module_code, lecturer_id) "
                        + "SELECT module_code, lecturer_id FROM modules WHERE lecturer_id IS NOT NULL");
            }

            List<String> constraints = new ArrayList<String>();
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT DISTINCT CONSTRAINT_NAME FROM information_schema.KEY_COLUMN_USAGE "
                            + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'modules' "
                            + "AND COLUMN_NAME = 'lecturer_id' AND REFERENCED_TABLE_NAME = 'lecturers'");
                    ResultSet results = statement.executeQuery()) {
                while (results.next()) constraints.add(results.getString("CONSTRAINT_NAME"));
            }
            try (Statement statement = connection.createStatement()) {
                for (String constraint : constraints) {
                    statement.executeUpdate("ALTER TABLE modules DROP FOREIGN KEY `"
                            + constraint.replace("`", "``") + "`");
                }
                statement.executeUpdate("ALTER TABLE modules DROP COLUMN lecturer_id");
            }
        }

        boolean hasLegacyAssignments;
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT COUNT(*) FROM information_schema.TABLES "
                        + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'lecturer_modules'");
                ResultSet results = statement.executeQuery()) {
            hasLegacyAssignments = results.next() && results.getInt(1) > 0;
        }
        if (hasLegacyAssignments) {
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("INSERT IGNORE INTO module_lecturers (module_code, lecturer_id) "
                        + "SELECT module_code, lecturer_id FROM lecturer_modules");
                statement.executeUpdate("DROP TABLE lecturer_modules");
            }
        }
    }

    private void seedDemoAdministrator(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement();
                ResultSet results = statement.executeQuery("SELECT COUNT(*) FROM administrators")) {
            if (!results.next() || results.getLong(1) != 0) return;
        }
        String passwordHash;
        char[] demoPassword = DEMO_ADMIN_PASSWORD.toCharArray();
        try {
            passwordHash = hashPassword(demoPassword);
        } catch (GeneralSecurityException error) {
            throw new SQLException("Could not create the administrator password hash.", error);
        } finally {
            Arrays.fill(demoPassword, '\0');
        }
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO administrators (username, first_name, surname, password_hash) VALUES (?, ?, ?, ?)")) {
            statement.setString(1, DEMO_ADMIN_USERNAME);
            statement.setString(2, "Campus");
            statement.setString(3, "Administrator");
            statement.setString(4, passwordHash);
            statement.executeUpdate();
        }
    }

    PortalData.Administrator authenticateAdministrator(String username, char[] suppliedPassword)
            throws SQLException {
        try (Connection connection = connect();
                PreparedStatement statement = connection.prepareStatement(
                        "SELECT id, username, first_name, surname, password_hash FROM administrators WHERE username = ?")) {
            statement.setString(1, username);
            try (ResultSet results = statement.executeQuery()) {
                if (!results.next() || !verifyPassword(suppliedPassword, results.getString("password_hash"))) {
                    return null;
                }
                return new PortalData.Administrator(results.getInt("id"),
                        results.getString("username"),
                        results.getString("first_name") + " " + results.getString("surname"));
            }
        }
    }

    static String hashPassword(char[] password) throws GeneralSecurityException {
        byte[] salt = new byte[PASSWORD_SALT_BYTES];
        new SecureRandom().nextBytes(salt);
        PBEKeySpec specification = new PBEKeySpec(password, salt, PASSWORD_ITERATIONS, PASSWORD_HASH_BITS);
        try {
            byte[] hash = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(specification).getEncoded();
            return "pbkdf2_sha256$" + PASSWORD_ITERATIONS + "$"
                    + Base64.getEncoder().encodeToString(salt) + "$"
                    + Base64.getEncoder().encodeToString(hash);
        } finally {
            specification.clearPassword();
            Arrays.fill(salt, (byte) 0);
        }
    }

    static boolean verifyPassword(char[] password, String encodedHash) throws SQLException {
        String[] parts = encodedHash.split("\\$", -1);
        if (parts.length != 4 || !"pbkdf2_sha256".equals(parts[0])) {
            throw new SQLException("The stored administrator password hash is invalid.");
        }
        try {
            int iterations = Integer.parseInt(parts[1]);
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[3]);
            if (iterations < 1 || iterations > 1000000 || salt.length == 0 || expectedHash.length == 0) {
                throw new SQLException("The stored administrator password hash has invalid parameters.");
            }
            PBEKeySpec specification = new PBEKeySpec(password, salt, iterations, expectedHash.length * 8);
            try {
                byte[] actualHash = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                        .generateSecret(specification).getEncoded();
                return MessageDigest.isEqual(expectedHash, actualHash);
            } finally {
                specification.clearPassword();
                Arrays.fill(salt, (byte) 0);
                Arrays.fill(expectedHash, (byte) 0);
            }
        } catch (IllegalArgumentException | GeneralSecurityException error) {
            throw new SQLException("Could not verify the administrator password hash.", error);
        }
    }

    boolean isInitialized() throws SQLException {
        try (Connection connection = connect();
                PreparedStatement statement = connection.prepareStatement(
                        "SELECT metadata_value FROM portal_metadata WHERE metadata_key = ?")) {
            statement.setString(1, "application_initialized");
            try (ResultSet results = statement.executeQuery()) {
                return results.next() && "true".equals(results.getString(1));
            }
        }
    }

    boolean hasApplicationRecords() throws SQLException {
        try (Connection connection = connect(); Statement statement = connection.createStatement();
                ResultSet results = statement.executeQuery(
                        "SELECT (SELECT COUNT(*) FROM students) + (SELECT COUNT(*) FROM lecturers)")) {
            return results.next() && results.getLong(1) > 0;
        }
    }

    List<PortalData.Student> loadStudents() throws SQLException {
        List<PortalData.Student> students = new ArrayList<PortalData.Student>();
        Map<Integer, PortalData.Student> byId = new HashMap<Integer, PortalData.Student>();
        try (Connection connection = connect();
                PreparedStatement statement = connection.prepareStatement(
                        "SELECT s.id, s.student_number, s.first_name, s.surname, s.email, s.course_code, "
                                + "s.status, s.enrollment_date, a.password_hash, a.must_change_password "
                                + "FROM students s LEFT JOIN student_accounts a ON a.student_id = s.id ORDER BY s.id");
                ResultSet results = statement.executeQuery()) {
            while (results.next()) {
                PortalData.Student student = new PortalData.Student(
                        results.getInt("id"),
                        results.getString("student_number"),
                        results.getString("first_name"),
                        results.getString("surname"),
                        results.getString("email"),
                        results.getString("course_code"),
                        results.getString("status"),
                        results.getDate("enrollment_date").toLocalDate().toString());
                student.passwordHash = results.getString("password_hash");
                student.mustChangePassword = results.getBoolean("must_change_password");
                students.add(student);
                byId.put(student.id, student);
            }
        }
        try (Connection connection = connect();
                PreparedStatement statement = connection.prepareStatement(
                        "SELECT student_id, module_code, assessment_number, score FROM assessment_marks ORDER BY student_id, module_code, assessment_number");
                ResultSet results = statement.executeQuery()) {
            while (results.next()) {
                PortalData.Student student = byId.get(results.getInt("student_id"));
                if (student == null) continue;
                String moduleCode = results.getString("module_code");
                List<Integer> marks = student.marks.get(moduleCode);
                if (marks == null) {
                    marks = new ArrayList<Integer>(Arrays.asList(null, null, null, null));
                    student.marks.put(moduleCode, marks);
                }
                marks.set(results.getInt("assessment_number") - 1, results.getInt("score"));
            }
        }
        return students;
    }

    List<PortalData.Lecturer> loadLecturers() throws SQLException {
        List<PortalData.Lecturer> lecturers = new ArrayList<PortalData.Lecturer>();
        try (Connection connection = connect();
                PreparedStatement statement = connection.prepareStatement(
                        "SELECT l.id, l.first_name, l.surname, l.email, a.password_hash, a.must_change_password "
                                + "FROM lecturers l LEFT JOIN lecturer_accounts a ON a.lecturer_id = l.id "
                                + "ORDER BY l.id");
                ResultSet results = statement.executeQuery()) {
            while (results.next()) {
                PortalData.Lecturer lecturer = new PortalData.Lecturer(
                        results.getInt("id"),
                        results.getString("first_name"),
                        results.getString("surname"),
                        results.getString("email"));
                lecturer.passwordHash = results.getString("password_hash");
                lecturer.mustChangePassword = results.getBoolean("must_change_password");
                lecturers.add(lecturer);
            }
        }
        return lecturers;
    }

    List<PortalData.Course> loadCourses() throws SQLException {
        List<PortalData.Course> courses = new ArrayList<PortalData.Course>();
        Map<String, PortalData.Course> byCode = new HashMap<String, PortalData.Course>();
        try (Connection connection = connect();
                PreparedStatement statement = connection.prepareStatement(
                        "SELECT course_code, course_name, duration FROM courses ORDER BY course_code");
                ResultSet results = statement.executeQuery()) {
            while (results.next()) {
                PortalData.Course course = new PortalData.Course(results.getString("course_code"),
                        results.getString("course_name"), results.getString("duration"),
                        new ArrayList<String>());
                courses.add(course);
                byCode.put(course.code, course);
            }
        }
        try (Connection connection = connect();
                PreparedStatement statement = connection.prepareStatement(
                        "SELECT course_code, module_code FROM course_modules ORDER BY course_code, module_code");
                ResultSet results = statement.executeQuery()) {
            while (results.next()) {
                PortalData.Course course = byCode.get(results.getString("course_code"));
                if (course != null) course.modules.add(results.getString("module_code"));
            }
        }
        return courses;
    }

    List<PortalData.Module> loadModules() throws SQLException {
        List<PortalData.Module> modules = new ArrayList<PortalData.Module>();
        Map<String, PortalData.Module> byCode = new HashMap<String, PortalData.Module>();
        try (Connection connection = connect();
                PreparedStatement statement = connection.prepareStatement(
                        "SELECT module_code, module_name FROM modules ORDER BY module_code");
                ResultSet results = statement.executeQuery()) {
            while (results.next()) {
                PortalData.Module module = new PortalData.Module(
                        results.getString("module_code"), results.getString("module_name"));
                modules.add(module);
                byCode.put(module.code, module);
            }
        }
        try (Connection connection = connect();
                PreparedStatement statement = connection.prepareStatement(
                        "SELECT module_code, lecturer_id FROM module_lecturers "
                                + "ORDER BY module_code, lecturer_id");
                ResultSet results = statement.executeQuery()) {
            while (results.next()) {
                PortalData.Module module = byCode.get(results.getString("module_code"));
                if (module != null) module.lecturerIds.add(results.getInt("lecturer_id"));
            }
        }
        return modules;
    }

    List<PortalData.TimetableEntry> loadTimetableEntries() throws SQLException {
        List<PortalData.TimetableEntry> entries = new ArrayList<PortalData.TimetableEntry>();
        try (Connection connection = connect();
                PreparedStatement statement = connection.prepareStatement(
                        "SELECT student_id, module_code, day_of_week, start_time, end_time, location "
                                + "FROM timetable_entries ORDER BY student_id, day_of_week, start_time");
                ResultSet results = statement.executeQuery()) {
            while (results.next()) {
                entries.add(new PortalData.TimetableEntry(
                        results.getInt("student_id"),
                        results.getString("module_code"),
                        java.time.DayOfWeek.of(results.getInt("day_of_week")),
                        results.getTime("start_time").toLocalTime(),
                        results.getTime("end_time").toLocalTime(),
                        results.getString("location")));
            }
        }
        return entries;
    }

    List<PortalData.Announcement> loadAnnouncements() throws SQLException {
        List<PortalData.Announcement> announcements = new ArrayList<PortalData.Announcement>();
        try (Connection connection = connect();
                PreparedStatement statement = connection.prepareStatement(
                        "SELECT id, title, message, published_on, due_date FROM announcements "
                                + "ORDER BY published_on DESC, id DESC");
                ResultSet results = statement.executeQuery()) {
            while (results.next()) {
                java.sql.Date dueDate = results.getDate("due_date");
                announcements.add(new PortalData.Announcement(
                        results.getInt("id"),
                        results.getString("title"),
                        results.getString("message"),
                        results.getDate("published_on").toLocalDate(),
                        dueDate == null ? null : dueDate.toLocalDate()));
            }
        }
        return announcements;
    }

    List<PortalData.Assessment> loadAssessments() throws SQLException {
        List<PortalData.Assessment> assessments = new ArrayList<PortalData.Assessment>();
        try (Connection connection = connect();
                PreparedStatement statement = connection.prepareStatement(
                        "SELECT id, module_code, assessment_type, title, due_date, details "
                                + "FROM assessments ORDER BY due_date, id");
                ResultSet results = statement.executeQuery()) {
            while (results.next()) {
                assessments.add(new PortalData.Assessment(
                        results.getInt("id"),
                        results.getString("module_code"),
                        results.getString("assessment_type"),
                        results.getString("title"),
                        results.getDate("due_date").toLocalDate(),
                        results.getString("details")));
            }
        }
        return assessments;
    }

    List<PortalData.AssessmentProgress> loadAssessmentProgress() throws SQLException {
        List<PortalData.AssessmentProgress> progress = new ArrayList<PortalData.AssessmentProgress>();
        try (Connection connection = connect();
                PreparedStatement statement = connection.prepareStatement(
                        "SELECT assessment_id, student_id, status FROM student_assessment_progress "
                                + "ORDER BY assessment_id, student_id");
                ResultSet results = statement.executeQuery()) {
            while (results.next()) {
                progress.add(new PortalData.AssessmentProgress(
                        results.getInt("assessment_id"),
                        results.getInt("student_id"),
                        results.getString("status")));
            }
        }
        return progress;
    }

    List<PortalData.StudentTask> loadStudentTasks() throws SQLException {
        List<PortalData.StudentTask> tasks = new ArrayList<PortalData.StudentTask>();
        try (Connection connection = connect();
                PreparedStatement statement = connection.prepareStatement(
                        "SELECT id, student_id, module_code, task_type, title, due_date, details, status "
                                + "FROM student_tasks ORDER BY due_date, id");
                ResultSet results = statement.executeQuery()) {
            while (results.next()) {
                tasks.add(new PortalData.StudentTask(
                        results.getInt("id"),
                        results.getInt("student_id"),
                        results.getString("module_code"),
                        results.getString("task_type"),
                        results.getString("title"),
                        results.getDate("due_date").toLocalDate(),
                        results.getString("details"),
                        results.getString("status")));
            }
        }
        return tasks;
    }

    void save(List<PortalData.Student> students, List<PortalData.Lecturer> lecturers,
            List<PortalData.Course> courses, List<PortalData.Module> modules,
            List<PortalData.TimetableEntry> timetableEntries,
            List<PortalData.Announcement> announcements,
            List<PortalData.Assessment> assessments,
            List<PortalData.AssessmentProgress> assessmentProgress,
            List<PortalData.StudentTask> studentTasks) throws SQLException {
        try (Connection connection = connect()) {
            connection.setAutoCommit(false);
            try {
                saveLecturers(connection, lecturers);
                saveCurriculum(connection, courses, modules);
                saveModuleLecturers(connection, modules);
                try (Statement statement = connection.createStatement()) {
                    statement.executeUpdate("DELETE FROM timetable_entries");
                    statement.executeUpdate("DELETE FROM announcements");
                    statement.executeUpdate("DELETE FROM assessment_marks");
                }
                deleteRemovedStudents(connection, students);
                saveStudents(connection, students);
                saveAssessments(connection, assessments, assessmentProgress, students);
                saveStudentTasks(connection, studentTasks, students, modules);
                saveMarks(connection, students);
                saveTimetableEntries(connection, timetableEntries);
                saveAnnouncements(connection, announcements);
                deleteRemovedLecturers(connection, lecturers);
                try (PreparedStatement statement = connection.prepareStatement(
                        "INSERT INTO portal_metadata (metadata_key, metadata_value) VALUES (?, ?) ON DUPLICATE KEY UPDATE metadata_value = VALUES(metadata_value)")) {
                    statement.setString(1, "application_initialized");
                    statement.setString(2, "true");
                    statement.executeUpdate();
                }
                connection.commit();
            } catch (SQLException error) {
                connection.rollback();
                throw error;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    private void saveTimetableEntries(Connection connection,
            List<PortalData.TimetableEntry> entries) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO timetable_entries (student_id, module_code, day_of_week, start_time, end_time, location) "
                        + "VALUES (?, ?, ?, ?, ?, ?)")) {
            for (PortalData.TimetableEntry entry : entries) {
                statement.setInt(1, entry.studentId);
                statement.setString(2, entry.moduleCode);
                statement.setInt(3, entry.day.getValue());
                statement.setTime(4, java.sql.Time.valueOf(entry.start));
                statement.setTime(5, java.sql.Time.valueOf(entry.end));
                statement.setString(6, entry.location);
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private void saveAnnouncements(Connection connection,
            List<PortalData.Announcement> announcements) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO announcements (id, title, message, published_on, due_date) VALUES (?, ?, ?, ?, ?)")) {
            for (PortalData.Announcement announcement : announcements) {
                statement.setInt(1, announcement.id);
                statement.setString(2, announcement.title);
                statement.setString(3, announcement.message);
                statement.setObject(4, announcement.publishedOn);
                if (announcement.dueDate == null) statement.setNull(5, java.sql.Types.DATE);
                else statement.setObject(5, announcement.dueDate);
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private void saveCurriculum(Connection connection, List<PortalData.Course> courses,
            List<PortalData.Module> modules) throws SQLException {
        try (PreparedStatement courseStatement = connection.prepareStatement(
                    "INSERT INTO courses (course_code, course_name, duration) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE course_name = VALUES(course_name), duration = VALUES(duration)");
                PreparedStatement moduleStatement = connection.prepareStatement(
                    "INSERT INTO modules (module_code, module_name) VALUES (?, ?) "
                            + "ON DUPLICATE KEY UPDATE module_name = VALUES(module_name)");
                PreparedStatement courseModuleStatement = connection.prepareStatement(
                    "INSERT IGNORE INTO course_modules (course_code, module_code) VALUES (?, ?)")) {
            for (PortalData.Course course : courses) {
                courseStatement.setString(1, course.code);
                courseStatement.setString(2, course.name);
                courseStatement.setString(3, course.duration);
                courseStatement.addBatch();
                for (String moduleCode : course.modules) {
                    courseModuleStatement.setString(1, course.code);
                    courseModuleStatement.setString(2, moduleCode);
                    courseModuleStatement.addBatch();
                }
            }
            for (PortalData.Module module : modules) {
                moduleStatement.setString(1, module.code);
                moduleStatement.setString(2, module.name);
                moduleStatement.addBatch();
            }
            courseStatement.executeBatch();
            moduleStatement.executeBatch();
            courseModuleStatement.executeBatch();
        }
    }

    private void saveModuleLecturers(Connection connection, List<PortalData.Module> modules)
            throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("DELETE FROM module_lecturers");
        }
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO module_lecturers (module_code, lecturer_id) VALUES (?, ?)")) {
            for (PortalData.Module module : modules) {
                for (Integer lecturerId : module.lecturerIds) {
                    statement.setString(1, module.code);
                    statement.setInt(2, lecturerId);
                    statement.addBatch();
                }
            }
            statement.executeBatch();
        }
    }

    private void saveAssessments(Connection connection, List<PortalData.Assessment> assessments,
            List<PortalData.AssessmentProgress> progress, List<PortalData.Student> students)
            throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("DELETE FROM student_assessment_progress");
        }
        deleteRemovedAssessments(connection, assessments);
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO assessments (id, module_code, assessment_type, title, due_date, details) "
                        + "VALUES (?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE "
                        + "module_code = VALUES(module_code), assessment_type = VALUES(assessment_type), "
                        + "title = VALUES(title), due_date = VALUES(due_date), details = VALUES(details)")) {
            for (PortalData.Assessment assessment : assessments) {
                statement.setInt(1, assessment.id);
                statement.setString(2, assessment.moduleCode);
                statement.setString(3, assessment.type);
                statement.setString(4, assessment.title);
                statement.setObject(5, assessment.dueDate);
                statement.setString(6, assessment.details);
                statement.addBatch();
            }
            statement.executeBatch();
        }
        java.util.Set<Integer> studentIds = new java.util.HashSet<Integer>();
        for (PortalData.Student student : students) studentIds.add(student.id);
        java.util.Set<Integer> assessmentIds = new java.util.HashSet<Integer>();
        for (PortalData.Assessment assessment : assessments) assessmentIds.add(assessment.id);
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO student_assessment_progress (assessment_id, student_id, status) "
                        + "VALUES (?, ?, ?)")) {
            for (PortalData.AssessmentProgress item : progress) {
                if (!studentIds.contains(item.studentId) || !assessmentIds.contains(item.assessmentId)) continue;
                statement.setInt(1, item.assessmentId);
                statement.setInt(2, item.studentId);
                statement.setString(3, item.status);
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private void deleteRemovedAssessments(Connection connection, List<PortalData.Assessment> assessments)
            throws SQLException {
        StringBuilder sql = new StringBuilder("DELETE FROM assessments");
        if (!assessments.isEmpty()) {
            sql.append(" WHERE id NOT IN (");
            for (int i = 0; i < assessments.size(); i++) {
                if (i > 0) sql.append(", ");
                sql.append("?");
            }
            sql.append(")");
        }
        try (PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < assessments.size(); i++) statement.setInt(i + 1, assessments.get(i).id);
            statement.executeUpdate();
        }
    }

    private void saveStudentTasks(Connection connection, List<PortalData.StudentTask> tasks,
            List<PortalData.Student> students, List<PortalData.Module> modules) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("DELETE FROM student_tasks");
        }
        java.util.Set<Integer> studentIds = new java.util.HashSet<Integer>();
        for (PortalData.Student student : students) studentIds.add(student.id);
        java.util.Set<String> moduleCodes = new java.util.HashSet<String>();
        for (PortalData.Module module : modules) moduleCodes.add(module.code);
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO student_tasks "
                        + "(id, student_id, module_code, task_type, title, due_date, details, status) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
            for (PortalData.StudentTask task : tasks) {
                if (!studentIds.contains(task.studentId)) continue;
                statement.setInt(1, task.id);
                statement.setInt(2, task.studentId);
                if (task.moduleCode == null || !moduleCodes.contains(task.moduleCode)) {
                    statement.setNull(3, java.sql.Types.VARCHAR);
                }
                else statement.setString(3, task.moduleCode);
                statement.setString(4, task.type);
                statement.setString(5, task.title);
                statement.setObject(6, task.dueDate);
                statement.setString(7, task.details);
                statement.setString(8, task.status);
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private void saveStudents(Connection connection, List<PortalData.Student> students) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO students (id, student_number, first_name, surname, email, course_code, status, enrollment_date) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE "
                        + "student_number = VALUES(student_number), first_name = VALUES(first_name), "
                        + "surname = VALUES(surname), email = VALUES(email), course_code = VALUES(course_code), "
                        + "status = VALUES(status), enrollment_date = VALUES(enrollment_date)")) {
            for (PortalData.Student student : students) {
                statement.setInt(1, student.id);
                statement.setString(2, student.number);
                statement.setString(3, student.firstName);
                statement.setString(4, student.surname);
                statement.setString(5, student.email);
                statement.setString(6, student.courseCode);
                statement.setString(7, student.status);
                statement.setObject(8, LocalDate.parse(student.enrollmentDate));
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private void saveLecturers(Connection connection, List<PortalData.Lecturer> lecturers) throws SQLException {
        try (PreparedStatement lecturerStatement = connection.prepareStatement(
                    "INSERT INTO lecturers (id, first_name, surname, email) VALUES (?, ?, ?, ?) "
                            + "ON DUPLICATE KEY UPDATE first_name = VALUES(first_name), "
                            + "surname = VALUES(surname), email = VALUES(email)")) {
            for (PortalData.Lecturer lecturer : lecturers) {
                lecturerStatement.setInt(1, lecturer.id);
                lecturerStatement.setString(2, lecturer.firstName);
                lecturerStatement.setString(3, lecturer.surname);
                lecturerStatement.setString(4, lecturer.email);
                lecturerStatement.addBatch();
            }
            lecturerStatement.executeBatch();
        }
    }

    private void deleteRemovedLecturers(Connection connection, List<PortalData.Lecturer> lecturers)
            throws SQLException {
        StringBuilder sql = new StringBuilder("DELETE FROM lecturers");
        if (!lecturers.isEmpty()) {
            sql.append(" WHERE id NOT IN (");
            for (int i = 0; i < lecturers.size(); i++) {
                if (i > 0) sql.append(", ");
                sql.append("?");
            }
            sql.append(")");
        }
        try (PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < lecturers.size(); i++) statement.setInt(i + 1, lecturers.get(i).id);
            statement.executeUpdate();
        }
    }

    private void saveMarks(Connection connection, List<PortalData.Student> students) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO assessment_marks (student_id, module_code, assessment_number, score) VALUES (?, ?, ?, ?)")) {
            for (PortalData.Student student : students) {
                for (Map.Entry<String, List<Integer>> entry : student.marks.entrySet()) {
                    List<Integer> marks = entry.getValue();
                    for (int i = 0; i < marks.size() && i < 4; i++) {
                        Integer mark = marks.get(i);
                        if (mark == null) continue;
                        statement.setInt(1, student.id);
                        statement.setString(2, entry.getKey());
                        statement.setInt(3, i + 1);
                        statement.setInt(4, mark);
                        statement.addBatch();
                    }
                }
            }
            statement.executeBatch();
        }
    }

    static List<PortalData.Student> loadLegacyStudents() throws IOException {
        return new CsvStore(java.nio.file.Paths.get(System.getProperty("user.home"), ".jarateng-portal"))
                .loadStudents();
    }

    static List<PortalData.Lecturer> loadLegacyLecturers() throws IOException {
        return new CsvStore(java.nio.file.Paths.get(System.getProperty("user.home"), ".jarateng-portal"))
                .loadLecturers();
    }
}
