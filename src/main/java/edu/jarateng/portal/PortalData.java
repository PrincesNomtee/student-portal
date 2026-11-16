package edu.jarateng.portal;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class PortalData {
    static final class Student {
        int id;
        String number;
        String firstName;
        String surname;
        String email;
        String courseCode;
        String status;
        String enrollmentDate;
        String passwordHash;
        boolean mustChangePassword;
        Map<String, List<Integer>> marks = new LinkedHashMap<String, List<Integer>>();

        Student(int id, String number, String firstName, String surname, String email,
                String courseCode, String status, String enrollmentDate) {
            this.id = id;
            this.number = number;
            this.firstName = firstName;
            this.surname = surname;
            this.email = email;
            this.courseCode = courseCode;
            this.status = status;
            this.enrollmentDate = enrollmentDate;
        }

        String fullName() {
            return firstName + " " + surname;
        }

        boolean hasAccount() {
            return passwordHash != null && !passwordHash.isEmpty();
        }
    }

    static final class Course {
        final String code;
        final String name;
        final String duration;
        final List<String> modules;

        Course(String code, String name, String duration, String... modules) {
            this(code, name, duration, Arrays.asList(modules));
        }

        Course(String code, String name, String duration, List<String> modules) {
            this.code = code;
            this.name = name;
            this.duration = duration;
            this.modules = new ArrayList<String>(modules);
        }
    }

    static final class Module {
        final String code;
        final String name;
        final List<Integer> lecturerIds;

        Module(String code, String name) {
            this(code, name, (Integer) null);
        }

        Module(String code, String name, Integer lecturerId) {
            this.code = code;
            this.name = name;
            this.lecturerIds = new ArrayList<Integer>();
            if (lecturerId != null) this.lecturerIds.add(lecturerId);
        }
    }

    static final class Lecturer {
        final int id;
        final String firstName;
        final String surname;
        final String email;
        final List<String> modules;
        String passwordHash;
        boolean mustChangePassword;

        Lecturer(int id, String firstName, String surname, String email, String... modules) {
            this.id = id;
            this.firstName = firstName;
            this.surname = surname;
            this.email = email;
            this.modules = new ArrayList<String>(Arrays.asList(modules));
        }

        String fullName() {
            return firstName + " " + surname;
        }

        boolean hasAccount() {
            return passwordHash != null && !passwordHash.isEmpty();
        }
    }

    static final class Administrator {
        final int id;
        final String username;
        final String name;

        Administrator(int id, String username, String name) {
            this.id = id;
            this.username = username;
            this.name = name;
        }
    }

    static final class TimetableEntry {
        final int studentId;
        final String moduleCode;
        final DayOfWeek day;
        final LocalTime start;
        final LocalTime end;
        final String location;

        TimetableEntry(int studentId, String moduleCode, DayOfWeek day, LocalTime start,
                LocalTime end, String location) {
            this.studentId = studentId;
            this.moduleCode = moduleCode;
            this.day = day;
            this.start = start;
            this.end = end;
            this.location = location;
        }
    }

    static final class Announcement {
        final int id;
        final String title;
        final String message;
        final LocalDate publishedOn;
        final LocalDate dueDate;

        Announcement(int id, String title, String message, LocalDate publishedOn, LocalDate dueDate) {
            this.id = id;
            this.title = title;
            this.message = message;
            this.publishedOn = publishedOn;
            this.dueDate = dueDate;
        }
    }

    static final class Assessment {
        final int id;
        final String moduleCode;
        final String type;
        final String title;
        final LocalDate dueDate;
        final String details;

        Assessment(int id, String moduleCode, String type, String title,
                LocalDate dueDate, String details) {
            this.id = id;
            this.moduleCode = moduleCode;
            this.type = type;
            this.title = title;
            this.dueDate = dueDate;
            this.details = details;
        }
    }

    static final class AssessmentProgress {
        final int assessmentId;
        final int studentId;
        String status;

        AssessmentProgress(int assessmentId, int studentId, String status) {
            this.assessmentId = assessmentId;
            this.studentId = studentId;
            this.status = status;
        }
    }

    static final class StudentTask {
        final int id;
        final int studentId;
        final String moduleCode;
        final String type;
        final String title;
        final LocalDate dueDate;
        final String details;
        String status;

        StudentTask(int id, int studentId, String moduleCode, String type, String title,
                LocalDate dueDate, String details, String status) {
            this.id = id;
            this.studentId = studentId;
            this.moduleCode = moduleCode;
            this.type = type;
            this.title = title;
            this.dueDate = dueDate;
            this.details = details;
            this.status = status;
        }
    }

    static final class AttendanceSession {
        final int id;
        final String moduleCode;
        final LocalDate sessionDate;
        final String title;

        AttendanceSession(int id, String moduleCode, LocalDate sessionDate, String title) {
            this.id = id;
            this.moduleCode = moduleCode;
            this.sessionDate = sessionDate;
            this.title = title;
        }
    }

    static final class AttendanceRecord {
        final int sessionId;
        final int studentId;
        String status;

        AttendanceRecord(int sessionId, int studentId, String status) {
            this.sessionId = sessionId;
            this.studentId = studentId;
            this.status = status;
        }
    }

    final List<Student> students;
    final List<Course> courses;
    final List<Module> modules;
    final List<Lecturer> lecturers;
    final List<TimetableEntry> timetableEntries;
    final List<Announcement> announcements;
    final List<Assessment> assessments;
    final List<AssessmentProgress> assessmentProgress;
    final List<StudentTask> studentTasks;
    final List<AttendanceSession> attendanceSessions;
    final List<AttendanceRecord> attendanceRecords;
    private final DatabaseStore store;
    private final boolean sessionOnlyDemo;

    private PortalData(DatabaseStore store, List<Student> students, List<Lecturer> lecturers,
            List<Course> courses, List<Module> modules) {
        this(store, students, lecturers, courses, modules,
                new ArrayList<TimetableEntry>(), new ArrayList<Announcement>(), false);
    }

    private PortalData(DatabaseStore store, List<Student> students, List<Lecturer> lecturers,
            List<Course> courses, List<Module> modules, List<TimetableEntry> timetableEntries,
            List<Announcement> announcements, boolean sessionOnlyDemo) {
        this(store, students, lecturers, courses, modules, timetableEntries, announcements,
                new ArrayList<Assessment>(), new ArrayList<AssessmentProgress>(),
                new ArrayList<StudentTask>(), new ArrayList<AttendanceSession>(),
                new ArrayList<AttendanceRecord>(), sessionOnlyDemo);
    }

    private PortalData(DatabaseStore store, List<Student> students, List<Lecturer> lecturers,
            List<Course> courses, List<Module> modules, List<TimetableEntry> timetableEntries,
            List<Announcement> announcements, List<Assessment> assessments,
            List<AssessmentProgress> assessmentProgress, List<StudentTask> studentTasks,
            List<AttendanceSession> attendanceSessions, List<AttendanceRecord> attendanceRecords,
            boolean sessionOnlyDemo) {
        this.store = store;
        this.students = students;
        this.courses = courses;
        this.modules = modules;
        this.lecturers = lecturers;
        this.timetableEntries = timetableEntries;
        this.announcements = announcements;
        this.assessments = assessments;
        this.assessmentProgress = assessmentProgress;
        this.studentTasks = studentTasks;
        this.attendanceSessions = attendanceSessions;
        this.attendanceRecords = attendanceRecords;
        this.sessionOnlyDemo = sessionOnlyDemo;
        synchronizeLecturerModules();
    }

    private static List<Course> sampleCourses() {
        return new ArrayList<Course>(Arrays.asList(
                new Course("BIS101", "Business Information Systems", "3 years", "BDA201", "DPM202", "DVR203", "ISF204"),
                new Course("DSA101", "Data Science and Analytics", "3 years", "BDA201", "DVR203", "ASM205", "CPE301"),
                new Course("CYB101", "Cybersecurity Operations", "3 years", "DPM202", "ISF204", "BDA201", "CPE301"),
                new Course("FDE101", "Fashion Design", "3 years", "FDM101", "FDM102", "FDM103",
                        "FDM201", "FDM202", "FDM203", "FDM301", "FDM302")));
    }

    private static List<Module> sampleModules() {
        return new ArrayList<Module>(Arrays.asList(
                new Module("BDA201", "Business Data Management and Analytics", 1),
                new Module("DPM202", "Digital Project Management", 2),
                new Module("DVR203", "Data Visualisation and Reporting", 1),
                new Module("ISF204", "Information Security Fundamentals", 2),
                new Module("ASM205", "Applied Statistical Modelling", 3),
                new Module("CPE301", "Cloud Platforms and DevOps", 4),
                new Module("FDM101", "Fashion Illustration and Design Principles", null),
                new Module("FDM102", "Textiles and Fabric Science", null),
                new Module("FDM103", "Pattern Drafting and Garment Construction", null),
                new Module("FDM201", "Fashion History and Cultural Studies", null),
                new Module("FDM202", "Digital Fashion Design and CAD", null),
                new Module("FDM203", "Draping and Advanced Garment Construction", null),
                new Module("FDM301", "Fashion Merchandising and Marketing", null),
                new Module("FDM302", "Sustainable Fashion and Collection Development", null)));
    }

    private static List<Lecturer> sampleLecturers() {
        return new ArrayList<Lecturer>(Arrays.asList(
                new Lecturer(1, "Morgan", "Lane", "m.lane@jarateng.edu", "BDA201", "DVR203"),
                new Lecturer(2, "Sage", "Rowan", "s.rowan@jarateng.edu", "DPM202", "ISF204"),
                new Lecturer(3, "Jamie", "Reed", "j.reed@jarateng.edu", "ASM205", "DVR203"),
                new Lecturer(4, "Robin", "Ellis", "r.ellis@jarateng.edu", "CPE301")));
    }

    static PortalData load(DatabaseStore store) throws IOException, SQLException {
        store.initialize();
        List<Student> students;
        List<Lecturer> lecturers;
        if (store.isInitialized()) {
            students = store.loadStudents();
            lecturers = store.loadLecturers();
        } else if (store.hasApplicationRecords()) {
            students = store.loadStudents();
            lecturers = store.loadLecturers();
        } else {
            List<Student> legacyStudents = DatabaseStore.loadLegacyStudents();
            List<Lecturer> legacyLecturers = DatabaseStore.loadLegacyLecturers();
            students = legacyStudents == null || legacyStudents.isEmpty() ? sampleStudents() : legacyStudents;
            lecturers = legacyLecturers == null || legacyLecturers.isEmpty() ? sampleLecturers() : legacyLecturers;
        }
        List<Course> courses = store.loadCourses();
        List<Module> modules = store.loadModules();
        List<TimetableEntry> timetableEntries = store.loadTimetableEntries();
        List<Announcement> announcements = store.loadAnnouncements();
        List<Assessment> assessments = store.loadAssessments();
        List<AssessmentProgress> assessmentProgress = store.loadAssessmentProgress();
        List<StudentTask> studentTasks = store.loadStudentTasks();
        List<AttendanceSession> attendanceSessions = store.loadAttendanceSessions();
        List<AttendanceRecord> attendanceRecords = store.loadAttendanceRecords();
        if (courses.isEmpty() && modules.isEmpty()) {
            courses = sampleCourses();
            modules = sampleModules();
        }
        PortalData data = new PortalData(store, students, lecturers, courses, modules,
                timetableEntries, announcements, assessments, assessmentProgress, studentTasks,
                attendanceSessions, attendanceRecords, false);
        if (!store.isInitialized()) data.save();
        return data;
    }

    static PortalData demo() {
        List<Assessment> assessments = new ArrayList<Assessment>(Arrays.asList(
                new Assessment(1, "BDA201", "Assignment", "Data analysis case study",
                        LocalDate.now().plusDays(7), "Analyse the sample business dataset and summarize your findings."),
                new Assessment(2, "DPM202", "Exam", "Project management midterm",
                        LocalDate.now().plusDays(14), "Review the project planning and delivery topics.")));
        List<StudentTask> studentTasks = new ArrayList<StudentTask>(Arrays.asList(
                new StudentTask(1, 1, "BDA201", "Assignment", "Review case study notes",
                        LocalDate.now().plusDays(3), "Prepare questions for the next class.", "In progress")));
        List<AttendanceSession> attendanceSessions = new ArrayList<AttendanceSession>(Arrays.asList(
                new AttendanceSession(1, "BDA201", LocalDate.now().minusDays(7), "Lecture"),
                new AttendanceSession(2, "DVR203", LocalDate.now().minusDays(3), "Lab session")));
        List<AttendanceRecord> attendanceRecords = new ArrayList<AttendanceRecord>(Arrays.asList(
                new AttendanceRecord(1, 1, "Present"),
                new AttendanceRecord(1, 2, "Absent"),
                new AttendanceRecord(1, 3, "Present"),
                new AttendanceRecord(2, 1, "Present"),
                new AttendanceRecord(2, 2, "Present")));
        return new PortalData(null, sampleStudents(), sampleLecturers(),
                sampleCourses(), sampleModules(), new ArrayList<TimetableEntry>(),
                new ArrayList<Announcement>(), assessments, new ArrayList<AssessmentProgress>(),
                studentTasks, attendanceSessions, attendanceRecords, true);
    }

    boolean isSessionOnlyDemo() {
        return sessionOnlyDemo;
    }

    void save() throws SQLException {
        if (sessionOnlyDemo) return;
        store.save(students, lecturers, courses, modules, timetableEntries, announcements,
                assessments, assessmentProgress, studentTasks, attendanceSessions, attendanceRecords);
    }

    Administrator authenticateAdministrator(String username, char[] password) throws SQLException {
        if (sessionOnlyDemo) {
            char[] expectedPassword = DatabaseStore.DEMO_ADMIN_PASSWORD.toCharArray();
            try {
                if (DatabaseStore.DEMO_ADMIN_USERNAME.equals(username)
                        && Arrays.equals(expectedPassword, password)) {
                    return new Administrator(1, DatabaseStore.DEMO_ADMIN_USERNAME, "Campus Administrator");
                }
            } finally {
                Arrays.fill(expectedPassword, '\0');
            }
            return null;
        }
        return store.authenticateAdministrator(username, password);
    }

    Student authenticateStudent(String studentNumber, char[] password) throws SQLException {
        for (Student candidate : students) {
            boolean demoAlias = sessionOnlyDemo && candidate.id == 1
                    && "student.demo".equalsIgnoreCase(studentNumber);
            if (!candidate.number.equalsIgnoreCase(studentNumber) && !demoAlias) continue;
            if (candidate.hasAccount()) {
                return DatabaseStore.verifyPassword(password, candidate.passwordHash) ? candidate : null;
            }
            if (demoAlias && "Student2026!".equals(new String(password))) return candidate;
        }
        return null;
    }

    Lecturer authenticateLecturer(String email, char[] password) throws SQLException {
        if (sessionOnlyDemo && "lecturer.demo".equalsIgnoreCase(email)
                && "Lecturer2026!".equals(new String(password))) {
            return lecturer(1);
        }
        for (Lecturer candidate : lecturers) {
            if (!candidate.email.equalsIgnoreCase(email)) continue;
            if (candidate.hasAccount()
                    && DatabaseStore.verifyPassword(password, candidate.passwordHash)) return candidate;
        }
        return null;
    }

    void provisionStudentAccount(Student student, char[] temporaryPassword) throws SQLException {
        String passwordHash;
        try {
            passwordHash = DatabaseStore.hashPassword(temporaryPassword);
        } catch (GeneralSecurityException error) {
            throw new SQLException("Could not create the student password.", error);
        }
        if (!sessionOnlyDemo) store.provisionStudentAccount(student.id, passwordHash);
        student.passwordHash = passwordHash;
        student.mustChangePassword = true;
    }

    void changeStudentPassword(Student student, char[] password) throws SQLException {
        if (password.length < 12) {
            throw new IllegalArgumentException("Use at least 12 characters for the password.");
        }
        String passwordHash;
        try {
            passwordHash = DatabaseStore.hashPassword(password);
        } catch (GeneralSecurityException error) {
            throw new SQLException("Could not create the student password.", error);
        }
        if (!sessionOnlyDemo) store.changeStudentPassword(student.id, passwordHash);
        student.passwordHash = passwordHash;
        student.mustChangePassword = false;
    }

    void provisionLecturerAccount(Lecturer lecturer, char[] temporaryPassword) throws SQLException {
        String passwordHash;
        try {
            passwordHash = DatabaseStore.hashPassword(temporaryPassword);
        } catch (GeneralSecurityException error) {
            throw new SQLException("Could not create the lecturer password.", error);
        }
        if (!sessionOnlyDemo) store.provisionLecturerAccount(lecturer.id, passwordHash);
        lecturer.passwordHash = passwordHash;
        lecturer.mustChangePassword = true;
    }

    void changeLecturerPassword(Lecturer lecturer, char[] password) throws SQLException {
        if (password.length < 12) {
            throw new IllegalArgumentException("Use at least 12 characters for the password.");
        }
        String passwordHash;
        try {
            passwordHash = DatabaseStore.hashPassword(password);
        } catch (GeneralSecurityException error) {
            throw new SQLException("Could not create the lecturer password.", error);
        }
        if (!sessionOnlyDemo) store.changeLecturerPassword(lecturer.id, passwordHash);
        lecturer.passwordHash = passwordHash;
        lecturer.mustChangePassword = false;
    }

    Course course(String code) {
        for (Course course : courses) {
            if (course.code.equals(code)) return course;
        }
        return null;
    }

    Student student(int id) {
        for (Student student : students) {
            if (student.id == id) return student;
        }
        return null;
    }

    Lecturer lecturer(int id) {
        for (Lecturer lecturer : lecturers) {
            if (lecturer.id == id) return lecturer;
        }
        return null;
    }

    String moduleName(String code) {
        for (Module module : modules) {
            if (module.code.equals(code)) return module.name;
        }
        return code;
    }

    Module module(String code) {
        for (Module module : modules) {
            if (module.code.equals(code)) return module;
        }
        return null;
    }

    void synchronizeLecturerModules() {
        for (Lecturer lecturer : lecturers) lecturer.modules.clear();
        for (Module module : modules) {
            for (Integer lecturerId : module.lecturerIds) {
                Lecturer lecturer = lecturer(lecturerId);
                if (lecturer != null && !lecturer.modules.contains(module.code)) {
                    lecturer.modules.add(module.code);
                }
            }
        }
    }

    int nextStudentId() {
        int next = 1;
        for (Student student : students) next = Math.max(next, student.id + 1);
        return next;
    }

    int nextLecturerId() {
        int next = 1;
        for (Lecturer lecturer : lecturers) next = Math.max(next, lecturer.id + 1);
        return next;
    }

    int nextAnnouncementId() {
        int next = 1;
        for (Announcement announcement : announcements) next = Math.max(next, announcement.id + 1);
        return next;
    }

    int nextAssessmentId() {
        int next = 1;
        for (Assessment assessment : assessments) next = Math.max(next, assessment.id + 1);
        return next;
    }

    int nextStudentTaskId() {
        int next = 1;
        for (StudentTask task : studentTasks) next = Math.max(next, task.id + 1);
        return next;
    }

    int nextAttendanceSessionId() {
        int next = 1;
        for (AttendanceSession session : attendanceSessions) next = Math.max(next, session.id + 1);
        return next;
    }

    String attendanceStatus(int sessionId, int studentId) {
        for (AttendanceRecord record : attendanceRecords) {
            if (record.sessionId == sessionId && record.studentId == studentId) return record.status;
        }
        return "Not marked";
    }

    void setAttendanceStatus(int sessionId, int studentId, String status) {
        for (AttendanceRecord record : attendanceRecords) {
            if (record.sessionId == sessionId && record.studentId == studentId) {
                record.status = status;
                return;
            }
        }
        attendanceRecords.add(new AttendanceRecord(sessionId, studentId, status));
    }

    String assessmentStatus(int studentId, int assessmentId) {
        for (AssessmentProgress progress : assessmentProgress) {
            if (progress.studentId == studentId && progress.assessmentId == assessmentId) {
                return progress.status;
            }
        }
        return "Not started";
    }

    void setAssessmentStatus(int studentId, int assessmentId, String status) {
        for (int i = 0; i < assessmentProgress.size(); i++) {
            AssessmentProgress progress = assessmentProgress.get(i);
            if (progress.studentId == studentId && progress.assessmentId == assessmentId) {
                if (status == null) assessmentProgress.remove(i);
                else progress.status = status;
                return;
            }
        }
        if (status != null) assessmentProgress.add(new AssessmentProgress(assessmentId, studentId, status));
    }

    String studentNumber(int id) {
        return String.format("UJ%02d%04d", java.time.LocalDate.now().getYear() % 100, id);
    }

    static List<Student> sampleStudents() {
        List<Student> students = new ArrayList<Student>();
        Student riley = new Student(1, "UJ260001", "Riley", "Park", "r.park@example.test", "BIS101", "Active", "2025-02-10");
        riley.marks.put("BDA201", Arrays.asList(72, 68, 76, 74));
        riley.marks.put("DPM202", Arrays.asList(64, 71, 69, 73));
        riley.marks.put("DVR203", Arrays.asList(81, 78, 84, 80));
        riley.marks.put("ISF204", Arrays.asList(70, 74, 72, 76));
        Student avery = new Student(2, "UJ260002", "Avery", "Stone", "a.stone@example.test", "DSA101", "Active", "2025-02-12");
        avery.marks.put("BDA201", Arrays.asList(58, 62, 65, 60));
        avery.marks.put("ASM205", Arrays.asList(48, 52, 46, 55));
        avery.marks.put("DVR203", Arrays.asList(75, 73, 78, 76));
        avery.marks.put("CPE301", Arrays.asList(66, 64, 70, 68));
        Student casey = new Student(3, "UJ260003", "Casey", "River", "c.river@example.test", "CYB101", "Pending", "2025-02-14");
        casey.marks.put("DPM202", Arrays.asList(61, 65, 63, 67));
        casey.marks.put("ISF204", Arrays.asList(55, 59, 62, 60));
        casey.marks.put("BDA201", Arrays.asList(70, 72, 74, 71));
        casey.marks.put("CPE301", Arrays.asList(57, 60, 63, 61));
        students.add(riley);
        students.add(avery);
        students.add(casey);
        return students;
    }
}
