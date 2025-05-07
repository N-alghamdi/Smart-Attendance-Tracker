/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.attendancetracker.service;

/**
 *
 * @author sulto
 */

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

public class AttendanceService {

    private static final ConcurrentHashMap<String, Boolean> submittedIds = new ConcurrentHashMap<>();
    private static String activeCode = null;
    private static LocalDateTime codeExpiry = null;

    // Generate a new attendance code and its validity period
    public static synchronized void generateNewCode(String code, int validSeconds) {
        activeCode = code;
        codeExpiry = LocalDateTime.now().plusSeconds(validSeconds);
        submittedIds.clear();
    }

    // Validate if the code is correct and not expired
    public static synchronized boolean isCodeValid(String code) {
        if (activeCode == null || LocalDateTime.now().isAfter(codeExpiry)) {
            return false; // code expired or not set
        }
        return activeCode.equals(code); // valid code check
    }

    // Check if the student has already submitted attendance
    public static synchronized boolean hasAlreadySubmitted(String studentId) {
        return submittedIds.containsKey(studentId);
    }

    // Mark the student's attendance as submitted
    public static synchronized void recordSubmission(String studentId) {
        submittedIds.put(studentId, true);
    }

    // Get the current valid attendance code
    public static synchronized String getCurrentCode() {
        return activeCode;
    }

    // Check if the current attendance code is still active
    public static synchronized boolean isCodeActive() {
        return activeCode != null && LocalDateTime.now().isBefore(codeExpiry);
    }

}