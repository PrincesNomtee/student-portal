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
        Integer lecturerId;

        Module(String code, String name, Integer lecturerId) {
            this.code = code;
            this.name = name;
            this.lecturerId = lecturerId;
        }
    }

    static final class Lecturer {
        final int id;
        final String firstName;
        final String surname;
        final String email;
        final List<String> modules;

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

    final List<Student> students;
    final List<Course> courses;
    final List<Module> modules;
    final List<Lecturer> lecturers;
    final List<TimetableEntry> timetableEntries;
    final List<Announcement> announcements;
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
        this.store = store;
        this.students = students;
        this.courses = courses;
        this.modules = modules;
        this.lecturers = lecturers;
        this.timetableEntries = timetableEntries;
        this.announcements = announcements;
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
        if (courses.isEmpty() && modules.isEmpty()) {
            courses = sampleCourses();
            modules = sampleModules();
        }
        PortalData data = new PortalData(store, students, lecturers, courses, modules,
                timetableEntries, announcements, false);
        if (!store.isInitialized()) data.save();
        return data;
    }

    static PortalData demo() {
        return new PortalData(null, sampleStudents(), sampleLecturers(),
                sampleCourses(), sampleModules(), new ArrayList<TimetableEntry>(),
                new ArrayList<Announcement>(), true);
    }

    boolean isSessionOnlyDemo() {
        return sessionOnlyDemo;
    }

    void save() throws SQLException {
        if (sessionOnlyDemo) return;
        store.save(students, lecturers, courses, modules, timetableEntries, announcements);
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
            if (module.lecturerId == null) continue;
            Lecturer lecturer = lecturer(module.lecturerId);
            if (lecturer != null) lecturer.modules.add(module.code);
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
