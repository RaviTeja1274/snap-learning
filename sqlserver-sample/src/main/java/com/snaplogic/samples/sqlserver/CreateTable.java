package com.snaplogic.samples.sqlserver;

import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

public class CreateTable {


    private String createTableCommand;

    public CreateTable() {
        Properties properties =new Properties();
        try(FileReader fr = new FileReader("create_table.properties")) {
            properties.load(fr);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String encryptionKeyName=properties.getProperty("columnEncryptionKeyName");
        createTableCommand = properties.getProperty("command")
            .replace("{key}", encryptionKeyName);
    }

    public static void main(String[] args) {
        new CreateTable()
            .createTable();
    }

    public void createTable(){
        String connectionUrl = CommonsImpl.data().getConnectionUrl();
        try (Connection sourceConnection = DriverManager.getConnection(connectionUrl);
            PreparedStatement insertStatement = sourceConnection.prepareStatement(createTableCommand)) {
            insertStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
