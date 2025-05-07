package com.attendancetracker.data;

import java.time.LocalDateTime;

public class AttendanceRecord {
    private String studentId;
    private LocalDateTime timestamp;
    private String code;

    // Full constructor
    public AttendanceRecord(String studentId, LocalDateTime timestamp, String code) {
        this.studentId = studentId;
        this.timestamp = timestamp;
        this.code = code;
    }

    // Constructor without code (optional, backward-compatible)
    public AttendanceRecord(String studentId, LocalDateTime timestamp) {
        this(studentId, timestamp, null);
    }

    // Getters
    public String getStudentId() {
        return studentId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getCode() {
        return code;
    }

   
}
