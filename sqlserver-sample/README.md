# Introduction
Process To Create encrypt column and insert data.

## Create java keystore
* Create .jks file using following command\
```text
keytool -genkeypair -keyalg RSA -alias <key_alias> -keystore <jks_file> -storepass <password> -validity 360 -keysize 2048 -storetype jks
```
**Example:**
keytool -genkeypair -keyalg RSA -alias myalias -keystore mystore.jks -storepass mypwd -validity 360 -keysize 2048 -storetype jks

## Common Properties
* Update the following properties in common.properties file
* Properties:\
  **host:** sql-server host\
  **database:** name of database to work on\
  **user:** username of server\
  **password:** password need to be used to login to server\
  **keyStorePath:** location of jks file created\
  **keyStorePassword:** password used to create jks file

## Create column master key
* Login to sql-server\
  `sqlcmd -S localhost -U <username> -P '<password>' -g -d <database_name>`
* Create column master key using the key alias created in above step\
```sql
CREATE COLUMN MASTER KEY <key_name> WITH (KEY_STORE_PROVIDER_NAME = N'MSSQL_JAVA_KEYSTORE',KEY_PATH = N'<key_alias>');
```
**Example:**
CREATE COLUMN MASTER KEY test_cmk WITH (KEY_STORE_PROVIDER_NAME = N'MSSQL_JAVA_KEYSTORE',KEY_PATH = N'myalias');


## Create column encryption key
* Run `CreateColumn` class by updating following properties in create_encrypt_column.properties file
* Properties:\
  **keyAlias:** jks alias created using keytool command\
  **columnMasterKeyName**: master key created in previous step\
  **columnEncryptionKey**: name of key to be used for column encryption, it will be used to encrpyt columns in a table.\
  **keyStoreLocation**: location of jks file\
  **keyStoreSecret**: password used to create jks file

## Create table 
* create table using the column encryption key created in previous step by providing following properties in create_table.properties and Run `CreateTable` class
* Properties to update:\
  **cmd**: command to create table\
  **columnEncryptionKeyName:** key value used to create a column encryption\
**Note1:** added a sample create table command, use the same as reference to create one. It has one literal `{key}` that will be.
replaced with the columnEncryptionKey at runtime so set the value as it is before generating create table command\
**Note2:** The actual line which will create encrypted column is\
`ENCRYPTED WITH (ENCRYPTION_TYPE = DETERMINISTIC, ALGORITHM = 'AEAD_AES_256_CBC_HMAC_SHA_256', COLUMN_ENCRYPTION_KEY = {key})` so be note of before creating table.

## Insert data into table
* Run `SqlServerInsert` class by changing insert command in the class based on your table