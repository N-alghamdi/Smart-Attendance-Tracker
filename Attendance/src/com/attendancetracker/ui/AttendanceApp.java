package com.attendancetracker.ui;

import com.attendancetracker.data.DatabaseManager;
import com.attendancetracker.network.AttendanceClient;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

public class AttendanceApp extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        TextField studentIdField = new TextField();
        studentIdField.setPromptText("Enter your ID");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");

        TextField attendanceCodeField = new TextField();
        attendanceCodeField.setPromptText("Enter today's attendance code");

        Button submitButton = new Button("Submit");

        VBox vbox = new VBox(10);
        vbox.setStyle("-fx-padding: 20;");
        vbox.getChildren().addAll(
                studentIdField,
                passwordField,
                attendanceCodeField,
                submitButton
        );

        // Handle attendance submission using anonymous class instead of lambda
        submitButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                String studentId = studentIdField.getText().trim();
                String password = passwordField.getText().trim();
                String code = attendanceCodeField.getText().trim();

                if (!DatabaseManager.validateUserCredentials(studentId, password)) {
                    showAlert(Alert.AlertType.ERROR, "Invalid Credentials", "Incorrect ID or password.");
                    return;
                }

                String role = DatabaseManager.getUserRole(studentId);
                if ("teacher".equals(role)) {
                    showAlert(Alert.AlertType.INFORMATION, "Teacher Mode",
                            "Welcome Teacher. Please start the server (run AttendanceServer.java).");
                } else {
                    String response = AttendanceClient.startClient(studentId, code);
                    showAlert(Alert.AlertType.INFORMATION, "Attendance Response", response);
                }
            }
        });

        Scene scene = new Scene(vbox, 400, 350);
        primaryStage.setTitle("Smart Attendance Tracker");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}