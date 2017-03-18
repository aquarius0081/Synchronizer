package com.company;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashSet;
import java.util.List;

public class DBUtil {
    public static HashSet<Job> readFromDB() {
        HashSet<Job> jobs = new HashSet<>();
        try {
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
            }
        } catch (Exception e) {
            e.printStackTrace();
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
            stmt.addBatch(sqlBatch.toString());
            stmt.executeLargeBatch();
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Connection getConnectionToDB() {
        Connection connection = null;
        try {
            DriverManager.registerDriver(new com.microsoft.sqlserver.jdbc.SQLServerDriver());
            String url = "jdbc:sqlserver://127.0.0.1\\EMPLOYEESLIST:1433;databaseName=Enterprise";
            String login = "esa";
            String password = "qweASD12#";
            connection = DriverManager.getConnection(url, login, password);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return connection;
    }
}