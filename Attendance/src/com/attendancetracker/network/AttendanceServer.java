/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.attendancetracker.network;

/**
 *
 * @author sulto
 */
import com.attendancetracker.service.AttendanceService;
import com.attendancetracker.service.CodeGenerator;
import com.attendancetracker.data.AttendanceRecord;
import com.attendancetracker.data.DatabaseManager;
import com.attendancetracker.io.AttendanceLogger;

import java.io.*;
import java.net.*;
import java.util.Enumeration;
import java.net.NetworkInterface;
import java.net.InetAddress;
import java.net.Inet4Address;
import java.time.LocalDateTime;
import java.io.IOException;

public class AttendanceServer {

    private static final int PORT = 5000;
    private static String allowedSubnet;

    public static void main(String[] args) {
        // Dynamically determine the local subnet
        allowedSubnet = getLocalSubnet();

        if (allowedSubnet == null) {
            System.err.println("Could not determine local subnet.");
            return;
        }

        System.out.println("Allowed Subnet: " + allowedSubnet);

        // Generate and activate a new attendance code valid for 2 minutes
        String code = CodeGenerator.generateCode();
        AttendanceService.generateNewCode(code, 120); // Generating and setting the new code
        System.out.println("Today's Attendance Code: " + code + " (valid for 2 minutes)");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started on port " + PORT + "...");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                String clientIP = clientSocket.getInetAddress().getHostAddress();

                // Check if the client IP is in the allowed subnet
                if (!clientIP.startsWith(allowedSubnet)) {
                    System.out.println("Blocked: " + clientIP);  // Log blocked IP
                    clientSocket.close();
                    continue;
                }

                // Handling the client connection in a separate thread
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        handleClient(clientSocket);
                    }
                }).start();
            }
        } catch (IOException e) {
           System.err.println("Connection failed: " + e.getMessage());
        }
    }

    // Handles the client logic
    private static void handleClient(Socket socket) {
        try (
                DataInputStream in = new DataInputStream(socket.getInputStream());
                DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {
            String data = in.readUTF();
            String[] parts = data.split(",");
            
            if (parts.length == 3) {
                parts = new String[]{ parts[0], parts[2] };
            } else if (parts.length != 2) {
                out.writeUTF("Invalid data format.");
                return;
            }

            String studentId = parts[0].trim();
            String code = parts[1].trim();

            // 1) Wrong or expired code?
            if (!AttendanceService.isCodeValid(code)) {
                out.writeUTF("Invalid or expired attendance code.");
            } // 2) Code is right but this student already checked in
            else if (AttendanceService.hasAlreadySubmitted(studentId)) {
                out.writeUTF("Attendance already marked.");
            } // 3) First valid check-in
            else {
                // mark in memory
                AttendanceService.recordSubmission(studentId);

                // persist
                AttendanceRecord record = new AttendanceRecord(studentId, LocalDateTime.now(), code);
                DatabaseManager.logAttendance(record);
                AttendanceLogger.log(record);

                out.writeUTF("Attendance recorded for: " + studentId);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }
    }

    private static String getLocalSubnet() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                String name = iface.getDisplayName().toLowerCase();

                if (!iface.isUp() || iface.isLoopback() ||
                    name.contains("virtual") || name.contains("vbox") || name.contains("vmware") ||
                    name.contains("loopback") || name.contains("tunnel") || name.contains("bluetooth")) {
                    continue;
                }

                if (!(name.contains("wi-fi") || name.contains("wlan") || name.contains("ethernet"))) {
                    continue;
                }

                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
                        String hostAddress = addr.getHostAddress();
                        System.out.println("Selected IP: " + hostAddress + " from interface: " + iface.getDisplayName());
                        return hostAddress.substring(0, hostAddress.lastIndexOf('.') + 1);
                    }
                }
            }
        } catch (SocketException e) {
            System.err.println("Error getting local subnet: " + e.getMessage());
        }
        return null;
    }
}