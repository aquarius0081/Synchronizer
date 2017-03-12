package com.company;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DBUtil
{
    public static List<Job> readFromDB() {
        List<Job> jobs = new ArrayList<Job>();
        try {
            DriverManager.registerDriver(new com.microsoft.sqlserver.jdbc.SQLServerDriver());
            String url = "jdbc:sqlserver://127.0.0.1\\EMPLOYEESLIST:1433;databaseName=Enterprise";
            String login = "esa";
            String password = "qweASD12#";
            Connection con = DriverManager.getConnection(url, login, password);
            try {
                Statement stmt = con.createStatement();
                final ResultSet rs = stmt.executeQuery("SELECT * FROM Job");
                while (rs.next()) {

                    jobs.add(new Job(){{
                        setDepcode(rs.getString("DepCode"));
                        setDepjob(rs.getString("DepJob"));
                        setDescription(rs.getString("Description"));
                    }});
                }
                rs.close();
                stmt.close();
            } finally {
                con.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jobs;
    }
}