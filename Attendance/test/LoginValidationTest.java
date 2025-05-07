/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author noaf
 */


import com.attendancetracker.data.DatabaseManager;

import java.util.Scanner;



public class LoginValidationTest {
   public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        try {
            System.out.print("Enter ID: ");
            String id = scanner.nextLine();

            System.out.print("Enter Password: ");
            String pw = scanner.nextLine();

            boolean success = DatabaseManager.validateUserCredentials(id, pw);

            if (success) {
                System.out.println("These credentials are correct and exists in the database");
            } else {
                System.out.println("These credentials are wrong and it is not found in the database");
            }
        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }
}


