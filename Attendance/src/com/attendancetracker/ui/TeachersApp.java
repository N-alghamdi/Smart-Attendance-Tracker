package com.attendancetracker.ui;

import com.attendancetracker.data.AttendanceRecord;
import com.attendancetracker.data.DatabaseManager;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.beans.value.ObservableValue;
import java.io.File;
import java.io.FileWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import javafx.util.Callback;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

public class TeachersApp extends Application {

    private Stage primaryStage;
    private BorderPane root;
    private DatePicker startDatePicker;
    private DatePicker endDatePicker;
    private static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd ' | ' HH:mm:ss");
    private Tab dailyTab;
    private Tab weeklyTab;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        root = new BorderPane();
        showLoginScreen();
        primaryStage.setTitle("Teacher Dashboard - Smart Attendance Tracker");
        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.show();
    }

    private void showLoginScreen() {
        VBox loginBox = new VBox(10);
        loginBox.setPadding(new Insets(20));

        TextField idField = new TextField();
        idField.setPromptText("Enter teacher ID");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter password");

        Button loginBtn = new Button("Login");
        loginBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                String id = idField.getText().trim();
                String pw = passwordField.getText().trim();
                if (DatabaseManager.validateUserCredentials(id, pw)
                        && "teacher".equals(DatabaseManager.getUserRole(id))) {
                    showTeacherDashboard();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid credentials.");
                }
            }
        });

        loginBox.getChildren().addAll(new Label("Teacher Login"), idField, passwordField, loginBtn);
        root.setCenter(loginBox);
    }

    private void showTeacherDashboard() {
        HBox filterBox = new HBox(10);
        filterBox.setPadding(new Insets(10));
        startDatePicker = new DatePicker();
        startDatePicker.setPromptText("Start Date");
        endDatePicker = new DatePicker();
        endDatePicker.setPromptText("End Date");
        Button applyFilterBtn = new Button("Apply Date Filter");
        applyFilterBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                dailyTab.setContent(createDailyTable());
                weeklyTab.setContent(createWeeklyTreeTable());
            }
        });

        Button clearFilterBtn = new Button("Clear Date Filter");
        clearFilterBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                startDatePicker.setValue(null);
                endDatePicker.setValue(null);
                dailyTab.setContent(createDailyTable());
                weeklyTab.setContent(createWeeklyTreeTable());
            }
        });

        Button refreshBtn = new Button("Refresh");
        refreshBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                dailyTab.setContent(createDailyTable());
                weeklyTab.setContent(createWeeklyTreeTable());
            }
        });

        filterBox.getChildren().addAll(new Label("Filter by Date:"), startDatePicker, endDatePicker, applyFilterBtn, clearFilterBtn, refreshBtn);

        TabPane tabPane = new TabPane();
        dailyTab = new Tab("Daily Summary");
        weeklyTab = new Tab("Weekly Summary");
        tabPane.getTabs().addAll(dailyTab, weeklyTab);
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        dailyTab.setContent(createDailyTable());
        weeklyTab.setContent(createWeeklyTreeTable());

        Button exportBtn = new Button("Export as CSV");
        exportBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                exportAttendanceToCSV();
            }
        });

        VBox layout = new VBox(10, filterBox, tabPane, exportBtn);
        layout.setPadding(new Insets(10));
        root.setCenter(layout);
    }

    private VBox createDailyTable() {
        TreeTableView<AttendanceRecord> table = new TreeTableView<>();
        table.setColumnResizePolicy(TreeTableView.CONSTRAINED_RESIZE_POLICY);

        TreeTableColumn<AttendanceRecord, String> idCol = new TreeTableColumn<>("Student ID");
        idCol.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<AttendanceRecord, String>, javafx.beans.value.ObservableValue<String>>() {
            @Override
            public javafx.beans.value.ObservableValue<String> call(TreeTableColumn.CellDataFeatures<AttendanceRecord, String> param) {
                TreeItem<AttendanceRecord> item = param.getValue();
                if (item.getParent() != null && item.getParent().getParent() != null) {
                    return new SimpleStringProperty("");
                }
                return new SimpleStringProperty(item.getValue().getStudentId());
            }
        });

        TreeTableColumn<AttendanceRecord, String> timeCol = new TreeTableColumn<>("Timestamp");
        timeCol.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<AttendanceRecord, String>, javafx.beans.value.ObservableValue<String>>() {
            @Override
            public javafx.beans.value.ObservableValue<String> call(TreeTableColumn.CellDataFeatures<AttendanceRecord, String> param) {
                return new SimpleStringProperty(param.getValue().getValue().getTimestamp().format(DISPLAY_FORMATTER));
            }
        });

        TreeTableColumn<AttendanceRecord, String> codeCol = new TreeTableColumn<>("Code");
        codeCol.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<AttendanceRecord, String>, javafx.beans.value.ObservableValue<String>>() {
            @Override
            public javafx.beans.value.ObservableValue<String> call(TreeTableColumn.CellDataFeatures<AttendanceRecord, String> param) {
                return new SimpleStringProperty(param.getValue().getValue().getCode());
            }
        });

        table.getColumns().addAll(idCol, timeCol, codeCol);

        LocalDate start = startDatePicker.getValue();
        LocalDate end = endDatePicker.getValue();
        List<AttendanceRecord> records = (start != null && end != null)
                ? DatabaseManager.getAttendanceRecordsBetween(start, end)
                : DatabaseManager.getAllAttendanceRecords();

        Map<String, List<AttendanceRecord>> grouped = new LinkedHashMap<>();
        for (AttendanceRecord record : records) {
            String studentId = record.getStudentId();
            if (!grouped.containsKey(studentId)) {
                grouped.put(studentId, new ArrayList<>());
            }
            grouped.get(studentId).add(record);
        }

        TreeItem<AttendanceRecord> rootItem = new TreeItem<>();
        rootItem.setExpanded(true);

        for (Map.Entry<String, List<AttendanceRecord>> entry : grouped.entrySet()) {
            List<AttendanceRecord> recs = entry.getValue();
            Collections.sort(recs, new Comparator<AttendanceRecord>() {
                @Override
                public int compare(AttendanceRecord o1, AttendanceRecord o2) {
                    return o2.getTimestamp().compareTo(o1.getTimestamp());
                }
            });
            TreeItem<AttendanceRecord> parent = new TreeItem<>(recs.get(0));
            for (int i = 1; i < recs.size(); i++) {
                parent.getChildren().add(new TreeItem<>(recs.get(i)));
            }
            rootItem.getChildren().add(parent);
        }

        table.setRoot(rootItem);
        table.setShowRoot(false);

        table.setRowFactory(new Callback<TreeTableView<AttendanceRecord>, TreeTableRow<AttendanceRecord>>() {
            @Override
            public TreeTableRow<AttendanceRecord> call(TreeTableView<AttendanceRecord> tableView) {
                return new TreeTableRow<AttendanceRecord>() {
                    @Override
                    protected void updateItem(AttendanceRecord item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setStyle("");
                        } else {
                            String baseColor = (getIndex() % 2 == 0) ? "#f4f4f4" : "white";
                            setStyle("-fx-background-color: " + baseColor + ";" +
                                     "-fx-control-inner-background: " + baseColor + ";" +
                                     "-fx-background: " + baseColor + ";" +
                                     "-fx-selection-bar: " + baseColor + ";" +
                                     "-fx-selection-bar-non-focused: " + baseColor + ";");
                        }
                    }
                };
            }
        });

        return new VBox(table);
    }
