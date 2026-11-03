package edu.jarateng.portal;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.io.IOException;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;

public final class PortalApp {
    private static final String DEMO_STUDENT_USERNAME = "student.demo";
    private static final String DEMO_STUDENT_PASSWORD = "Student2026!";
    private static final String DEMO_LECTURER_USERNAME = "lecturer.demo";
    private static final String DEMO_LECTURER_PASSWORD = "Lecturer2026!";
    private static final Color NAVY = new Color(17, 47, 78);
    private static final Color BLUE = new Color(34, 83, 125);
    private static final Color GOLD = new Color(207, 170, 83);
    private static final Color INK = new Color(37, 49, 63);
    private static final Color MUTED = new Color(105, 118, 134);
    private static final Color BACKGROUND = new Color(244, 246, 248);
    private static final Color LINE = new Color(224, 229, 234);
    private static final Font BODY = new Font("Segoe UI", Font.PLAIN, 13);
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;
    private final PortalData data;
    private final JFrame frame = new JFrame("University of Jarateng | Student Information System");
    private PortalUser user;
    private CardLayout pageLayout;
    private JPanel pageCards;
    private JPanel navigation;
    private String currentPage = "Overview";

    private static final class PortalUser {
        final String role;
        final String name;
        final int id;

        PortalUser(String role, String name, int id) {
            this.role = role;
            this.name = name;
            this.id = id;
        }
    }

