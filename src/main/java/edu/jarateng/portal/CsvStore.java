package edu.jarateng.portal;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class CsvStore {
    private static final String STUDENT_HEADER = "id,student_number,first_name,surname,email,course,status,enrollment_date,marks";
    private static final String LECTURER_HEADER = "id,first_name,surname,email,modules";
    private final Path directory;

    CsvStore(Path directory) {
        this.directory = directory;
    }

    List<PortalData.Student> loadStudents() throws IOException {
        Path file = directory.resolve("students.csv");
        if (!Files.exists(file)) return null;
        List<PortalData.Student> students = new ArrayList<PortalData.Student>();
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String header = reader.readLine();
            if (!STUDENT_HEADER.equals(header)) throw new IOException("The saved student file has an unsupported format.");
            String row;
            int line = 1;
            while ((row = reader.readLine()) != null) {
                line++;
                if (row.trim().isEmpty()) continue;
                try {
                    List<String> cells = parseRow(row);
                    if (cells.size() != 9) throw new IllegalArgumentException("Expected 9 columns.");
                    PortalData.Student student = new PortalData.Student(
                            Integer.parseInt(cells.get(0)), cells.get(1), cells.get(2), cells.get(3),
                            cells.get(4), cells.get(5), cells.get(6), cells.get(7));
                    parseMarks(cells.get(8), student);
                    students.add(student);
                } catch (RuntimeException error) {
                    throw new IOException("Could not read saved student data on line " + line + ".", error);
                }
            }
        }
        return students;
    }

    void saveStudents(List<PortalData.Student> students) throws IOException {
        Path file = directory.resolve("students.csv");
        Files.createDirectories(directory);
        Path temporary = file.resolveSibling(file.getFileName().toString() + ".tmp");
        try {
            try (BufferedWriter writer = Files.newBufferedWriter(temporary, StandardCharsets.UTF_8)) {
                writer.write(STUDENT_HEADER);
                writer.newLine();
                for (PortalData.Student student : students) {
                    List<String> cells = new ArrayList<String>();
                    cells.add(String.valueOf(student.id));
                    cells.add(student.number);
                    cells.add(student.firstName);
                    cells.add(student.surname);
                    cells.add(student.email);
                    cells.add(student.courseCode);
                    cells.add(student.status);
                    cells.add(student.enrollmentDate);
                    cells.add(encodeMarks(student.marks));
                    writer.write(formatRow(cells));
                    writer.newLine();
                }
            }
            try {
                Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (java.nio.file.AtomicMoveNotSupportedException error) {
                Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    List<PortalData.Lecturer> loadLecturers() throws IOException {
        Path file = directory.resolve("lecturers.csv");
        if (!Files.exists(file)) return null;
        List<PortalData.Lecturer> lecturers = new ArrayList<PortalData.Lecturer>();
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String header = reader.readLine();
            if (!LECTURER_HEADER.equals(header)) throw new IOException("The saved lecturer file has an unsupported format.");
            String row;
            int line = 1;
            while ((row = reader.readLine()) != null) {
                line++;
                if (row.trim().isEmpty()) continue;
                try {
                    List<String> cells = parseRow(row);
                    if (cells.size() != 5) throw new IllegalArgumentException("Expected 5 columns.");
                    String[] modules = cells.get(4).isEmpty() ? new String[0] : cells.get(4).split("\\|", -1);
                    for (String module : modules) {
                        if (module.isEmpty()) throw new IllegalArgumentException("Invalid module assignment.");
                    }
                    lecturers.add(new PortalData.Lecturer(Integer.parseInt(cells.get(0)),
                            cells.get(1), cells.get(2), cells.get(3), modules));
                } catch (RuntimeException error) {
                    throw new IOException("Could not read saved lecturer data on line " + line + ".", error);
                }
            }
        }
        return lecturers;
    }

    void saveLecturers(List<PortalData.Lecturer> lecturers) throws IOException {
        Path file = directory.resolve("lecturers.csv");
        Files.createDirectories(directory);
        Path temporary = file.resolveSibling(file.getFileName().toString() + ".tmp");
        try {
            try (BufferedWriter writer = Files.newBufferedWriter(temporary, StandardCharsets.UTF_8)) {
                writer.write(LECTURER_HEADER);
                writer.newLine();
                for (PortalData.Lecturer lecturer : lecturers) {
                    List<String> cells = new ArrayList<String>();
                    cells.add(String.valueOf(lecturer.id));
                    cells.add(lecturer.firstName);
                    cells.add(lecturer.surname);
                    cells.add(lecturer.email);
                    cells.add(String.join("|", lecturer.modules));
                    writer.write(formatRow(cells));
                    writer.newLine();
                }
            }
            try {
                Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (java.nio.file.AtomicMoveNotSupportedException error) {
                Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private static String encodeMarks(Map<String, List<Integer>> marks) {
        StringBuilder encoded = new StringBuilder();
        for (Map.Entry<String, List<Integer>> entry : marks.entrySet()) {
            if (encoded.length() > 0) encoded.append('|');
            encoded.append(entry.getKey()).append(':');
            for (int i = 0; i < entry.getValue().size(); i++) {
                if (i > 0) encoded.append(',');
                Integer score = entry.getValue().get(i);
                if (score != null) encoded.append(score);
            }
        }
        return encoded.toString();
    }

    private static void parseMarks(String encoded, PortalData.Student student) {
        if (encoded.isEmpty()) return;
        for (String moduleEntry : encoded.split("\\|")) {
            String[] parts = moduleEntry.split(":", 2);
            if (parts.length != 2 || parts[0].isEmpty()) throw new IllegalArgumentException("Invalid mark entry.");
            List<Integer> scores = new ArrayList<Integer>();
            if (!parts[1].isEmpty()) {
                for (String score : parts[1].split(",", -1)) {
                    if (score.isEmpty()) {
                        scores.add(null);
                        continue;
                    }
                    int value = Integer.parseInt(score);
                    if (value < 0 || value > 100) throw new IllegalArgumentException("Invalid mark value.");
                    scores.add(value);
                }
            }
            student.marks.put(parts[0], scores);
        }
    }

    private static String formatRow(List<String> cells) {
        StringBuilder row = new StringBuilder();
        for (int i = 0; i < cells.size(); i++) {
            if (i > 0) row.append(',');
            String cell = cells.get(i) == null ? "" : cells.get(i);
            row.append('"').append(cell.replace("\"", "\"\"")).append('"');
        }
        return row.toString();
    }

    private static List<String> parseRow(String row) {
        List<String> cells = new ArrayList<String>();
        StringBuilder cell = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < row.length(); i++) {
            char current = row.charAt(i);
            if (current == '"') {
                if (quoted && i + 1 < row.length() && row.charAt(i + 1) == '"') {
                    cell.append('"');
                    i++;
                } else {
                    quoted = !quoted;
                }
            } else if (current == ',' && !quoted) {
                cells.add(cell.toString());
                cell.setLength(0);
            } else {
                cell.append(current);
            }
        }
        if (quoted) throw new IllegalArgumentException("Unclosed quoted field.");
        cells.add(cell.toString());
        return cells;
    }
}
