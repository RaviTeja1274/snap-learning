package com.snaplogic.snaps.managementsystem;

import com.snaplogic.snap.api.SnapDataException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DbConnector {

    private static final String url = "jdbc:mariadb://localhost:3306/course_management_system?useSSL=false&user=root&password=gaian";
    private static final String user = "root";
    private static final String password = "gaian";
    private Connection connection;

    public DbConnector() throws SQLException, ClassNotFoundException {
        Class.forName("org.mariadb.jdbc.Driver");
        connection = DriverManager.getConnection(url);
    }

    public void registerStudent(int studentId, int courseId, String courseName) {
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = connection.prepareStatement(
                "insert into registration(student_id,course_id,course_name) values(?,?,?)");
            preparedStatement.setInt(1, studentId);
            preparedStatement.setInt(2, courseId);
            preparedStatement.setString(3, courseName);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new SnapDataException("unable to save details in database")
                .withReason(e.getMessage());
        }
    }

    public void payFeesForStudent(int studentId, int courseId, int amount, String comment) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(
                "insert into fees(student_id,course_id,amount,comment) values(?,?,?,?)");
            preparedStatement.setInt(1, studentId);
            preparedStatement.setInt(2, courseId);
            preparedStatement.setInt(3, amount);
            preparedStatement.setString(4, comment);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new SnapDataException("unable to save details in database")
                .withReason(e.getMessage());
        }
    }

    public void deregisterStudent(int registrationId) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(
                "DELETE from registration where registration_id=?");
            preparedStatement.setInt(1, registrationId);
            int rowsUpdated = preparedStatement.executeUpdate();
            if (rowsUpdated == 0){
                throw new SnapDataException("Student with the provided registration id not found");
            }
        } catch (SQLException e) {
            throw new SnapDataException("unable to save details in database")
                .withReason(e.getMessage());
        }
    }

    public void close() throws SQLException {
        connection.close();
        connection = null;
    }

}
