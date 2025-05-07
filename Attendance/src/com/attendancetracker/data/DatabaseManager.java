package com.attendancetracker.data;

import java.sql.*;
import java.util.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DatabaseManager {

   private static final String DB_URL = "jdbc:oracle:thin:@localhost:1521/attendancepdb1";
   private static final String USER = "attendance_user";
   private static final String PASSWORD = "attend123";

    private static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd ' | ' HH:mm:ss");

    static {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {

            DatabaseMetaData meta = conn.getMetaData();

            ResultSet rs = meta.getTables(null, null, "USERS", null);
            if (!rs.next()) {
                stmt.execute("CREATE TABLE users (" +
                        "id VARCHAR2(20) PRIMARY KEY, " +
                        "password VARCHAR2(100) NOT NULL, " +
                        "role VARCHAR2(20) NOT NULL)");
            }

            rs = meta.getTables(null, null, "STUDENTS", null);
            if (!rs.next()) {
                stmt.execute("CREATE TABLE students (" +
                        "id VARCHAR2(20) PRIMARY KEY, " +
                        "name VARCHAR2(100))");
            }

            rs = meta.getTables(null, null, "ATTENDANCE", null);
            if (!rs.next()) {
                stmt.execute("CREATE TABLE attendance (" +
                        "id VARCHAR2(20), " +
                        "timestamp TIMESTAMP, " +
                        "code VARCHAR2(10), " +
                        "PRIMARY KEY (id, code), " +
                        "FOREIGN KEY (id) REFERENCES users(id))");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

 public static boolean validateUserCredentials(String userId, String password) {
    String query = "SELECT 1 FROM users WHERE id = ? AND LOWER(password) = LOWER(?)";
    try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
         PreparedStatement pstmt = conn.prepareStatement(query)) {
        System.out.println("Trying login for user: " + userId + " with password: " + password);

        pstmt.setString(1, userId);
        pstmt.setString(2, password);
        try (ResultSet rs = pstmt.executeQuery()) {
            return rs.next();
        }
    } catch (SQLException e) {
        e.printStackTrace();
        return false;
    }
}

    public static void addStudent(Student student) {
        String sql = "MERGE INTO users s USING dual ON (s.id = ?) " +
                     "WHEN NOT MATCHED THEN INSERT (id, name) VALUES (?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, student.getId());
            pstmt.setString(2, student.getId());
            pstmt.setString(3, student.getName());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void logAttendance(AttendanceRecord record) {
        String sql = "INSERT INTO attendance (id, timestamp, code) VALUES (?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, record.getStudentId());
            pstmt.setTimestamp(2, Timestamp.valueOf(record.getTimestamp()));
            pstmt.setString(3, record.getCode());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<AttendanceRecord> getAllAttendanceRecords() {
        List<AttendanceRecord> records = new ArrayList<>();
        String query = "SELECT id, timestamp, code FROM attendance ORDER BY timestamp DESC";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String studentId = rs.getString("id");
                LocalDateTime timestamp = rs.getTimestamp("timestamp").toLocalDateTime();
                String code = rs.getString("code");
                records.add(new AttendanceRecord(studentId, timestamp, code));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return records;
    }

    public static List<AttendanceRecord> getAttendanceRecordsBetween(LocalDate start, LocalDate end) {
        List<AttendanceRecord> records = new ArrayList<>();
        String query = "SELECT id, timestamp, code FROM attendance WHERE timestamp BETWEEN ? AND ? ORDER BY timestamp DESC";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setTimestamp(1, Timestamp.valueOf(start.atStartOfDay()));
            stmt.setTimestamp(2, Timestamp.valueOf(end.plusDays(1).atStartOfDay()));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String studentId = rs.getString("id");
                    LocalDateTime timestamp = rs.getTimestamp("timestamp").toLocalDateTime();
                    String code = rs.getString("code");
                    records.add(new AttendanceRecord(studentId, timestamp, code));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return records;
    }

    public static Map<LocalDate, List<AttendanceRecord>> groupAttendanceByDay(List<AttendanceRecord> records) {
        Map<LocalDate, List<AttendanceRecord>> grouped = new TreeMap<>();
        for (AttendanceRecord record : records) {
            LocalDate date = record.getTimestamp().toLocalDate();
            if (!grouped.containsKey(date)) {
                grouped.put(date, new ArrayList<>());
            }
            grouped.get(date).add(record);
        }
        return grouped;
    }

    public static Map<LocalDate, List<AttendanceRecord>> groupAttendanceByDay(List<AttendanceRecord> records, LocalDate start, LocalDate end) {
        Map<LocalDate, List<AttendanceRecord>> grouped = new TreeMap<>();
        for (AttendanceRecord record : records) {
            LocalDate date = record.getTimestamp().toLocalDate();
            if ((date.isEqual(start) || date.isAfter(start)) && (date.isEqual(end) || date.isBefore(end))) {
                if (!grouped.containsKey(date)) {
                    grouped.put(date, new ArrayList<>());
                }
                grouped.get(date).add(record);
            }
        }
        return grouped;
    }

    public static Map<String, List<AttendanceRecord>> groupAttendanceByWeek(List<AttendanceRecord> records) {
        if (records.isEmpty()) return new TreeMap<>();

        LocalDate baseDate = LocalDate.of(2025, 4, 23);
        Map<String, List<AttendanceRecord>> grouped = new TreeMap<>();

        for (AttendanceRecord record : records) {
            LocalDate currentDate = record.getTimestamp().toLocalDate();
            long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(baseDate, currentDate);
            int weekNumber = (int)(daysBetween / 7) + 1;

            LocalDate weekStart = baseDate.plusDays((weekNumber - 1) * 7);
            LocalDate weekEnd = weekStart.plusDays(6);
            String weekLabel = "Week " + weekNumber + " - " + weekStart + " to " + weekEnd;

            if (!grouped.containsKey(weekLabel)) {
                grouped.put(weekLabel, new ArrayList<>());
            }
            grouped.get(weekLabel).add(record);
        }

        return grouped;
    }

    public static boolean hasMarkedAttendance(String studentId, String code) {
        String sql = "SELECT 1 FROM attendance WHERE id = ? AND code = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, studentId);
            stmt.setString(2, code);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String getUserRole(String id) {
        String sql = "SELECT role FROM users WHERE id = ?"; 
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getString("role");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<String> getAttendanceHistory(String studentId) {
        List<String> history = new ArrayList<>();
        String sql = "SELECT timestamp, code FROM attendance WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, studentId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                LocalDateTime timestamp = rs.getTimestamp("timestamp").toLocalDateTime();
                history.add(timestamp.format(DISPLAY_FORMATTER) + " | Code: " + rs.getString("code"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return history;
    }

    public static List<String> getAllAttendanceSummary() {
        List<String> summary = new ArrayList<>();
        String sql = "SELECT id, timestamp, code FROM attendance ORDER BY timestamp DESC";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                LocalDateTime timestamp = rs.getTimestamp("timestamp").toLocalDateTime();
                summary.add("ID: " + rs.getString("id") +
                        " | Time: " + timestamp.format(DISPLAY_FORMATTER) +
                        " | Code: " + rs.getString("code"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return summary;
    }
}