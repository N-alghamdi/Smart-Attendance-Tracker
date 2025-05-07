/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.attendancetracker.io;

/**
 *
 * @author sulto
 */
import com.attendancetracker.data.AttendanceRecord;

import java.io.FileWriter;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

public class AttendanceLogger {
    private static final String LOG_FILE = "attendance_log.txt";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void log(AttendanceRecord record) {
        try (FileWriter writer = new FileWriter(LOG_FILE, true)) {
            String line = String.format("%s,%s\n",
                record.getStudentId(),
                record.getTimestamp().format(FORMATTER));
            writer.write(line);
        } catch (IOException e) {
            System.err.println("\u26A0\uFE0F Failed to write to log: " + e.getMessage());
        }
    }
}