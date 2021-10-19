package com.snaplogic.samples.sqlserver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SqlServerInsert {

    // change this to your specific table needs
    private static final String INSERT_CMD = "INSERT INTO [dbo].[Patients2] VALUES (?, ?, ?, ?)";
    public static void main(String[] args) {
        String connectionUrl = CommonsImpl.data().getConnectionUrl();
        try (Connection sourceConnection = DriverManager.getConnection(connectionUrl);
            PreparedStatement insertStatement = sourceConnection.prepareStatement(INSERT_CMD)) {

            // set your values here

//            insertStatement.setString(1, "795-73-9838");
//            insertStatement.setString(2, "Catherine");
//            insertStatement.setString(3, "Abel");
//            insertStatement.setDate(4, Date.valueOf("1996-09-10"));
//            insertStatement.executeUpdate();
            System.out.println("1 record inserted.\n");
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
