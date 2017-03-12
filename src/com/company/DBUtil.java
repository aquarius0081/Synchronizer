package com.company;

import java.sql.*;

public class DBUtil
{
    public static void testDatabase() {
        try {
            DriverManager.registerDriver(new com.microsoft.sqlserver.jdbc.SQLServerDriver());
            String url = "jdbc:sqlserver://127.0.0.1\\EMPLOYEESLIST:1433;databaseName=Enterprise";
            String login = "esa";
            String password = "qweASD12#";
            Connection con = DriverManager.getConnection(url, login, password);
            try {
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM Job");
                while (rs.next()) {
                    String str = rs.getString("DepCode");
                    System.out.println("Contact:" + str);
                }
                rs.close();
                stmt.close();
            } finally {
                con.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}