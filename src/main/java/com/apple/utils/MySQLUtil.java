package com.apple.utils;

import com.google.common.base.CaseFormat;
import org.apache.commons.beanutils.BeanUtils;
import org.codehaus.jackson.map.util.BeanUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MySQLUtil {
    public static <T> List<T> query(String sql, Class<T> clz) {

        ArrayList<T> result = new ArrayList<>();

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection(
                    "jdbc:mysql://121.89.202.255:3306/playground?useSSL=false",
                    "root",
                    "lovely0620");
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();

            ResultSetMetaData metaData = rs.getMetaData();

            while (rs.next()) {
                T obj = clz.newInstance();
                // Pay attention. JDBC starts from 1, not 0.
                for (int i = 1; i <= metaData.getColumnCount(); i++) {
                    String columnName = metaData.getColumnName(i);
                    columnName = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, columnName);
                    BeanUtils.setProperty(obj, columnName, rs.getObject(i));
                }
                result.add(obj);
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException("");
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {
        for (Students s : query("select * from students;", Students.class)) {
            System.out.println(s);
        }
    }
}
