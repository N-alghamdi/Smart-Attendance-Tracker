
# Smart Attendance Tracker

## Overview
This project is a **JavaFX-based desktop application** that tracks student attendance using a code-based entry system. It includes **teacher and student interfaces**, backed by an Oracle 21c database. Teachers can monitor daily and weekly attendance summaries with expandable views for duplicated student records.

---

## Features
- **Login system** for teachers and students
- **Code-based attendance submission**
- **Daily and weekly summaries** with expandable rows for repeated entries
- **Date-based filtering** and CSV export
- Automatically groups weekly attendance based on a fixed reference date
- Full UI built with **JavaFX**, styled via CSS
- **Database integration** with Oracle using JDBC

---

## Modules and Classes

### 1. `AttendanceApp.java`
- Main app class for student-side attendance submission
- Allows students to input **ID** and **code**
- Sends data to the Oracle database

### 2. `TeachersApp.java`
- Teacher dashboard UI
- Displays:
  - Daily attendance (flat view)
  - Weekly attendance (grouped by week and student, expandable)
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

### 4. `AttendanceRecord.java`
- Represents a single attendance entry
- Attributes:
  - `studentId`
  - `timestamp`
  - `code`

---

## How It Works

### 1. Student Flow
- Students launch `AttendanceApp`
- Enter student ID and code
- Code is submitted and saved in the database with a timestamp

### 2. Teacher Flow
- Teachers log in via `TeachersApp`
- Can view attendance in two modes:
  - **Daily Summary** (flat list)
  - **Weekly Summary** (grouped by week and student, expandable)
- Apply date filters or export data as CSV

---

## Example Usage

### Student Entry
```
Student ID: 2211974
Code: ZY1JRO
```

### Teacher View (Weekly)
```
> Week 3
    2211974   | 2025-05-07 | 20:05:45 | ZY1JRO
    2211974   | 2025-05-06 | 19:58:12 | KX1FGA
```

---

## How to Run

### Prerequisites
- Oracle 21c database installed and running
- JavaFX SDK (21+) properly configured

### Steps

1. Import the project into your IDE
2. Update Oracle DB credentials in `DatabaseManager.java`:
   ```java
   private static final String URL = "jdbc:oracle:thin:@localhost:1521/attendancepdb1";
   private static final String USER = "your_user";
   private static final String PASSWORD = "your_password";
   ```
3. Compile and run:
   - For student UI: `AttendanceApp.java`
   - For teacher UI: `TeachersApp.java`

---

## Customization

- **Week Start Date**: Change the base date in `TeachersApp.java`
  ```java
  LocalDate base = LocalDate.of(2025, 4, 23);
  ```
- **UI Theme**: Modify `styles.css` for custom look
- **Database**: You can switch to MySQL or another DB by adjusting `DatabaseManager.java`

---

## Limitations
- No real-time validation of code correctness
- Student interface does not show success/failure confirmation on submission
- No admin control panel for user management (only DB-level inserts for now)

---

## Future Improvements
- Add student-side history view
- Use hashed password authentication
- Add real-time attendance validation rules
- Integrate email notifications

---
