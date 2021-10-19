package com.snaplogic.samples.sqlserver;

import com.microsoft.sqlserver.jdbc.SQLServerColumnEncryptionJavaKeyStoreProvider;
import com.microsoft.sqlserver.jdbc.SQLServerColumnEncryptionKeyStoreProvider;
import com.microsoft.sqlserver.jdbc.SQLServerException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Properties;

public class CreateColumn {

    // Alias of the key stored in the keystore.
    private static String keyAlias;

    // Name by which the column master key will be known in the database.
    private static String columnMasterKeyName;

    // Name by which the column encryption key will be known in the database.
    private static String columnEncryptionKey;

    // The location of the keystore.
    private static String keyStoreLocation;

    // The password of the keystore and the key.
    private static char[] keyStoreSecret;

    /**
     * Name of the encryption algorithm used to encrypt the value of the column encryption key. The algorithm for the system providers must be
     * RSA_OAEP.
     */
    private static String algorithm = "RSA_OAEP";

    static {
        Properties properties =new Properties();
        try(FileReader fr = new FileReader("create_encrypt_column.properties")) {
            properties.load(fr);
        } catch (IOException e) {
            e.printStackTrace();
        }
        keyAlias = properties.getProperty("keyAlias");
        columnMasterKeyName = properties.getProperty("columnMasterKey");
        columnEncryptionKey = properties.getProperty("columnEncryptionKey");
        keyStoreLocation = properties.getProperty("keyStoreLocation");
        keyStoreSecret = properties.getProperty("keyStoreSecret").toCharArray();
    }

    public static void main(String[] args) {
        new CreateColumn().createColumn();
    }

    public void createColumn(){
        String connectionUrl = CommonsImpl.data().getConnectionUrl();
        try (Connection connection = DriverManager.getConnection(connectionUrl);
            Statement statement = connection.createStatement();) {
            SQLServerColumnEncryptionKeyStoreProvider storeProvider = new SQLServerColumnEncryptionJavaKeyStoreProvider(keyStoreLocation,
                keyStoreSecret);

            byte[] encryptedCEK = getEncryptedCEK(storeProvider);

            /**
             * Create column encryption key For more details on the syntax, see:
             * https://docs.microsoft.com/sql/t-sql/statements/create-column-encryption-key-transact-sql Encrypted column encryption key first needs
             * to be converted into varbinary_literal from bytes, for which byteArrayToHex() is used.
             */
            String createCEKSQL = "CREATE COLUMN ENCRYPTION KEY "
                + columnEncryptionKey
                + " WITH VALUES ( "
                + " COLUMN_MASTER_KEY = "
                + columnMasterKeyName
                + " , ALGORITHM =  '"
                + algorithm
                + "' , ENCRYPTED_VALUE =  0x"
                + byteArrayToHex(encryptedCEK)
                + " ) ";
            statement.executeUpdate(createCEKSQL);
            System.out.println("Column encryption key created with name : " + columnEncryptionKey);
        }catch (Exception w){
            w.printStackTrace();
        }

    }

    private static byte[] getEncryptedCEK(SQLServerColumnEncryptionKeyStoreProvider storeProvider) throws SQLServerException {
        String plainTextKey = "You need to give your plain text";

        // plainTextKey has to be 32 bytes with current algorithm supported
        byte[] plainCEK = plainTextKey.getBytes();

        // This will give us encrypted column encryption key in bytes
        byte[] encryptedCEK = storeProvider.encryptColumnEncryptionKey(keyAlias, algorithm, plainCEK);

        return encryptedCEK;
    }

    public static String byteArrayToHex(byte[] a) {
        StringBuilder sb = new StringBuilder(a.length * 2);
        for (byte b : a)
            sb.append(String.format("%02x", b).toUpperCase());
        return sb.toString();
    }

}
