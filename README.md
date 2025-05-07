
# Smart Attendance Tracker

## Overview
This project is a **JavaFX-based application** that tracks student attendance using a code-based entry system. It includes **teacher and student interfaces**, backed by an Oracle 21c database. Teachers can monitor daily and weekly attendance summaries.

---

## Features
- **Login system** for teachers and students
- **Code-based attendance submission**
- **Daily and weekly summaries** with expandable rows for repeated entries
- **Date-based filtering** and CSV export
- Automatically groups weekly attendance based on a fixed reference date
- Full UI built with **JavaFX**
- **Database integration** with Oracle using JDBC

---

## Classes

### 1. `AttendanceApp.java`
- Main app class for student-side attendance submission
- Allows students to input **ID** and **Password** and **code**
- Sends data to the Oracle database

### 2. `TeachersApp.java`
- Teacher dashboard UI
- Displays:
  - Daily attendance
  - Weekly attendance
- Features:
  - **Expandable rows** for duplicate entries
  - **Date filters**
  - **CSV export**

### 3. `DatabaseManager.java`
- Handles all **Oracle database connections**
- CRUD operations for:
  - Attendance records
  - User authentication (for teachers)
- Methods:
  - `validateTeacherLogin()`
  - `addAttendanceRecord()`
  - `getAttendanceRecordsBetween()`
  - `getAllAttendanceRecords()`

### 4. `AttendanceServer.java `
- Receives data and processes it
- ensuring that when students log in, that thier wifi IP address same as the lecture place

### 5. `AttendanceClient.java`
- Sends attendance data from student to server


### 6. `AttendanceLogger.java`
- Handles writing attendance logs to a file


---

## How It Works

### 1. Student Flow
- Students launch `AttendanceApp`
- Enter student ID and password and code for attendance
- studnets marked as attended whether the credentials match in the database 

### 2. Teacher Flow
- Teachers log in via `TeachersApp`
- Can view attendance in two modes:
  - **Daily Summary** 
  - **Weekly Summary**
- Apply date filters or export data as CSV

---

## Example Usage

### Student Entry
```
Student ID: 2211974
Password: 123456
Code: ZY1JRO
```

### Teacher View (Weekly Summary)
```
> Week 3
    2211974   | 2025-05-07 | 20:05:45 | ZY1JRO
    2211974   | 2025-05-06 | 19:58:12 | KX1FGA
```

---

## How to Run
1. Run **AttendanceServer.java** 
2. Then run  **AttendanceApp.java**  or **TeachersApp.java** depending on the role.

### Prerequisites
- Oracle 21c database installed with tables 
- JavaFX SDK (21+) properly configured


---

## Customization

- **Week Start Date**: Change the base date in `TeachersApp.java`
  ```java
  LocalDate base = LocalDate.of(2025, 4, 23);
  ```
- **UI Theme**: Modify `TeachersApp.java` or `AttendanceApp.java` for custom look
- **Database**: You can switch to MySQL or another DB by adjusting `DatabaseManager.java`

  
