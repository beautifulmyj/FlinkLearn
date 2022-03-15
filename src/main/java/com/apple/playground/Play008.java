package com.apple.playground;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class Play008 {
    public static void main(String[] args) throws SQLException {
// JDBC连接的URL, 不同数据库有不同的格式:
        String JDBC_URL = "jdbc:mysql://8.142.100.240:3306/playground";
        String JDBC_USER = "root";
        String JDBC_PASSWORD = "lovely0620";
// 获取连接:
        Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
// TODO: 访问数据库...
// 关闭连接:
        conn.close();
    }
//    public static <T> List<T> query(String sql, Class<T> clazz) throws ClassNotFoundException, SQLException {
//        Class.forName("com.mysql.jdbc.Driver");
//        Connection conn = DriverManager.getConnection(
//                "jdbc:mysql://hadoop102:3306",
//                "",
//                ""
//        );
//        PreparedStatement ps = conn.prepareStatement(sql);
//        ps.execute()
//    }
}