private VBox createWeeklyTreeTable() {
    TreeTableView<AttendanceRecord> table = new TreeTableView<>();
    table.setColumnResizePolicy(TreeTableView.CONSTRAINED_RESIZE_POLICY);

    TreeTableColumn<AttendanceRecord, String> idCol = new TreeTableColumn<>("Student ID");
    idCol.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<AttendanceRecord, String>, ObservableValue<String>>() {
        public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<AttendanceRecord, String> param) {
            AttendanceRecord record = param.getValue().getValue();
            if (record.getStudentId().startsWith("Week ")) return new SimpleStringProperty(record.getStudentId());
            TreeItem<AttendanceRecord> parent = param.getValue().getParent();
            if (parent != null && parent.getValue() != null && parent.getValue().getStudentId().equals(record.getStudentId())) {
                return new SimpleStringProperty(""); 
            }
            return new SimpleStringProperty(record.getStudentId());
        }
    });

    TreeTableColumn<AttendanceRecord, String> timeCol = new TreeTableColumn<>("Timestamp");
    timeCol.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<AttendanceRecord, String>, ObservableValue<String>>() {
        public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<AttendanceRecord, String> param) {
            AttendanceRecord record = param.getValue().getValue();
            if (record.getCode().isEmpty()) return new SimpleStringProperty("");
            return new SimpleStringProperty(record.getTimestamp().format(DISPLAY_FORMATTER));
        }
    });

    TreeTableColumn<AttendanceRecord, String> codeCol = new TreeTableColumn<>("Code");
    codeCol.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<AttendanceRecord, String>, ObservableValue<String>>() {
        public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<AttendanceRecord, String> param) {
            return new SimpleStringProperty(param.getValue().getValue().getCode());
        }
    });

    table.getColumns().addAll(idCol, timeCol, codeCol);

    LocalDate start = startDatePicker.getValue();
    LocalDate end = endDatePicker.getValue();
    List<AttendanceRecord> records = (start != null && end != null)
        ? DatabaseManager.getAttendanceRecordsBetween(start, end)
        : DatabaseManager.getAllAttendanceRecords();

    LocalDate base = LocalDate.of(2025, 4, 23);
    Map<Integer, List<AttendanceRecord>> groupedByWeek = new TreeMap<>();

    for (AttendanceRecord r : records) {
        long days = java.time.Duration.between(base.atStartOfDay(), r.getTimestamp()).toDays();
        int week = (int) (days / 7) + 1;
        if (!groupedByWeek.containsKey(week)) {
            groupedByWeek.put(week, new ArrayList<>());
        }
        groupedByWeek.get(week).add(r);
    }

    TreeItem<AttendanceRecord> rootItem = new TreeItem<>();
    rootItem.setExpanded(true);

    for (Map.Entry<Integer, List<AttendanceRecord>> weekEntry : groupedByWeek.entrySet()) {
        int weekNum = weekEntry.getKey();
        List<AttendanceRecord> weekRecords = weekEntry.getValue();

        Map<String, List<AttendanceRecord>> groupedByID = new LinkedHashMap<>();
        for (AttendanceRecord r : weekRecords) {
            if (!groupedByID.containsKey(r.getStudentId())) {
                groupedByID.put(r.getStudentId(), new ArrayList<>());
            }
            groupedByID.get(r.getStudentId()).add(r);
        }

        AttendanceRecord weekPlaceholder = new AttendanceRecord("Week " + weekNum, base.plusDays((weekNum - 1) * 7).atStartOfDay(), "");
        TreeItem<AttendanceRecord> weekParent = new TreeItem<>(weekPlaceholder);
        weekParent.setExpanded(false); 

        for (Map.Entry<String, List<AttendanceRecord>> idEntry : groupedByID.entrySet()) {
            String studentId = idEntry.getKey();
            List<AttendanceRecord> idRecords = idEntry.getValue();

            Collections.sort(idRecords, new Comparator<AttendanceRecord>() {
                public int compare(AttendanceRecord o1, AttendanceRecord o2) {
                    return o2.getTimestamp().compareTo(o1.getTimestamp());
                }
            });

            TreeItem<AttendanceRecord> studentParent = new TreeItem<>(idRecords.get(0));
            for (int i = 1; i < idRecords.size(); i++) {
                studentParent.getChildren().add(new TreeItem<>(idRecords.get(i)));
            }

            weekParent.getChildren().add(studentParent);
        }

        rootItem.getChildren().add(weekParent);
    }

    table.setRoot(rootItem);
    table.setShowRoot(false);

    table.setRowFactory(new Callback<TreeTableView<AttendanceRecord>, TreeTableRow<AttendanceRecord>>() {
        public TreeTableRow<AttendanceRecord> call(TreeTableView<AttendanceRecord> tableView) {
            return new TreeTableRow<AttendanceRecord>() {
                protected void updateItem(AttendanceRecord item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setStyle("");
                    } else {
                        String baseColor = (getIndex() % 2 == 0) ? "#f4f4f4" : "white";
                        setStyle("-fx-background-color: " + baseColor + ";" +
                                "-fx-control-inner-background: " + baseColor + ";" +
                                "-fx-background: " + baseColor + ";" +
                                "-fx-selection-bar: " + baseColor + ";" +
                                "-fx-selection-bar-non-focused: " + baseColor + ";");
                    }
                }
            };
        }
    });

    return new VBox(table);
}

    private void exportAttendanceToCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Attendance Summary");
        fileChooser.setInitialFileName("attendance_summary.csv");
        File file = fileChooser.showSaveDialog(primaryStage);

        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                writer.write("Student ID,Timestamp,Code\n");

                List<AttendanceRecord> records = DatabaseManager.getAllAttendanceRecords();

                Map<String, List<AttendanceRecord>> grouped = new LinkedHashMap<>();
                for (AttendanceRecord record : records) {
                    String studentId = record.getStudentId();
                    if (!grouped.containsKey(studentId)) {
                        grouped.put(studentId, new ArrayList<>());
                    }
                    grouped.get(studentId).add(record);
                }

                int groupCounter = 0;
                int totalGroups = grouped.size();

                for (Map.Entry<String, List<AttendanceRecord>> entry : grouped.entrySet()) {
                    String studentId = entry.getKey();
                    List<AttendanceRecord> recs = entry.getValue();
                    Collections.sort(recs, new Comparator<AttendanceRecord>() {
                        @Override
                        public int compare(AttendanceRecord o1, AttendanceRecord o2) {
                            return o2.getTimestamp().compareTo(o1.getTimestamp());
                        }
                    });

                    if (!recs.isEmpty()) {
                        AttendanceRecord first = recs.get(0);
                        writer.write(String.format("%s,%s,%s\n",
                                studentId,
                                first.getTimestamp().format(DISPLAY_FORMATTER),
                                first.getCode()));
                    }

                    for (int i = 1; i < recs.size(); i++) {
                        AttendanceRecord r = recs.get(i);
                        writer.write(String.format(",%s,%s\n",
                                r.getTimestamp().format(DISPLAY_FORMATTER),
                                r.getCode()));
                    }

                    groupCounter++;
                    if (groupCounter < totalGroups) {
                        writer.write("\n");
                    }
                }

                showAlert(Alert.AlertType.INFORMATION, "Success", "Attendance exported successfully.");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Export Failed", "Could not export file.");
            }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}