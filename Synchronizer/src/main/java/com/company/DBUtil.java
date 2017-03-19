package com.company;

import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.*;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

class DBUtil {
    private final static Logger logger = Logger.getLogger(DBUtil.class);

    static HashSet<Job> readFromDB() {
        final HashSet<Job> jobs = new HashSet<>();
        try (final Connection con = getConnectionToDB()) {
            final Statement stmt = con.createStatement();
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
            logger.fatal(String.format("Fatal error during reading data from DB: %s", e.getMessage()));
            throw new RuntimeException("Fatal error during reading data from DB!");
        }
        return jobs;
    }

    static void writeToDB(final List<String> sqlStatements) {
        try (final Connection con = getConnectionToDB()) {
            final Statement stmt = con.createStatement();
            final StringBuilder sqlBatch = new StringBuilder();
            sqlBatch.append("BEGIN TRY\n");
            sqlBatch.append("BEGIN TRANSACTION \n");
            for (String sqlStmt : sqlStatements) {
                sqlBatch.append(sqlStmt).append("\n");
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
            logger.fatal(String.format("Fatal error during writing data to DB: %s", e.getMessage()));
            throw new RuntimeException("Fatal error during writing data to DB!");
        }
    }

    private static Connection getConnectionToDB() {
        final Connection connection;
        try {
            final Properties properties = new Properties();
            properties.load(new FileInputStream("synchronizer.properties"));

            DriverManager.registerDriver(new com.microsoft.sqlserver.jdbc.SQLServerDriver());
            connection = DriverManager.getConnection(properties.getProperty("db.connectionString"),
                    properties.getProperty("db.user"),
                    properties.getProperty("db.password"));
        } catch (FileNotFoundException e) {
            final String fatalErrorMessage = "The synchronizer.properties file is not found!";
            logger.fatal(fatalErrorMessage);
            throw new RuntimeException(fatalErrorMessage);
        } catch (IOException e) {
            logger.fatal(String.format("Error occurred when loading from the synchronizer.properties file: %s", e.getMessage()));
            throw new RuntimeException("Error occurred when reading from the synchronizer.properties file!");
        } catch (SQLException e) {
            logger.fatal(String.format("Cannot connect to DB: %s", e.getMessage()));
            throw new RuntimeException("Cannot connect to DB!");
        }
        return connection;
    }
}