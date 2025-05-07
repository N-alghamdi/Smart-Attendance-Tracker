package com.attendancetracker.network;

import java.io.*;
import java.net.Socket;
import java.util.Properties;

public class AttendanceClient {

    // Method to load the server address from a config file
     private static String loadServerAddress() {
        Properties props = new Properties();
        String defaultAddress = "localhost";
        try (FileInputStream input = new FileInputStream("config.properties")) {
            props.load(input);
            String address = props.getProperty("server.address");
            if (address != null && !address.trim().isEmpty()) {
                return address.trim();
            }
        } catch (IOException e) {
            System.out.println("Warning: Could not load config.properties. Using default address: " + defaultAddress);
        }
        return defaultAddress;
    }

    /**
     * Connects to the attendance server, sends student ID and code,
     * and returns the server's response message.
     *
     * @param studentId the student's ID
     * @param code the attendance code
     * @return server response message
     */
    public static String startClient(String studentId, String code) {
        String serverAddress = loadServerAddress();
        int port = 5000;
        String response = "";

        try (Socket socket = new Socket(serverAddress, port);
             DataOutputStream out = new DataOutputStream(socket.getOutputStream());
             DataInputStream in = new DataInputStream(socket.getInputStream())) {

            // Send student ID and code
            out.writeUTF(studentId + "," + code);
            out.flush();

            // Receive response
            response = in.readUTF();
            System.out.println("Server: " + response);

        } catch (IOException e) {
            response = "Error connecting to server: " + e.getMessage();
            System.err.println(response);
        }

        return response;
    }
    
    //this used only in clientServerTest file for testing
     public static String startClientForTest(String studentId, String password, String code) {
        String serverAddress = loadServerAddress();
        int port = 5000;
        String response = "";

        try (Socket socket = new Socket(serverAddress, port);
             DataOutputStream out = new DataOutputStream(socket.getOutputStream());
             DataInputStream in = new DataInputStream(socket.getInputStream())) {

          
            String message = studentId + "," + password + "," + code;
            out.writeUTF(message);
            out.flush();

           
            response = in.readUTF();
            System.out.println("Server: " + response);

        } catch (IOException e) {
            response = "Error connecting to server: " + e.getMessage();
            System.err.println(response);
        }

        return response;
    }
}
