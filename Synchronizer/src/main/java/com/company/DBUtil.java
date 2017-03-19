package com.company;

import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

public class DBUtil {
    private final static Logger logger = Logger.getLogger(DBUtil.class);

    public static HashSet<Job> readFromDB() {
        HashSet<Job> jobs = new HashSet<>();
        try (Connection con = getConnectionToDB()) {
            Statement stmt = con.createStatement();
            final ResultSet rs = stmt.executeQuery("SELECT * FROM Job");
            while (rs.next()) {
                jobs.add(new Job() {{
                    setDepcode(rs.getString("DepCode"));
                    setDepjob(rs.getString("DepJob"));
                    setDescription(rs.getString("Description"));
                }});
            }
            rs.close();
            stmt.close();
        } catch (Exception e) {
            logger.fatal(String.format("Fatal error during reading data from DB: %s", e.getStackTrace()));
            throw new RuntimeException("Fatal error during reading data from DB!");
        }
        return jobs;
    }

    public static void writeToDB(List<String> sqlStatements) {
        try (Connection con = getConnectionToDB()) {
            Statement stmt = con.createStatement();
            StringBuilder sqlBatch = new StringBuilder();
            sqlBatch.append("BEGIN TRY\n");
            sqlBatch.append("BEGIN TRANSACTION \n");
            for (String sqlStmt : sqlStatements) {
                sqlBatch.append(sqlStmt + "\n");
            }
            sqlBatch.append("COMMIT\n");
            sqlBatch.append("END TRY\n");
            sqlBatch.append("BEGIN CATCH\n");
            sqlBatch.append("    IF @@TRANCOUNT > 0\n");
            sqlBatch.append("        ROLLBACK\n");
            sqlBatch.append("END CATCH");
            final String sqlBatchStr = sqlBatch.toString();
            stmt.addBatch(sqlBatchStr);
            if (logger.isDebugEnabled()) {
                logger.debug("SQL Batch to execute:\n" + sqlBatchStr);
            }
            stmt.executeLargeBatch();
            stmt.close();
        } catch (Exception e) {
            logger.fatal(String.format("Fatal error during writing data to DB: %s", e.getStackTrace()));
            throw new RuntimeException("Fatal error during writing data to DB!");
        }
    }

    private static Connection getConnectionToDB() {
        Connection connection = null;
        try {
            Properties properties = new Properties();
            InputStream input = new FileInputStream("synchronizer.properties");
            properties.load(input);

            DriverManager.registerDriver(new com.microsoft.sqlserver.jdbc.SQLServerDriver());
            String url = properties.getProperty("db.connectionString");
            String login = properties.getProperty("db.user");
            String password = properties.getProperty("db.password");
            connection = DriverManager.getConnection(url, login, password);
        } catch (FileNotFoundException e) {
            String fatalErrorMessage = "The synchronizer.properties file is not found!";
            logger.fatal(fatalErrorMessage);
            throw new RuntimeException(fatalErrorMessage);
        } catch (IOException e) {
            logger.fatal(String.format("Error occurred when loading from the synchronizer.properties file: ", e.getStackTrace()));
            throw new RuntimeException("Error occurred when reading from the synchronizer.properties file!");
        } catch (SQLException e) {
            logger.fatal(String.format("Cannot connect to DB: %s", e.getStackTrace()));
            throw new RuntimeException("Cannot connect to DB!");
        }
        return connection;
    }
}