/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author noaf
 */
import com.attendancetracker.service.AttendanceService;
import com.attendancetracker.network.AttendanceServer;
import com.attendancetracker.network.AttendanceClient;
import java.util.Scanner;
public class ClientServerTest {
     public static void main(String[] args) {
        try {
            
            Thread serverThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        AttendanceServer.main(new String[0]);
                    } catch (Exception e) {
                        System.out.println("Server error: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            });
            serverThread.start();

           
            Thread.sleep(1000);

            
            Scanner scanner = new Scanner(System.in);
            System.out.println("=== Client-Server Test ===");
            System.out.print("Enter Student ID: ");
            String id = scanner.nextLine();
            System.out.print("Enter Password: ");
            String password = scanner.nextLine();
            String response = AttendanceClient.startClientForTest(id, password, AttendanceService.getCurrentCode());
            System.out.println("Server Response: " + response);

            if (response.contains("recorded") || response.contains("already")) {
                System.out.println("Client-Server test completed successfully.");
            } else {
                System.out.println("Client-Server test failed");
            }

        } catch (Exception e) {
            System.out.println("Test failed with error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
