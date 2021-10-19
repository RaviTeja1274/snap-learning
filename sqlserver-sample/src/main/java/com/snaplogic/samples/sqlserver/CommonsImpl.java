package com.snaplogic.samples.sqlserver;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class CommonsImpl {

    private String host;
    private String user;
    private String password;
    private String keyStorePath;
    private String keyStorePassword;
    private String database;

    private static final String CONNECTION_URL = "jdbc:sqlserver://%s;databaseName=%s;user=%s;password=%s;columnEncryptionSetting=Enabled;keyStoreAuthentication=JavaKeyStorePassword;keyStoreLocation=%s;keyStoreSecret=%s";


    private CommonsImpl(String host, String user, String password, String keyStorePath,
        String keyStorePassword, String database) {
        this.host = host;
        this.user = user;
        this.password = password;
        this.keyStorePath = keyStorePath;
        this.keyStorePassword = keyStorePassword;
        this.database = database;
    }

    public String getConnectionUrl(){
        return String.format(CONNECTION_URL, host,database,user,password,keyStorePath,keyStorePassword);
    }

    public static CommonsImpl data(){
        Properties properties =new Properties();
        try(FileReader fileReader = new FileReader("common.properties")) {
            properties.load(fileReader);
        } catch (IOException e) {
            e.printStackTrace();
        }
       return new CommonsImpl(properties.getProperty("host"),
            properties.getProperty("user"),
            properties.getProperty("password"),
            properties.getProperty("keyStorePath"),
            properties.getProperty("keyStorePassword"),
            properties.getProperty("database"));
    }

}