    private PortalApp(PortalData data) {
        this.data = data;
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(860, 600));
        frame.setSize(1220, 800);
        frame.setLocationRelativeTo(null);
        frame.getAccessibleContext().setAccessibleName("University of Jarateng student portal");
        showLogin();
    }

    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception error) {
            System.err.println("Could not apply the Nimbus appearance: " + error.getMessage());
        }
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (Arrays.asList(args).contains("--demo")) {
                    new PortalApp(PortalData.demo()).frame.setVisible(true);
                    return;
                }
                String password = System.getenv("MYSQL_PASSWORD");
                if (password == null || password.isEmpty()) {
                    JPasswordField passwordInput = new CopyablePasswordField(24);
                    JPanel prompt = new JPanel(new BorderLayout(0, 8));
                    prompt.add(new JLabel("MySQL password for " + DatabaseStore.configuredUsername()), BorderLayout.NORTH);
                    prompt.add(passwordInput, BorderLayout.CENTER);
                    int answer = JOptionPane.showConfirmDialog(null, prompt,
                            "Connect to Jarateng MySQL database", JOptionPane.OK_CANCEL_OPTION,
                            JOptionPane.PLAIN_MESSAGE);
                    if (answer != JOptionPane.OK_OPTION) return;
                    password = new String(passwordInput.getPassword());
                }
                DatabaseStore store = new DatabaseStore(DatabaseStore.configuredUrl(),
                        DatabaseStore.configuredUsername(), password);
                try {
                    new PortalApp(PortalData.load(store)).frame.setVisible(true);
                } catch (IOException | SQLException error) {
                    JOptionPane.showMessageDialog(null,
                            "The portal could not connect to or load the MySQL database.\n\n"
                                    + error.getMessage()
                                    + "\n\nCheck that MySQL Server is running and that the connection settings and database permissions are correct.",
                            "Unable to open portal", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    private void showLogin() {
        JPanel root = new JPanel(new GridLayout(1, 2));
        root.setBackground(Color.WHITE);

        JPanel introduction = new JPanel();
        introduction.setBackground(NAVY);
        introduction.setLayout(new BoxLayout(introduction, BoxLayout.Y_AXIS));
        introduction.setBorder(BorderFactory.createEmptyBorder(62, 60, 48, 56));
        JLabel crest = new JLabel("UJ");
        crest.setOpaque(true);
        crest.setBackground(GOLD);
        crest.setForeground(NAVY);
        crest.setFont(new Font("Segoe UI", Font.BOLD, 22));
        crest.setHorizontalAlignment(SwingConstants.CENTER);
        crest.setPreferredSize(new Dimension(54, 54));
        crest.setMaximumSize(new Dimension(54, 54));
        introduction.add(crest);
        introduction.add(Box.createVerticalStrut(54));
        introduction.add(label("UNIVERSITY OF JARATENG", 11, new Color(239, 209, 137), Font.BOLD));
        introduction.add(Box.createVerticalStrut(12));
        introduction.add(label("Student information,\nmade straightforward.", 30, Color.WHITE, Font.BOLD));
        introduction.add(Box.createVerticalStrut(17));
        introduction.add(label("A campus portal for enrolment, course records and academic progress.", 15, new Color(211, 221, 231), Font.PLAIN));
        introduction.add(Box.createVerticalGlue());
        introduction.add(label("REGISTRAR'S OFFICE  ·  ACADEMIC SERVICES", 10, new Color(173, 190, 207), Font.BOLD));

        JPanel right = new JPanel(new GridBagLayout());
        right.setBackground(Color.WHITE);
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(34, 42, 34, 42));
        form.setMaximumSize(new Dimension(460, 560));
        form.add(label("PORTAL ACCESS", 10, MUTED, Font.BOLD));
        form.add(Box.createVerticalStrut(12));
        form.add(label("Sign in", 28, INK, Font.BOLD));
        form.add(Box.createVerticalStrut(7));
        form.add(label(data.isSessionOnlyDemo()
                ? "Use a demo account to open a role-specific workspace."
                : "Students sign in with their student number and password.", 13, MUTED, Font.PLAIN));
        if (data.isSessionOnlyDemo()) {
            form.add(Box.createVerticalStrut(8));
            form.add(label("SESSION-ONLY DEMO  ·  Changes are not saved after closing.", 11,
                    new Color(163, 91, 24), Font.BOLD));
        }
        form.add(Box.createVerticalStrut(26));

        JComboBox<String> role = combo(new String[] { "Student", "Lecturer", "Administrator" });
        JTextField username = field("");
        username.setToolTipText("Username");
        JPasswordField password = new CopyablePasswordField();
        styleField(password);
        password.setToolTipText("Password");
        form.add(fieldGroup("Workspace", role));
        form.add(Box.createVerticalStrut(14));
        form.add(fieldGroup("Username", username));
        form.add(Box.createVerticalStrut(14));
        form.add(fieldGroup("Password", password));
        form.add(Box.createVerticalStrut(22));
        JButton signIn = primaryButton("Sign in");
        signIn.setAlignmentX(Component.LEFT_ALIGNMENT);
        signIn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        form.add(signIn);
        form.add(Box.createVerticalStrut(19));
        JLabel error = label(" ", 12, new Color(163, 54, 47), Font.PLAIN);
        form.add(error);
        form.add(Box.createVerticalStrut(15));
        if (data.isSessionOnlyDemo()) {
            JPanel demo = new JPanel();
            demo.setBackground(new Color(247, 248, 250));
            demo.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(LINE),
                    BorderFactory.createEmptyBorder(14, 15, 14, 15)));
            demo.setLayout(new BoxLayout(demo, BoxLayout.Y_AXIS));
            demo.add(label("DEMO ACCOUNTS", 10, MUTED, Font.BOLD));
            demo.add(Box.createVerticalStrut(8));
            JButton studentDemo = demoCredentialButton(
                    "Student  " + DEMO_STUDENT_USERNAME + "  /  " + DEMO_STUDENT_PASSWORD);
            studentDemo.addActionListener(event -> {
                role.setSelectedItem("Student");
                username.setText(DEMO_STUDENT_USERNAME);
                password.setText(DEMO_STUDENT_PASSWORD);
                error.setText(" ");
            });
            demo.add(studentDemo);
            demo.add(Box.createVerticalStrut(4));
            JButton lecturerDemo = demoCredentialButton(
                    "Lecturer  " + DEMO_LECTURER_USERNAME + "  /  " + DEMO_LECTURER_PASSWORD);
            lecturerDemo.addActionListener(event -> {
                role.setSelectedItem("Lecturer");
                username.setText(DEMO_LECTURER_USERNAME);
                password.setText(DEMO_LECTURER_PASSWORD);
                error.setText(" ");
            });
            demo.add(lecturerDemo);
            demo.add(Box.createVerticalStrut(4));
            JButton administratorDemo = demoCredentialButton(
                    "Admin  " + DatabaseStore.DEMO_ADMIN_USERNAME + "  /  "
                            + DatabaseStore.DEMO_ADMIN_PASSWORD);
            administratorDemo.addActionListener(event -> {
                role.setSelectedItem("Administrator");
                username.setText(DatabaseStore.DEMO_ADMIN_USERNAME);
                password.setText(DatabaseStore.DEMO_ADMIN_PASSWORD);
                error.setText(" ");
            });
            demo.add(administratorDemo);
            demo.add(Box.createVerticalStrut(10));
            demo.add(label("Demonstration software only. Do not enter real student data.", 10, MUTED, Font.PLAIN));
            demo.setAlignmentX(Component.LEFT_ALIGNMENT);
            form.add(demo);
        } else {
            form.add(label("Student accounts are created from the administrator's Students page.",
                    11, MUTED, Font.PLAIN));
        }

        Runnable submit = new Runnable() {
            @Override
            public void run() {
                String selected = (String) role.getSelectedItem();
                String account = username.getText().trim().toLowerCase();
                char[] secret = password.getPassword();
                PortalUser authenticated = null;
                try {
                    if ("Student".equals(selected)) {
                        PortalData.Student student = data.authenticateStudent(account, secret);
                        if (student != null) {
                            authenticated = new PortalUser("Student", student.fullName(), student.id);
                        }
                    } else if ("Lecturer".equals(selected) && DEMO_LECTURER_USERNAME.equals(account)
                            && DEMO_LECTURER_PASSWORD.equals(new String(secret))) {
                        authenticated = new PortalUser("Lecturer", "Morgan Lane", 1);
                    } else if ("Administrator".equals(selected)) {
                        PortalData.Administrator administrator =
                                data.authenticateAdministrator(account, secret);
                        if (administrator != null) {
                            authenticated = new PortalUser("Administrator", administrator.name, administrator.id);
                        }
                    }
                } catch (SQLException authenticationError) {
                    error.setText("Sign-in could not be verified.");
                    JOptionPane.showMessageDialog(frame,
                            "Sign-in could not be verified.\n\n" + authenticationError.getMessage(),
                            "Sign-in error", JOptionPane.ERROR_MESSAGE);
                    return;
                } finally {
                    Arrays.fill(secret, '\0');
                }
                if (authenticated == null) {
                    error.setText("Those credentials do not match the selected workspace.");
                    return;
                }
                if ("Student".equals(authenticated.role)) {
                    PortalData.Student student = data.student(authenticated.id);
                    if (student == null) {
                        error.setText("This student account has no matching student record.");
                        return;
                    }
                    if (student.mustChangePassword && !changeStudentPasswordDialog(student, true)) return;
                }
                if ("Lecturer".equals(authenticated.role) && data.lecturer(authenticated.id) == null) {
                    error.setText("The demo lecturer account has no record in the faculty directory.");
                    return;
                }
                user = authenticated;
                currentPage = "Overview";
                showWorkspace();
            }
        };
        signIn.addActionListener(event -> submit.run());
        password.addActionListener(event -> submit.run());
        role.addActionListener(event -> error.setText(" "));

        right.add(form);
        root.add(introduction);
        root.add(right);
        frame.setContentPane(root);
        frame.revalidate();
        frame.repaint();
    }

    private void showWorkspace() {
        JPanel shell = new JPanel(new BorderLayout());
        shell.setBackground(BACKGROUND);
        shell.add(buildSidebar(), BorderLayout.WEST);

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(BACKGROUND);
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(Color.WHITE);
        top.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, GOLD),
                BorderFactory.createEmptyBorder(13, 30, 12, 26)));
        top.add(label("UNIVERSITY OF JARATENG  /  " + user.role.toUpperCase(), 10, MUTED, Font.BOLD), BorderLayout.WEST);
        JLabel identity = label(user.name + "     " + user.role, 12, INK, Font.BOLD);
        top.add(identity, BorderLayout.EAST);
        main.add(top, BorderLayout.NORTH);

        pageLayout = new CardLayout();
        pageCards = new JPanel(pageLayout);
        pageCards.setBackground(BACKGROUND);
        for (String page : pagesForRole()) pageCards.add(buildPage(page), page);
        JScrollPane pageScroll = new JScrollPane(pageCards);
        pageScroll.setBorder(BorderFactory.createEmptyBorder());
        pageScroll.getViewport().setBackground(BACKGROUND);
        main.add(pageScroll, BorderLayout.CENTER);
        shell.add(main, BorderLayout.CENTER);
        frame.setContentPane(shell);
        selectPage(currentPage);
        frame.revalidate();
        frame.repaint();
    }

    private JPanel buildSidebar() {
        JPanel side = new JPanel(new BorderLayout());
        side.setPreferredSize(new Dimension(244, 0));
        side.setBackground(NAVY);
        side.setBorder(BorderFactory.createEmptyBorder(22, 14, 15, 14));
        JPanel brand = new JPanel(new BorderLayout(11, 0));
        brand.setOpaque(false);
        JLabel mark = label("UJ", 15, NAVY, Font.BOLD);
        mark.setOpaque(true);
        mark.setBackground(GOLD);
        mark.setHorizontalAlignment(SwingConstants.CENTER);
        mark.setPreferredSize(new Dimension(39, 39));
        JPanel brandText = new JPanel();
        brandText.setOpaque(false);
        brandText.setLayout(new BoxLayout(brandText, BoxLayout.Y_AXIS));
        brandText.add(label("JARATENG", 13, Color.WHITE, Font.BOLD));
        brandText.add(Box.createVerticalStrut(3));
        brandText.add(label("STUDENT PORTAL", 9, new Color(185, 201, 216), Font.PLAIN));
        brand.add(mark, BorderLayout.WEST);
        brand.add(brandText, BorderLayout.CENTER);
        side.add(brand, BorderLayout.NORTH);

        navigation = new JPanel();
        navigation.setOpaque(false);
        navigation.setBorder(BorderFactory.createEmptyBorder(30, 0, 0, 0));
        navigation.setLayout(new BoxLayout(navigation, BoxLayout.Y_AXIS));
        navigation.add(label("WORKSPACE", 9, new Color(222, 192, 119), Font.BOLD));
        navigation.add(Box.createVerticalStrut(11));
        for (String page : pagesForRole()) {
            JButton item = new JButton(page);
            item.setHorizontalAlignment(SwingConstants.LEFT);
            item.setFont(BODY);
            item.setForeground(Color.WHITE);
            item.setBackground(NAVY);
            item.setBorder(BorderFactory.createEmptyBorder(11, 13, 11, 10));
            item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
            item.addActionListener(event -> selectPage(page));
            item.putClientProperty("portal.page", page);
            navigation.add(item);
            navigation.add(Box.createVerticalStrut(3));
        }
        side.add(navigation, BorderLayout.CENTER);

        JPanel footer = new JPanel();
        footer.setOpaque(false);
        footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(54, 77, 102)));
        footer.add(Box.createVerticalStrut(13));
        footer.add(label(user.name, 12, Color.WHITE, Font.BOLD));
        footer.add(Box.createVerticalStrut(4));
        footer.add(label(user.role, 11, new Color(185, 201, 216), Font.PLAIN));
        footer.add(Box.createVerticalStrut(12));
        JButton signOut = new JButton("Sign out");
        signOut.setHorizontalAlignment(SwingConstants.LEFT);
        signOut.setForeground(new Color(220, 228, 236));
        signOut.setBackground(NAVY);
        signOut.setBorder(BorderFactory.createEmptyBorder(7, 0, 5, 0));
        signOut.addActionListener(event -> {
            user = null;
            showLogin();
        });
        footer.add(signOut);
        side.add(footer, BorderLayout.SOUTH);
        return side;
    }

    private List<String> pagesForRole() {
        if ("Administrator".equals(user.role)) {
            return Arrays.asList("Overview", "Students", "Courses", "Lecturers", "Announcements");
        }
        if ("Lecturer".equals(user.role)) {
            return Arrays.asList("Overview", "My students", "Capture marks", "Announcements");
        }
        return Arrays.asList("Overview", "My modules", "Timetable", "Academic results",
                "Announcements", "My profile");
    }

    private void selectPage(String page) {
        currentPage = page;
        if (pageCards != null) {
            pageCards.removeAll();
            for (String item : pagesForRole()) pageCards.add(buildPage(item), item);
            pageLayout.show(pageCards, page);
            pageCards.revalidate();
            pageCards.repaint();
        }
        if (navigation != null) {
            for (Component component : navigation.getComponents()) {
                if (component instanceof JButton) {
                    JButton button = (JButton) component;
                    boolean selected = page.equals(button.getClientProperty("portal.page"));
                    button.setBackground(selected ? BLUE : NAVY);
                    button.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createMatteBorder(0, selected ? 3 : 0, 0, 0, GOLD),
                            BorderFactory.createEmptyBorder(11, selected ? 10 : 13, 11, 10)));
                }
            }
        }
    }

    private JPanel buildPage(String page) {
        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(BACKGROUND);
        content.setBorder(BorderFactory.createEmptyBorder(28, 30, 28, 30));
        JPanel body = new JPanel();
        body.setBackground(BACKGROUND);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(pageHeading(page, pageDescription(page)));
        body.add(Box.createVerticalStrut(20));
        if (data.isSessionOnlyDemo()) {
            body.add(notePanel("Session-only demo",
                    "Sample records are loaded locally. Changes are temporary and will be lost when the app closes."));
            body.add(Box.createVerticalStrut(14));
        }
        switch (page) {
            case "Overview": body.add(overview()); break;
            case "Students": body.add(adminStudents()); break;
            case "Courses": body.add(coursesPage()); break;
            case "Lecturers": body.add(lecturersPage()); break;
            case "My students": body.add(lecturerStudents()); break;
            case "Capture marks": body.add(captureMarks()); break;
            case "My modules": body.add(studentModules()); break;
            case "Timetable": body.add(studentTimetable()); break;
            case "Academic results": body.add(studentResults()); break;
            case "Announcements": body.add(announcementsPage("Administrator".equals(user.role))); break;
            case "My profile": body.add(studentProfile()); break;
            default: body.add(new JLabel("This page is not available."));
        }
        content.add(body, BorderLayout.NORTH);
        return content;
    }

    private String pageDescription(String page) {
        if ("Overview".equals(page)) return "A current snapshot of your " + user.role.toLowerCase() + " workspace.";
        if ("Students".equals(page)) return "Maintain student records and course registrations.";
        if ("Courses".equals(page)) return "Create courses and modules, share modules across courses, and assign lecturers.";
        if ("Lecturers".equals(page)) return "Faculty directory and teaching allocations.";
        if ("My students".equals(page)) return "Students enrolled in the modules assigned to you.";
        if ("Capture marks".equals(page)) return "Record four assessment marks for an enrolled student.";
        if ("My modules".equals(page)) return "Modules registered for your programme of study.";
        if ("Timetable".equals(page)) return "Plan your module week and check for overlapping classes.";
        if ("Academic results".equals(page)) return "Assessment results recorded against your modules.";
        if ("Announcements".equals(page)) return "Campus notices and upcoming deadlines.";
        return "Your student number and current registration details.";
    }

    private JPanel overview() {
        JPanel page = new JPanel();
        page.setOpaque(false);
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
        if ("Administrator".equals(user.role)) {
            int active = countStatus("Active");
            page.add(statRow(
                    statCard("STUDENT RECORDS", String.valueOf(data.students.size()), "Enrolled and pending", "01"),
                    statCard("ACTIVE ENROLMENTS", String.valueOf(active), "Current student status", "02"),
                    statCard("COURSES", String.valueOf(data.courses.size()), "Programmes offered", "03"),
                    statCard("LECTURERS", String.valueOf(data.lecturers.size()), "Faculty directory", "04")));
            page.add(Box.createVerticalStrut(18));
            page.add(section("Recently registered", studentTable(data.students, false)));
            page.add(Box.createVerticalStrut(14));
            JButton add = primaryButton("Register a student");
            add.addActionListener(event -> studentDialog(null));
            page.add(leftAligned(add));
        } else if ("Lecturer".equals(user.role)) {
            PortalData.Lecturer lecturer = data.lecturer(user.id);
            List<PortalData.Student> students = studentsFor(lecturer.modules);
            page.add(statRow(
                    statCard("TEACHING MODULES", String.valueOf(lecturer.modules.size()), "Assigned to you", "01"),
                    statCard("MY STUDENTS", String.valueOf(students.size()), "Across your modules", "02"),
                    statCard("ASSESSED MODULES", String.valueOf(countMarkedStudents(lecturer.modules)), "With recorded marks", "03"),
                    statCard("ACADEMIC YEAR", String.valueOf(LocalDate.now().getYear()), "Current session", "04")));
            page.add(Box.createVerticalStrut(18));
            page.add(section("Teaching allocation", moduleTable(lecturer.modules)));
        } else {
            PortalData.Student student = data.student(user.id);
            PortalData.Course course = data.course(student.courseCode);
            List<String> modules = course == null ? new ArrayList<String>() : course.modules;
            page.add(studentDashboard(student, modules));
        }
        return page;
    }

    private JPanel studentDashboard(PortalData.Student student, List<String> modules) {
        JPanel page = verticalPanel();
        int recorded = recordedAssessmentCount(student, modules);
        int total = modules.size() * 4;
        double average = overallAverage(student, modules);
        int actions = studentNextSteps(student, modules).size();
        int deadlines = upcomingDeadlineCount();
        page.add(statRow(
                statCard("OVERALL AVERAGE", recorded == 0 ? "—" : String.format("%.1f%%", average),
                        recorded + " recorded assessments", "01"),
                statCard("MARKS RECORDED", recorded + " / " + total, "Across enrolled modules", "02"),
                statCard("NEXT STEPS", String.valueOf(actions), "Based on your records", "03"),
                statCard("UPCOMING DEADLINES", String.valueOf(deadlines), "Next 7 days", "04")));
        page.add(Box.createVerticalStrut(18));
        page.add(section("Study progress", assessmentProgress(student, modules)));
        page.add(Box.createVerticalStrut(14));
        page.add(section("Assessment insights", resultsTable(student, modules)));
        page.add(Box.createVerticalStrut(14));
        page.add(section("Your next steps", nextStepsPanel(student, modules)));
        return page;
    }

    private JPanel assessmentProgress(PortalData.Student student, List<String> modules) {
        int recorded = recordedAssessmentCount(student, modules);
        int total = modules.size() * 4;
        int percentage = total == 0 ? 0 : (int) Math.round(recorded * 100.0 / total);
        javax.swing.JProgressBar progress = new javax.swing.JProgressBar(0, 100);
        progress.setValue(percentage);
        progress.setStringPainted(true);
        progress.setString(total == 0 ? "No course modules to track"
                : recorded + " of " + total + " assessment marks recorded");
        progress.setForeground(BLUE);
        progress.getAccessibleContext().setAccessibleName("Assessment recording progress");
        progress.getAccessibleContext().setAccessibleDescription(
                "Shows recorded assessment marks, not course completion.");
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setOpaque(false);
        panel.add(progress, BorderLayout.CENTER);
        panel.add(label("This measures assessment records available in the portal, not course completion.",
                11, MUTED, Font.PLAIN), BorderLayout.SOUTH);
        return panel;
    }

    private JPanel nextStepsPanel(PortalData.Student student, List<String> modules) {
        List<String> steps = studentNextSteps(student, modules);
        JPanel panel = verticalPanel();
        if (steps.isEmpty()) {
            panel.add(label("You are up to date based on the information currently in the portal.",
                    12, INK, Font.PLAIN));
        } else {
            for (String step : steps) {
                panel.add(label("•  " + step, 12, INK, Font.PLAIN));
                panel.add(Box.createVerticalStrut(8));
            }
        }
        JButton timetable = secondaryButton("Open my timetable");
        timetable.addActionListener(event -> selectPage("Timetable"));
        JButton announcements = secondaryButton("View announcements");
        announcements.addActionListener(event -> selectPage("Announcements"));
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        actions.setOpaque(false);
        actions.add(timetable);
        actions.add(announcements);
        panel.add(actions);
        return panel;
    }

    private List<String> studentNextSteps(PortalData.Student student, List<String> modules) {
        List<String> steps = new ArrayList<String>();
        if (!"Active".equalsIgnoreCase(student.status)) {
            steps.add("Confirm your " + student.status.toLowerCase()
                    + " registration status with the Registrar's Office.");
        }
        if (modules.isEmpty()) {
            steps.add("Contact the Registrar's Office to confirm modules assigned to your programme.");
        }
        for (String code : modules) {
            List<Integer> marks = student.marks.get(code);
            int count = recordedCount(marks);
            if (count < 4) {
                steps.add(count == 0
                        ? "No assessment marks are recorded for " + code + "; check with your lecturer."
                        : "Some assessment marks are not yet recorded for " + code + ".");
            }
            if (count > 0 && moduleAverage(marks) < 50.0) {
                steps.add("Ask your lecturer about academic support for " + code + " (current average "
                        + String.format("%.1f%%", moduleAverage(marks)) + ").");
            }
        }
        LocalDate today = LocalDate.now();
        for (PortalData.Announcement announcement : data.announcements) {
            if (announcement.dueDate == null) continue;
            if (!announcement.dueDate.isAfter(today)) {
                steps.add((announcement.dueDate.isBefore(today) ? "Overdue" : "Due today") + ": "
                        + announcement.title + " (" + announcement.dueDate.format(DATE_FORMAT) + ").");
            } else if (!announcement.dueDate.isAfter(today.plusDays(7))) {
                steps.add("Upcoming deadline: " + announcement.title + " ("
                        + announcement.dueDate.format(DATE_FORMAT) + ").");
            }
        }
        return steps;
    }

    private int upcomingDeadlineCount() {
        LocalDate today = LocalDate.now();
        int count = 0;
        for (PortalData.Announcement announcement : data.announcements) {
            if (announcement.dueDate != null && !announcement.dueDate.isBefore(today)
                    && !announcement.dueDate.isAfter(today.plusDays(7))) {
                count++;
            }
        }
        return count;
    }

    private int recordedAssessmentCount(PortalData.Student student, List<String> modules) {
        int count = 0;
        for (String module : modules) count += recordedCount(student.marks.get(module));
        return count;
    }

    private int recordedCount(List<Integer> marks) {
        if (marks == null) return 0;
        int count = 0;
        for (Integer mark : marks) if (mark != null) count++;
        return count;
    }

    private double overallAverage(PortalData.Student student, List<String> modules) {
        int total = 0;
        int count = 0;
        for (String module : modules) {
            List<Integer> marks = student.marks.get(module);
            if (marks == null) continue;
            for (Integer mark : marks) {
                if (mark != null) {
                    total += mark;
                    count++;
                }
            }
        }
        return count == 0 ? 0.0 : total / (double) count;
    }

    private double moduleAverage(List<Integer> marks) {
        if (marks == null) return 0.0;
        int total = 0;
        int count = 0;
        for (Integer mark : marks) {
            if (mark != null) {
                total += mark;
                count++;
            }
        }
        return count == 0 ? 0.0 : total / (double) count;
    }

    private JPanel adminStudents() {
        JPanel page = verticalPanel();
        JButton add = primaryButton("Register student");
        add.addActionListener(event -> studentDialog(null));
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        actions.setOpaque(false);
        actions.add(add);
        page.add(actions);
        page.add(Box.createVerticalStrut(13));
        JPanel section = section("Student directory", studentTable(data.students, true));
        page.add(section);
        return page;
    }

    private JPanel studentTable(List<PortalData.Student> students, boolean editable) {
        String[] columns = editable
                ? new String[] { "Student number", "Student", "Email address", "Programme", "Status", "Portal account" }
                : new String[] { "Student number", "Student", "Email address", "Programme", "Status" };
        Object[][] rows = new Object[students.size()][columns.length];
        for (int i = 0; i < students.size(); i++) {
            PortalData.Student student = students.get(i);
            PortalData.Course course = data.course(student.courseCode);
            Object[] row = { student.number, student.fullName(), student.email,
                    course == null ? student.courseCode : course.name, student.status };
            if (editable) {
                rows[i] = Arrays.copyOf(row, columns.length);
                rows[i][5] = student.hasAccount()
                        ? (student.mustChangePassword ? "Password setup pending" : "Active")
                        : "Not created";
            } else {
                rows[i] = row;
            }
        }
        JTable table = table(columns, rows);
        table.putClientProperty("portal.students", students);
        JPanel wrapper = tablePanel(table);
        if (editable) {
            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 10));
            buttons.setOpaque(false);
            JButton edit = secondaryButton("Edit selected");
            JButton account = secondaryButton("Create / reset login");
            JButton remove = secondaryButton("Remove selected");
            account.addActionListener(event -> createOrResetStudentAccount(table));
            edit.addActionListener(event -> {
                PortalData.Student selected = selectedStudent(table);
                if (selected == null) return;
                studentDialog(selected);
            });
            remove.addActionListener(event -> {
                PortalData.Student selected = selectedStudent(table);
                if (selected == null) return;
                int answer = JOptionPane.showConfirmDialog(frame,
                        "Remove " + selected.fullName() + " from the student register?",
                        "Confirm removal", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (answer == JOptionPane.YES_OPTION) {
                    int selectedIndex = data.students.indexOf(selected);
                    List<PortalData.TimetableEntry> removedEntries = removeTimetableEntries(selected.id);
                    data.students.remove(selected);
                    if (!persistAndRefresh("Student record removed.")) {
                        data.students.add(selectedIndex, selected);
                        data.timetableEntries.addAll(removedEntries);
                    }
                }
            });
            buttons.add(edit);
            buttons.add(account);
            buttons.add(remove);
            wrapper.add(buttons, BorderLayout.SOUTH);
        }
        return wrapper;
    }

    private void createOrResetStudentAccount(JTable table) {
        PortalData.Student selected = selectedStudent(table);
        if (selected == null) return;
        if (selected.hasAccount()) {
            int answer = JOptionPane.showConfirmDialog(frame,
                    "Reset the password for " + selected.fullName() + "?\n"
                            + "Their current password will stop working and they must choose a new one at sign-in.",
                    "Reset student password", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (answer != JOptionPane.YES_OPTION) return;
        }
        char[] temporaryPassword = DatabaseStore.defaultStudentTemporaryPassword();
        try {
            data.provisionStudentAccount(selected, temporaryPassword);
            String issuedPassword = new String(temporaryPassword);
            JOptionPane.showMessageDialog(frame,
                    "Student account ready.\n\nUsername: " + selected.number
                            + "\nTemporary password: " + issuedPassword
                            + "\n\nThis default is shared by all student accounts until changed. "
                            + "Share it privately, and have the student choose a private password at first sign-in."
                            + (data.isSessionOnlyDemo() ? "\n\nDemo mode: this account is temporary for this session." : ""),
                    "Student login details", JOptionPane.INFORMATION_MESSAGE);
            selectPage(currentPage);
        } catch (SQLException error) {
            showMessage("The student account could not be saved.\n" + error.getMessage(),
                    JOptionPane.ERROR_MESSAGE);
        } finally {
            Arrays.fill(temporaryPassword, '\0');
        }
    }

    @SuppressWarnings("unchecked")
    private PortalData.Student selectedStudent(JTable table) {
        int row = table.getSelectedRow();
        if (row < 0) {
            showMessage("Select a student record first.", JOptionPane.INFORMATION_MESSAGE);
            return null;
        }
        List<PortalData.Student> students = (List<PortalData.Student>) table.getClientProperty("portal.students");
        return students.get(table.convertRowIndexToModel(row));
    }

    private void studentDialog(PortalData.Student existing) {
        JTextField firstName = field(existing == null ? "" : existing.firstName);
        JTextField surname = field(existing == null ? "" : existing.surname);
        JTextField email = field(existing == null ? "" : existing.email);
        JComboBox<String> course = combo(courseOptions());
        JComboBox<String> status = combo(new String[] { "Active", "Pending", "Inactive" });
        if (existing != null) {
            course.setSelectedItem(existing.courseCode);
            status.setSelectedItem(existing.status);
        }
        JPanel form = new JPanel(new GridLayout(0, 2, 10, 11));
        form.setBorder(BorderFactory.createEmptyBorder(10, 4, 8, 4));
        form.add(new JLabel("First name")); form.add(firstName);
        form.add(new JLabel("Surname")); form.add(surname);
        form.add(new JLabel("Email address")); form.add(email);
        form.add(new JLabel("Programme")); form.add(course);
        form.add(new JLabel("Status")); form.add(status);
        int answer = JOptionPane.showConfirmDialog(frame, form,
                existing == null ? "Register student" : "Edit student record",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (answer != JOptionPane.OK_OPTION) return;
        String first = firstName.getText().trim();
        String last = surname.getText().trim();
        String address = email.getText().trim();
        String selectedCourse = (String) course.getSelectedItem();
        String selectedStatus = (String) status.getSelectedItem();
        if (first.isEmpty() || last.isEmpty() || !address.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            showMessage("Enter a first name, surname and valid email address.", JOptionPane.WARNING_MESSAGE);
            return;
        }
        for (PortalData.Student student : data.students) {
            if (student != existing && student.email.equalsIgnoreCase(address)) {
                showMessage("That email address is already in the student register.", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }
        PortalData.Student created = null;
        String oldFirstName = existing == null ? null : existing.firstName;
        String oldSurname = existing == null ? null : existing.surname;
        String oldEmail = existing == null ? null : existing.email;
        String oldCourse = existing == null ? null : existing.courseCode;
        String oldStatus = existing == null ? null : existing.status;
        List<PortalData.TimetableEntry> removedEntries = new ArrayList<PortalData.TimetableEntry>();
        if (existing == null) {
            int id = data.nextStudentId();
            created = new PortalData.Student(id, data.studentNumber(id), first, last,
                    address, selectedCourse, selectedStatus, LocalDate.now().toString());
            data.students.add(created);
        } else {
            PortalData.Course replacementCourse = data.course(selectedCourse);
            List<String> replacementModules = replacementCourse == null
                    ? new ArrayList<String>() : replacementCourse.modules;
            for (PortalData.TimetableEntry entry : new ArrayList<PortalData.TimetableEntry>(data.timetableEntries)) {
                if (entry.studentId == existing.id && !replacementModules.contains(entry.moduleCode)) {
                    data.timetableEntries.remove(entry);
                    removedEntries.add(entry);
                }
            }
            existing.firstName = first;
            existing.surname = last;
            existing.email = address;
            existing.courseCode = selectedCourse;
            existing.status = selectedStatus;
        }
        if (!persistAndRefresh(existing == null ? "Student registered successfully." : "Student record updated.")) {
            if (created != null) {
                data.students.remove(created);
            } else {
                existing.firstName = oldFirstName;
                existing.surname = oldSurname;
                existing.email = oldEmail;
                existing.courseCode = oldCourse;
                existing.status = oldStatus;
                data.timetableEntries.addAll(removedEntries);
            }
        }
    }

    private List<PortalData.TimetableEntry> removeTimetableEntries(int studentId) {
        List<PortalData.TimetableEntry> removed = new ArrayList<PortalData.TimetableEntry>();
        for (PortalData.TimetableEntry entry : new ArrayList<PortalData.TimetableEntry>(data.timetableEntries)) {
            if (entry.studentId == studentId) {
                data.timetableEntries.remove(entry);
                removed.add(entry);
            }
        }
        return removed;
    }

    private JPanel coursesPage() {
        JPanel page = verticalPanel();
        Object[][] rows = new Object[data.courses.size()][4];
        for (int i = 0; i < data.courses.size(); i++) {
            PortalData.Course course = data.courses.get(i);
            rows[i] = new Object[] { course.code, course.name, course.duration, course.modules.size() };
        }
        JTable courseTable = table(new String[] { "Course code", "Programme name", "Duration", "Modules" }, rows);
        courseTable.putClientProperty("portal.courses", data.courses);
        JPanel courseDirectory = tablePanel(courseTable);
        JPanel courseActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 10));
        courseActions.setOpaque(false);
        JButton addCourse = primaryButton("Add course");
        JButton assignModules = secondaryButton("Assign modules to selected course");
        addCourse.addActionListener(event -> courseDialog());
        assignModules.addActionListener(event -> assignModulesDialog(courseTable));
        courseActions.add(addCourse);
        courseActions.add(assignModules);
        courseDirectory.add(courseActions, BorderLayout.SOUTH);
        page.add(section("Academic programmes", courseDirectory));
        page.add(Box.createVerticalStrut(14));

        Object[][] moduleRows = new Object[data.modules.size()][4];
        for (int i = 0; i < data.modules.size(); i++) {
            PortalData.Module module = data.modules.get(i);
            moduleRows[i] = new Object[] { module.code, module.name, lecturerFor(module.code),
                    coursesForModule(module.code) };
        }
        JTable moduleTable = table(new String[] { "Module code", "Module name", "Lecturer", "Courses" }, moduleRows);
        moduleTable.putClientProperty("portal.modules", data.modules);
        JPanel moduleDirectory = tablePanel(moduleTable);
        JPanel moduleActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 10));
        moduleActions.setOpaque(false);
        JButton addModule = primaryButton("Create module");
        JButton assignLecturer = secondaryButton("Assign lecturer to selected module");
        addModule.addActionListener(event -> moduleDialog());
        assignLecturer.addActionListener(event -> assignLecturerDialog(moduleTable));
        moduleActions.add(addModule);
        moduleActions.add(assignLecturer);
        moduleDirectory.add(moduleActions, BorderLayout.SOUTH);
        page.add(section("Curriculum modules", moduleDirectory));
        page.add(Box.createVerticalStrut(14));
        page.add(notePanel("Curriculum rules",
                "A course can include many modules, and a module can be shared by multiple courses. Each module has at most one assigned lecturer."));
        return page;
    }

    private void courseDialog() {
        JTextField codeField = field("");
        JTextField nameField = field("");
        JTextField durationField = field("3 years");
        JPanel form = new JPanel(new GridLayout(0, 2, 10, 11));
        form.setBorder(BorderFactory.createEmptyBorder(10, 4, 8, 4));
        form.add(new JLabel("Course code")); form.add(codeField);
        form.add(new JLabel("Course name")); form.add(nameField);
        form.add(new JLabel("Duration")); form.add(durationField);
        int answer = JOptionPane.showConfirmDialog(frame, form, "Add course",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (answer != JOptionPane.OK_OPTION) return;

        String code = codeField.getText().trim().toUpperCase();
        String name = nameField.getText().trim();
        String duration = durationField.getText().trim();
        if (!validCode(code) || name.isEmpty() || name.length() > 180
                || duration.isEmpty() || duration.length() > 40) {
            showMessage("Enter a valid course code, name and duration.", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (data.course(code) != null) {
            showMessage("That course code is already in use.", JOptionPane.WARNING_MESSAGE);
            return;
        }
        PortalData.Course course = new PortalData.Course(code, name, duration);
        data.courses.add(course);
        if (!persistAndRefresh("Course added. Assign modules to complete its curriculum.")) {
            data.courses.remove(course);
        }
    }

    private void moduleDialog() {
        JTextField codeField = field("");
        JTextField nameField = field("");
        String[] courseOptions = new String[data.courses.size()];
        for (int i = 0; i < data.courses.size(); i++) {
            PortalData.Course course = data.courses.get(i);
            courseOptions[i] = course.code + " · " + course.name;
        }
        JList<String> courseList = new JList<String>(courseOptions);
        courseList.setVisibleRowCount(Math.min(6, Math.max(3, courseOptions.length)));
        courseList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane courseScroll = new JScrollPane(courseList);
        courseScroll.setPreferredSize(new Dimension(320, 112));
        JComboBox<String> lecturerBox = combo(lecturerOptions());

        JPanel form = new JPanel(new GridLayout(0, 2, 10, 11));
        form.setBorder(BorderFactory.createEmptyBorder(10, 4, 8, 4));
        form.add(new JLabel("Module code")); form.add(codeField);
        form.add(new JLabel("Module name")); form.add(nameField);
        form.add(new JLabel("Courses (select one or more)")); form.add(courseScroll);
        form.add(new JLabel("Lecturer")); form.add(lecturerBox);
        int answer = JOptionPane.showConfirmDialog(frame, form, "Create module",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (answer != JOptionPane.OK_OPTION) return;

        String code = codeField.getText().trim().toUpperCase();
        String name = nameField.getText().trim();
        if (!validCode(code) || name.isEmpty() || name.length() > 180) {
            showMessage("Enter a valid module code and name.", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (data.module(code) != null) {
            showMessage("That module code is already in use.", JOptionPane.WARNING_MESSAGE);
            return;
        }
        List<String> selectedCourses = courseList.getSelectedValuesList();
        if (selectedCourses.isEmpty()) {
            showMessage("Select at least one course for this module.", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Integer lecturerId = lecturerIdForOption((String) lecturerBox.getSelectedItem());
        PortalData.Module module = new PortalData.Module(code, name, lecturerId);
        data.modules.add(module);
        List<PortalData.Course> linkedCourses = new ArrayList<PortalData.Course>();
        for (String selected : selectedCourses) {
            PortalData.Course course = data.course(moduleCode(selected));
            if (course != null) {
                course.modules.add(code);
                linkedCourses.add(course);
            }
        }
        data.synchronizeLecturerModules();
        if (!persistAndRefresh("Module created and assigned to selected courses.")) {
            data.modules.remove(module);
            for (PortalData.Course course : linkedCourses) course.modules.remove(code);
            data.synchronizeLecturerModules();
        }
    }

    private void assignModulesDialog(JTable courseTable) {
        PortalData.Course course = selectedCourse(courseTable);
        if (course == null) return;
        if (data.modules.isEmpty()) {
            showMessage("Create a module before assigning modules to a course.", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String[] options = new String[data.modules.size()];
        for (int i = 0; i < data.modules.size(); i++) {
            PortalData.Module module = data.modules.get(i);
            options[i] = module.code + " · " + module.name;
        }
        JList<String> moduleList = new JList<String>(options);
        moduleList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        moduleList.setVisibleRowCount(Math.min(8, Math.max(3, options.length)));
        for (int i = 0; i < data.modules.size(); i++) {
            if (course.modules.contains(data.modules.get(i).code)) moduleList.addSelectionInterval(i, i);
        }
        int answer = JOptionPane.showConfirmDialog(frame, new JScrollPane(moduleList),
                "Assign modules to " + course.code, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (answer != JOptionPane.OK_OPTION) return;
        List<String> added = new ArrayList<String>();
        for (String option : moduleList.getSelectedValuesList()) {
            String code = moduleCode(option);
            if (!course.modules.contains(code)) {
                course.modules.add(code);
                added.add(code);
            }
        }
        if (added.isEmpty()) {
            showMessage("No new modules were selected.", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (!persistAndRefresh("Modules assigned to " + course.code + ".")) {
            course.modules.removeAll(added);
        }
    }

    private void assignLecturerDialog(JTable moduleTable) {
        int row = moduleTable.getSelectedRow();
        if (row < 0) {
            showMessage("Select a module from the curriculum first.", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        @SuppressWarnings("unchecked")
        List<PortalData.Module> modules =
                (List<PortalData.Module>) moduleTable.getClientProperty("portal.modules");
        PortalData.Module module = modules.get(moduleTable.convertRowIndexToModel(row));
        JComboBox<String> lecturerBox = combo(lecturerOptions());
        lecturerBox.setSelectedItem(lecturerOption(module.lecturerId));
        JPanel form = new JPanel(new GridLayout(0, 2, 10, 11));
        form.setBorder(BorderFactory.createEmptyBorder(10, 4, 8, 4));
        form.add(new JLabel("Module")); form.add(new JLabel(module.code + " · " + module.name));
        form.add(new JLabel("Lecturer")); form.add(lecturerBox);
        int answer = JOptionPane.showConfirmDialog(frame, form, "Assign module lecturer",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (answer != JOptionPane.OK_OPTION) return;
        Integer previousId = module.lecturerId;
        module.lecturerId = lecturerIdForOption((String) lecturerBox.getSelectedItem());
        data.synchronizeLecturerModules();
        if (!persistAndRefresh("Module lecturer assignment updated.")) {
            module.lecturerId = previousId;
            data.synchronizeLecturerModules();
        }
    }

    private String[] lecturerOptions() {
        String[] options = new String[data.lecturers.size() + 1];
        options[0] = "Unassigned";
        for (int i = 0; i < data.lecturers.size(); i++) {
            PortalData.Lecturer lecturer = data.lecturers.get(i);
            options[i + 1] = lecturer.id + " · " + lecturer.fullName();
        }
        return options;
    }

    private Integer lecturerIdForOption(String option) {
        if (option == null || "Unassigned".equals(option)) return null;
        int separator = option.indexOf(" · ");
        if (separator < 0) return null;
        return Integer.valueOf(option.substring(0, separator));
    }

    private String lecturerOption(Integer lecturerId) {
        if (lecturerId == null) return "Unassigned";
        for (PortalData.Lecturer lecturer : data.lecturers) {
            if (lecturer.id == lecturerId) return lecturer.id + " · " + lecturer.fullName();
        }
        return "Unassigned";
    }

    private PortalData.Course selectedCourse(JTable table) {
        int row = table.getSelectedRow();
        if (row < 0) {
            showMessage("Select a course from the programme list first.", JOptionPane.INFORMATION_MESSAGE);
            return null;
        }
        @SuppressWarnings("unchecked")
        List<PortalData.Course> courses = (List<PortalData.Course>) table.getClientProperty("portal.courses");
        return courses.get(table.convertRowIndexToModel(row));
    }

    private String coursesForModule(String moduleCode) {
        List<String> courses = new ArrayList<String>();
        for (PortalData.Course course : data.courses) {
            if (course.modules.contains(moduleCode)) courses.add(course.code);
        }
        return String.join(", ", courses);
    }

    private boolean validCode(String code) {
        return code.matches("[A-Z0-9][A-Z0-9_-]{1,23}");
    }

    private JPanel lecturersPage() {
        JPanel page = verticalPanel();
        Object[][] rows = new Object[data.lecturers.size()][4];
        for (int i = 0; i < data.lecturers.size(); i++) {
            PortalData.Lecturer lecturer = data.lecturers.get(i);
            rows[i] = new Object[] { lecturer.fullName(), lecturer.email, lecturer.modules.size(), String.join(", ", lecturer.modules) };
        }
        JTable lecturerTable = table(new String[] { "Lecturer", "Email address", "Modules", "Teaching allocation" }, rows);
        lecturerTable.putClientProperty("portal.lecturers", data.lecturers);
        JPanel directory = tablePanel(lecturerTable);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 10));
        actions.setOpaque(false);
        JButton add = primaryButton("Add lecturer");
        JButton remove = secondaryButton("Delete selected");
        add.addActionListener(event -> lecturerDialog());
        remove.addActionListener(event -> {
            int row = lecturerTable.getSelectedRow();
            if (row < 0) {
                showMessage("Select a lecturer from the directory first.", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            @SuppressWarnings("unchecked")
            List<PortalData.Lecturer> lecturers =
                    (List<PortalData.Lecturer>) lecturerTable.getClientProperty("portal.lecturers");
            PortalData.Lecturer selected = lecturers.get(lecturerTable.convertRowIndexToModel(row));
            if (!selected.modules.isEmpty()) {
                showMessage("Reassign or unassign this lecturer's modules before deleting the lecturer.",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            int answer = JOptionPane.showConfirmDialog(frame,
                    "Delete " + selected.fullName() + " from the faculty directory?",
                    "Confirm lecturer deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (answer == JOptionPane.YES_OPTION) {
                int selectedIndex = lecturers.indexOf(selected);
                data.lecturers.remove(selected);
                if (!persistAndRefresh("Lecturer deleted from the faculty directory.")) {
                    data.lecturers.add(selectedIndex, selected);
                }
            }
        });
        actions.add(add);
        actions.add(remove);
        directory.add(actions, BorderLayout.SOUTH);
        page.add(section("Faculty directory", directory));
        page.add(Box.createVerticalStrut(12));
        page.add(notePanel("Lecturer accounts", "Assign lecturers to modules from Courses. The demo sign-in remains limited to the built-in lecturer account."));
        return page;
    }

    private void lecturerDialog() {
        JTextField firstName = field("");
        JTextField surname = field("");
        JTextField email = field("");

        JPanel form = new JPanel(new GridLayout(0, 2, 10, 11));
        form.setBorder(BorderFactory.createEmptyBorder(10, 4, 8, 4));
        form.add(new JLabel("First name")); form.add(firstName);
        form.add(new JLabel("Surname")); form.add(surname);
        form.add(new JLabel("Email address")); form.add(email);
        int answer = JOptionPane.showConfirmDialog(frame, form, "Add lecturer",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (answer != JOptionPane.OK_OPTION) return;

        String first = firstName.getText().trim();
        String last = surname.getText().trim();
        String address = email.getText().trim();
        if (first.isEmpty() || last.isEmpty() || !address.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            showMessage("Enter a first name, surname and valid email address.", JOptionPane.WARNING_MESSAGE);
            return;
        }
        for (PortalData.Lecturer lecturer : data.lecturers) {
            if (lecturer.email.equalsIgnoreCase(address)) {
                showMessage("That email address is already in the faculty directory.", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }
        PortalData.Lecturer lecturer = new PortalData.Lecturer(data.nextLecturerId(), first, last, address);
        data.lecturers.add(lecturer);
        if (!persistAndRefresh("Lecturer added to the faculty directory.")) {
            data.lecturers.remove(lecturer);
        }
    }

    private JPanel lecturerStudents() {
        JPanel page = verticalPanel();
        PortalData.Lecturer lecturer = data.lecturer(user.id);
        page.add(section("Enrolled students", studentTable(studentsFor(lecturer.modules), false)));
        page.add(Box.createVerticalStrut(14));
        page.add(section("My teaching modules", moduleTable(lecturer.modules)));
        return page;
    }

    private JPanel captureMarks() {
        JPanel page = verticalPanel();
        PortalData.Lecturer lecturer = data.lecturer(user.id);
        List<PortalData.Student> assignedStudents = studentsFor(lecturer.modules);
        if (assignedStudents.isEmpty()) {
            page.add(notePanel("No students to assess", "There are no enrolled students in your assigned modules."));
            return page;
        }
        JComboBox<String> studentBox = combo(studentOptions(assignedStudents));
        JComboBox<String> moduleBox = combo(lecturer.modules.toArray(new String[0]));
        JTextField[] scoreFields = new JTextField[4];
        JPanel form = new JPanel(new GridLayout(0, 2, 12, 11));
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        form.add(new JLabel("Student")); form.add(studentBox);
        form.add(new JLabel("Module")); form.add(moduleBox);
        for (int i = 0; i < 4; i++) {
            form.add(new JLabel("Assessment " + (i + 1) + " (0–100)"));
            scoreFields[i] = field("");
            form.add(scoreFields[i]);
        }
        Runnable loadScores = () -> {
            PortalData.Student selected = studentByOption((String) studentBox.getSelectedItem());
            String module = moduleCode((String) moduleBox.getSelectedItem());
            List<Integer> marks = selected == null ? null : selected.marks.get(module);
            for (int i = 0; i < scoreFields.length; i++) {
                scoreFields[i].setText(marks != null && marks.size() > i ? String.valueOf(marks.get(i)) : "");
            }
        };
        studentBox.addActionListener(event -> loadScores.run());
        moduleBox.addActionListener(event -> loadScores.run());
        JButton save = primaryButton("Save assessment marks");
        save.addActionListener(event -> {
            PortalData.Student selected = studentByOption((String) studentBox.getSelectedItem());
            String module = moduleCode((String) moduleBox.getSelectedItem());
            if (selected == null || !lecturer.modules.contains(module)
                    || !data.course(selected.courseCode).modules.contains(module)) {
                showMessage("Choose a student enrolled in one of your assigned modules.", JOptionPane.WARNING_MESSAGE);
                return;
            }
            List<Integer> scores = new ArrayList<Integer>();
            for (JTextField scoreField : scoreFields) {
                String value = scoreField.getText().trim();
                if (value.isEmpty()) {
                    scores.add(null);
                } else {
                    try {
                        int score = Integer.parseInt(value);
                        if (score < 0 || score > 100) throw new NumberFormatException();
                        scores.add(score);
                    } catch (NumberFormatException error) {
                        showMessage("Assessment marks must be whole numbers from 0 to 100. Leave a field blank if it has not been marked.", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                }
            }
            selected.marks.put(module, scores);
            persistAndRefresh("Assessment marks saved.");
        });
        JPanel section = section("Record module results", form);
        section.add(leftAligned(save), BorderLayout.SOUTH);
        page.add(section);
        loadScores.run();
        return page;
    }

    private JPanel studentModules() {
        JPanel page = verticalPanel();
        PortalData.Student student = data.student(user.id);
        PortalData.Course course = data.course(student.courseCode);
        List<String> modules = course == null ? new ArrayList<String>() : course.modules;
        page.add(section("Registered modules", moduleTable(modules)));
        return page;
    }

    private JPanel studentTimetable() {
        JPanel page = verticalPanel();
        PortalData.Student student = data.student(user.id);
        PortalData.Course course = data.course(student.courseCode);
        List<String> modules = course == null ? new ArrayList<String>() : course.modules;
        List<PortalData.TimetableEntry> entries = new ArrayList<PortalData.TimetableEntry>();
        for (PortalData.TimetableEntry entry : data.timetableEntries) {
            if (entry.studentId == student.id) entries.add(entry);
        }
        entries.sort(Comparator.comparing((PortalData.TimetableEntry entry) -> entry.day)
                .thenComparing(entry -> entry.start));
        Object[][] rows = new Object[entries.size()][5];
        for (int i = 0; i < entries.size(); i++) {
            PortalData.TimetableEntry entry = entries.get(i);
            rows[i] = new Object[] { entry.day.getDisplayName(TextStyle.FULL, Locale.getDefault()),
                    entry.start.format(TIME_FORMAT),
                    entry.end.format(TIME_FORMAT), entry.moduleCode, entry.location };
        }
        JTable timetable = table(new String[] { "Day", "Start", "End", "Module", "Room / location" }, rows);
        timetable.getAccessibleContext().setAccessibleName("My weekly timetable");
        timetable.putClientProperty("portal.timetable.entries", entries);
        JPanel content = tablePanel(timetable);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 10));
        actions.setOpaque(false);
        JButton add = primaryButton("Add class");
        add.setToolTipText("Add a weekly class slot and check it for overlaps.");
        add.addActionListener(event -> timetableEntryDialog(student, modules));
        JButton remove = secondaryButton("Remove selected");
        remove.addActionListener(event -> removeTimetableEntry(timetable));
        actions.add(add);
        actions.add(remove);
        content.add(actions, BorderLayout.SOUTH);
        page.add(section("My weekly timetable", content));
        page.add(Box.createVerticalStrut(12));
        page.add(notePanel("Clash checking",
                "A class is rejected if it overlaps another entry on the same day. Adjacent classes are allowed. "
                        + "Times use the 24-hour HH:mm format."));
        return page;
    }

    @SuppressWarnings("unchecked")
    private void removeTimetableEntry(JTable timetable) {
        int row = timetable.getSelectedRow();
        if (row < 0) {
            showMessage("Select a timetable entry first.", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        List<PortalData.TimetableEntry> entries =
                (List<PortalData.TimetableEntry>) timetable.getClientProperty("portal.timetable.entries");
        PortalData.TimetableEntry selected = entries.get(timetable.convertRowIndexToModel(row));
        data.timetableEntries.remove(selected);
        if (!persistAndRefresh("Class removed from your timetable.")) {
            data.timetableEntries.add(selected);
        }
    }

    private void timetableEntryDialog(PortalData.Student student, List<String> modules) {
        if (modules.isEmpty()) {
            showMessage("Your programme has no modules to schedule yet.", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String[] moduleOptions = new String[modules.size()];
        for (int i = 0; i < modules.size(); i++) {
            String code = modules.get(i);
            moduleOptions[i] = code + " · " + data.moduleName(code);
        }
        JComboBox<String> module = combo(moduleOptions);
        JComboBox<String> day = combo(new String[] {
            "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"
        });
        JTextField start = field("");
        start.setToolTipText("24-hour time, for example 09:00");
        JTextField end = field("");
        end.setToolTipText("24-hour time, for example 10:30");
        JTextField location = field("");
        JPanel form = new JPanel(new GridLayout(0, 2, 10, 11));
        form.setBorder(BorderFactory.createEmptyBorder(10, 4, 8, 4));
        form.add(new JLabel("Module")); form.add(module);
        form.add(new JLabel("Day")); form.add(day);
        form.add(new JLabel("Start (HH:mm)")); form.add(start);
        form.add(new JLabel("End (HH:mm)")); form.add(end);
        form.add(new JLabel("Room / location")); form.add(location);
        int answer = JOptionPane.showConfirmDialog(frame, form, "Add weekly class",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (answer != JOptionPane.OK_OPTION) return;

        LocalTime startTime;
        LocalTime endTime;
        try {
            startTime = LocalTime.parse(start.getText().trim(), TIME_FORMAT);
            endTime = LocalTime.parse(end.getText().trim(), TIME_FORMAT);
        } catch (DateTimeException error) {
            showMessage("Enter valid 24-hour times such as 09:00 and 10:30.", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!startTime.isBefore(endTime)) {
            showMessage("The end time must be after the start time.", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String moduleCode = moduleCode((String) module.getSelectedItem());
        DayOfWeek selectedDay = DayOfWeek.valueOf((String) day.getSelectedItem());
        for (PortalData.TimetableEntry existing : data.timetableEntries) {
            if (existing.studentId == student.id && existing.day == selectedDay
                    && startTime.isBefore(existing.end) && endTime.isAfter(existing.start)) {
                showMessage("This class overlaps " + existing.moduleCode + " ("
                                + existing.start.format(TIME_FORMAT) + "–"
                                + existing.end.format(TIME_FORMAT) + "). Choose another time.",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
        }
        String room = location.getText().trim();
        if (room.length() > 120) {
            showMessage("The room or location must be 120 characters or fewer.", JOptionPane.WARNING_MESSAGE);
            return;
        }
        PortalData.TimetableEntry entry = new PortalData.TimetableEntry(student.id, moduleCode,
                selectedDay, startTime, endTime, room);
        data.timetableEntries.add(entry);
        if (!persistAndRefresh("Class added to your timetable.")) {
            data.timetableEntries.remove(entry);
        }
    }

    private JPanel announcementsPage(boolean editable) {
        JPanel page = verticalPanel();
        JPanel content = verticalPanel();
        List<PortalData.Announcement> announcements = new ArrayList<PortalData.Announcement>(data.announcements);
        announcements.sort(Comparator.comparing((PortalData.Announcement item) -> item.publishedOn)
                .thenComparingInt(item -> item.id).reversed());
        if (announcements.isEmpty()) {
            content.add(label("There are no announcements yet.", 12, MUTED, Font.PLAIN));
        } else {
            for (PortalData.Announcement announcement : announcements) {
                content.add(announcementCard(announcement, editable));
                content.add(Box.createVerticalStrut(10));
            }
        }
        if (editable) {
            JButton add = primaryButton("Publish announcement");
            add.addActionListener(event -> announcementDialog());
            page.add(leftAligned(add));
            page.add(Box.createVerticalStrut(13));
        }
        page.add(section("Campus announcements", content));
        return page;
    }

    private JPanel announcementCard(PortalData.Announcement announcement, boolean editable) {
        JPanel card = new JPanel(new BorderLayout(8, 8));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(LINE),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)));
        JPanel heading = new JPanel(new BorderLayout(8, 0));
        heading.setOpaque(false);
        heading.add(label(announcement.title, 13, NAVY, Font.BOLD), BorderLayout.CENTER);
        String posted = "Posted " + announcement.publishedOn.format(DATE_FORMAT);
        if (announcement.dueDate != null) posted += "  ·  Due " + announcement.dueDate.format(DATE_FORMAT);
        heading.add(label(posted, 10, MUTED, Font.PLAIN), BorderLayout.EAST);
        card.add(heading, BorderLayout.NORTH);
        JTextArea message = new JTextArea(announcement.message,
                Math.max(2, Math.min(22, (announcement.message.length() + 54) / 55)), 60);
        message.setEditable(false);
        message.setLineWrap(true);
        message.setWrapStyleWord(true);
        message.setFont(BODY);
        message.setForeground(INK);
        message.setOpaque(false);
        message.getAccessibleContext().setAccessibleName("Announcement details");
        card.add(message, BorderLayout.CENTER);
        if (editable) {
            JButton remove = secondaryButton("Remove");
            remove.getAccessibleContext().setAccessibleName("Remove announcement: " + announcement.title);
            remove.addActionListener(event -> removeAnnouncement(announcement));
            card.add(remove, BorderLayout.EAST);
        }
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        return card;
    }

    private void announcementDialog() {
        JTextField title = field("");
        JTextArea message = new JTextArea(5, 28);
        message.setLineWrap(true);
        message.setWrapStyleWord(true);
        JTextField dueDate = field("");
        dueDate.setToolTipText("Optional deadline in YYYY-MM-DD format.");
        JPanel form = new JPanel(new GridLayout(0, 1, 8, 8));
        form.setBorder(BorderFactory.createEmptyBorder(10, 4, 8, 4));
        form.add(new JLabel("Title"));
        form.add(title);
        form.add(new JLabel("Message"));
        form.add(new JScrollPane(message));
        form.add(new JLabel("Deadline (optional, YYYY-MM-DD)"));
        form.add(dueDate);
        int answer = JOptionPane.showConfirmDialog(frame, form, "Publish announcement",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (answer != JOptionPane.OK_OPTION) return;
        String announcementTitle = title.getText().trim();
        String announcementMessage = message.getText().trim();
        String dueText = dueDate.getText().trim();
        if (announcementTitle.isEmpty() || announcementTitle.length() > 160
                || announcementMessage.isEmpty() || announcementMessage.length() > 1000) {
            showMessage("Enter a title (up to 160 characters) and message (up to 1,000 characters).",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        LocalDate due = null;
        if (!dueText.isEmpty()) {
            try {
                due = LocalDate.parse(dueText, DATE_FORMAT);
            } catch (DateTimeParseException error) {
                showMessage("Enter the deadline in YYYY-MM-DD format, or leave it blank.",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
        }
        PortalData.Announcement announcement = new PortalData.Announcement(
                data.nextAnnouncementId(), announcementTitle, announcementMessage, LocalDate.now(), due);
        data.announcements.add(announcement);
        if (!persistAndRefresh("Announcement published.")) {
            data.announcements.remove(announcement);
        }
    }

    private void removeAnnouncement(PortalData.Announcement announcement) {
        int answer = JOptionPane.showConfirmDialog(frame,
                "Remove the announcement \"" + announcement.title + "\"?",
                "Remove announcement", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (answer != JOptionPane.YES_OPTION) return;
        int index = data.announcements.indexOf(announcement);
        data.announcements.remove(announcement);
        if (!persistAndRefresh("Announcement removed.")) {
            data.announcements.add(index, announcement);
        }
    }

    private JPanel studentResults() {
        JPanel page = verticalPanel();
        PortalData.Student student = data.student(user.id);
        PortalData.Course course = data.course(student.courseCode);
        List<String> modules = course == null ? new ArrayList<String>() : course.modules;
        page.add(section("Assessment results", resultsTable(student, modules)));
        page.add(Box.createVerticalStrut(12));
        page.add(notePanel("Assessment scale", "A module average of 50% or above is shown as a pass. Blank assessments have not yet been recorded."));
        return page;
    }

    private JPanel studentProfile() {
        PortalData.Student student = data.student(user.id);
        PortalData.Course course = data.course(student.courseCode);
        JPanel details = new JPanel(new GridLayout(0, 2, 14, 14));
        details.setBackground(Color.WHITE);
        details.setBorder(BorderFactory.createEmptyBorder(19, 20, 19, 20));
        addDetail(details, "Student number", student.number);
        addDetail(details, "Full name", student.fullName());
        addDetail(details, "Email address", student.email);
        addDetail(details, "Programme", course == null ? student.courseCode : course.name);
        addDetail(details, "Registration status", student.status);
        addDetail(details, "Enrolment date", student.enrollmentDate);
        JPanel page = verticalPanel();
        page.add(section("Student profile", details));
        page.add(Box.createVerticalStrut(12));
        JButton changePassword = secondaryButton("Change my password");
        changePassword.addActionListener(event -> changeStudentPasswordDialog(student, false));
        page.add(leftAligned(changePassword));
        return page;
    }

    private boolean changeStudentPasswordDialog(PortalData.Student student, boolean firstLogin) {
        JPasswordField newPassword = new CopyablePasswordField();
        JPasswordField confirmPassword = new CopyablePasswordField();
        styleField(newPassword);
        styleField(confirmPassword);
        JPanel form = new JPanel(new GridLayout(0, 1, 8, 8));
        form.setBorder(BorderFactory.createEmptyBorder(10, 4, 8, 4));
        form.add(new JLabel("New password (at least 12 characters)"));
        form.add(newPassword);
        form.add(new JLabel("Confirm new password"));
        form.add(confirmPassword);
        int answer = JOptionPane.showConfirmDialog(frame, form,
                firstLogin ? "Set your student password" : "Change your password",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (answer != JOptionPane.OK_OPTION) {
            if (firstLogin) showMessage("Set your password before continuing to the student workspace.",
                    JOptionPane.INFORMATION_MESSAGE);
            return false;
        }
        char[] next = newPassword.getPassword();
        char[] confirmation = confirmPassword.getPassword();
        try {
            if (next.length < 12) {
                showMessage("Use at least 12 characters for your password.", JOptionPane.WARNING_MESSAGE);
                return false;
            }
            if (!Arrays.equals(next, confirmation)) {
                showMessage("The passwords do not match. Please try again.", JOptionPane.WARNING_MESSAGE);
                return false;
            }
            data.changeStudentPassword(student, next);
            if (!firstLogin) {
                showMessage("Your password has been changed.", JOptionPane.INFORMATION_MESSAGE);
                selectPage(currentPage);
            }
            return true;
        } catch (SQLException | IllegalArgumentException error) {
            showMessage("Your password could not be saved.\n" + error.getMessage(), JOptionPane.ERROR_MESSAGE);
            return false;
        } finally {
            Arrays.fill(next, '\0');
            Arrays.fill(confirmation, '\0');
            newPassword.setText("");
            confirmPassword.setText("");
        }
    }

    private JPanel moduleTable(List<String> modules) {
        Object[][] rows = new Object[modules.size()][3];
        for (int i = 0; i < modules.size(); i++) {
            String code = modules.get(i);
            rows[i] = new Object[] { code, data.moduleName(code), lecturerFor(code) };
        }
        return tablePanel(table(new String[] { "Module code", "Module name", "Lecturer" }, rows));
    }

    private JPanel resultsTable(PortalData.Student student, List<String> modules) {
        Object[][] rows = new Object[modules.size()][8];
        for (int i = 0; i < modules.size(); i++) {
            String code = modules.get(i);
            List<Integer> scores = student.marks.get(code);
            Integer[] values = new Integer[4];
            int total = 0;
            int count = 0;
            for (int assessment = 0; assessment < 4; assessment++) {
                Integer value = scores != null && scores.size() > assessment ? scores.get(assessment) : null;
                values[assessment] = value;
                if (value != null) { total += value; count++; }
            }
            double average = count == 0 ? 0.0 : total / (double) count;
            String progress = count == 0 ? "No marks yet"
                    : average < 50.0 ? "Below 50% benchmark" : "On track";
            rows[i] = new Object[] { code, data.moduleName(code),
                    scoreText(values[0]), scoreText(values[1]), scoreText(values[2]), scoreText(values[3]),
                    count == 0 ? "—" : String.format("%.1f%% · %s", average, progress),
                    assessmentTrend(scores)};
        }
        return tablePanel(table(new String[] { "Module", "Module name", "Assessment 1", "Assessment 2",
                "Assessment 3", "Assessment 4", "Average / progress", "Trend" }, rows));
    }

    private String assessmentTrend(List<Integer> scores) {
        if (scores == null) return "Not enough data";
        int firstIndex = -1;
        int lastIndex = -1;
        for (int i = 0; i < scores.size(); i++) {
            if (scores.get(i) != null) {
                if (firstIndex < 0) firstIndex = i;
                lastIndex = i;
            }
        }
        if (firstIndex < 0 || firstIndex == lastIndex) return "Not enough data";
        int change = scores.get(lastIndex) - scores.get(firstIndex);
        return (change > 0 ? "+" : "") + change + " pts vs A" + (firstIndex + 1);
    }

    private String scoreText(Integer score) {
        return score == null ? "—" : score + "%";
    }

    private String lecturerFor(String module) {
        for (PortalData.Lecturer lecturer : data.lecturers) {
            if (lecturer.modules.contains(module)) return lecturer.fullName();
        }
        return "Unassigned";
    }

    private List<PortalData.Student> studentsFor(List<String> modules) {
        List<PortalData.Student> results = new ArrayList<PortalData.Student>();
        for (PortalData.Student student : data.students) {
            PortalData.Course course = data.course(student.courseCode);
            if (course != null && course.modules.stream().anyMatch(modules::contains)) results.add(student);
        }
        return results;
    }

    private int countMarkedStudents(List<String> modules) {
        int count = 0;
        for (PortalData.Student student : studentsFor(modules)) {
            for (String module : modules) {
                List<Integer> scores = student.marks.get(module);
                if (scores != null && scores.stream().anyMatch(score -> score != null)) { count++; break; }
            }
        }
        return count;
    }

    private int countStatus(String status) {
        int count = 0;
        for (PortalData.Student student : data.students) if (status.equals(student.status)) count++;
        return count;
    }

    private String[] courseOptions() {
        String[] options = new String[data.courses.size()];
        for (int i = 0; i < data.courses.size(); i++) options[i] = data.courses.get(i).code;
        return options;
    }

    private String[] studentOptions(List<PortalData.Student> students) {
        String[] options = new String[students.size()];
        for (int i = 0; i < students.size(); i++) {
            PortalData.Student student = students.get(i);
            options[i] = student.number + "  ·  " + student.fullName();
        }
        return options;
    }

    private PortalData.Student studentByOption(String option) {
        if (option == null) return null;
        for (PortalData.Student student : data.students) {
            if (option.startsWith(student.number)) return student;
        }
        return null;
    }

    private String moduleCode(String selected) {
        if (selected == null) return "";
        return selected.contains(" · ") ? selected.substring(0, selected.indexOf(" · ")) : selected;
    }

    private boolean persistAndRefresh(String successMessage) {
        try {
            data.save();
            showMessage(data.isSessionOnlyDemo()
                            ? successMessage + "\n\nDemo mode: this change is temporary and will not be saved after closing."
                            : successMessage,
                    JOptionPane.INFORMATION_MESSAGE);
            selectPage(currentPage);
            return true;
        } catch (SQLException error) {
            showMessage("The change could not be saved to this computer.\n" + error.getMessage(),
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private void showMessage(String message, int type) {
        JOptionPane.showMessageDialog(frame, message, "University of Jarateng", type);
    }

    private JPanel pageHeading(String title, String description) {
        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        heading.add(label(title, 25, NAVY, Font.BOLD));
        heading.add(Box.createVerticalStrut(6));
        heading.add(label(description, 13, MUTED, Font.PLAIN));
        return heading;
    }

    private JPanel statRow(JPanel... cards) {
        JPanel row = new JPanel(new GridLayout(0, 2, 13, 13));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 237));
        for (JPanel card : cards) row.add(card);
        return row;
    }

    private JPanel statCard(String title, String value, String detail, String index) {
        JPanel card = new JPanel();
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(LINE),
                BorderFactory.createEmptyBorder(13, 15, 13, 15)));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(label(title, 9, MUTED, Font.BOLD), BorderLayout.WEST);
        top.add(label(index, 9, GOLD, Font.BOLD), BorderLayout.EAST);
        card.add(top);
        card.add(Box.createVerticalStrut(11));
        card.add(label(value, value.length() > 12 ? 18 : 23, NAVY, Font.BOLD));
        card.add(Box.createVerticalStrut(5));
        card.add(label(detail, 10, MUTED, Font.PLAIN));
        card.setPreferredSize(new Dimension(220, 112));
        return card;
    }

    private JPanel section(String title, JPanel content) {
        JPanel section = new JPanel(new BorderLayout(0, 11));
        section.setBackground(Color.WHITE);
        section.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(LINE),
                BorderFactory.createEmptyBorder(15, 16, 15, 16)));
        JLabel heading = label(title, 15, NAVY, Font.BOLD);
        section.add(heading, BorderLayout.NORTH);
        section.add(content, BorderLayout.CENTER);
        section.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.setMaximumSize(new Dimension(Integer.MAX_VALUE, 520));
        return section;
    }

    private JPanel notePanel(String title, String message) {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(250, 248, 242));
        panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(232, 223, 201)),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(label(title, 13, NAVY, Font.BOLD));
        panel.add(Box.createVerticalStrut(5));
        panel.add(label(message, 12, MUTED, Font.PLAIN));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        return panel;
    }

    private JPanel verticalPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        return panel;
    }

    private JPanel tablePanel(JTable table) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        table.setFillsViewportHeight(true);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(LINE));
        scroll.getViewport().setBackground(Color.WHITE);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JTable table(String[] columns, Object[][] rows) {
        DefaultTableModel model = new DefaultTableModel(rows, columns) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable table = new JTable(model);
        table.setFont(BODY);
        table.setRowHeight(34);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(237, 240, 243));
        table.setSelectionBackground(new Color(232, 239, 246));
        table.setSelectionForeground(INK);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        table.getTableHeader().setBackground(new Color(247, 248, 250));
        table.getTableHeader().setForeground(MUTED);
        table.getTableHeader().setPreferredSize(new Dimension(0, 34));
        return table;
    }

    private JPanel fieldGroup(String title, Component input) {
        JPanel group = new JPanel();
        group.setOpaque(false);
        group.setLayout(new BoxLayout(group, BoxLayout.Y_AXIS));
        group.add(label(title, 11, INK, Font.BOLD));
        group.add(Box.createVerticalStrut(6));
        input.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        group.add(input);
        group.setAlignmentX(Component.LEFT_ALIGNMENT);
        group.setMaximumSize(new Dimension(Integer.MAX_VALUE, 68));
        return group;
    }

    private JPanel leftAligned(Component component) {
        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        wrapper.setOpaque(false);
        wrapper.add(component);
        wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        return wrapper;
    }

    private JLabel label(String text, int size, Color color, int style) {
        String escaped = text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
        JLabel label = new JLabel("<html>" + escaped.replace("\n", "<br>") + "</html>");
        label.setFont(new Font("Segoe UI", style, size));
        label.setForeground(color);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JTextField field(String value) {
        JTextField field = new JTextField(value);
        styleField(field);
        return field;
    }

    private void styleField(JTextField field) {
        field.setFont(BODY);
        field.setForeground(INK);
        field.setBackground(Color.WHITE);
        field.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(LINE),
                BorderFactory.createEmptyBorder(7, 9, 7, 9)));
        field.setPreferredSize(new Dimension(200, 38));
    }

    private JComboBox<String> combo(String[] values) {
        JComboBox<String> combo = new JComboBox<String>(new DefaultComboBoxModel<String>(values));
        combo.setFont(BODY);
        combo.setBackground(Color.WHITE);
        combo.setForeground(INK);
        combo.setPreferredSize(new Dimension(200, 38));
        return combo;
    }

    private JButton primaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setBackground(BLUE);
        button.setBorder(BorderFactory.createEmptyBorder(10, 17, 10, 17));
        button.setMargin(new Insets(8, 14, 8, 14));
        return button;
    }

    private JButton demoCredentialButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        button.setForeground(BLUE);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setBorder(BorderFactory.createEmptyBorder());
        button.setContentAreaFilled(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setToolTipText("Click to fill these demo credentials");
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        return button;
    }

    private JButton secondaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 11));
        button.setForeground(BLUE);
        button.setBackground(Color.WHITE);
        button.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(LINE),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        return button;
    }

    private void addDetail(JPanel panel, String title, String value) {
        panel.add(label(title.toUpperCase(), 9, MUTED, Font.BOLD));
        panel.add(label(value, 13, INK, Font.PLAIN));
    }

}
