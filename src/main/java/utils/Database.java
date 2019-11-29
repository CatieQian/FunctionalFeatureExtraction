package utils;

import java.sql.*;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;

public class Database {
    private static String driver = "com.mysql.jdbc.Driver";
    private static String URL = "jdbc:mysql://localhost:3306/";
    private static String databaseName = "functional_feature_db";
    private static String URLProperties = "?useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC&useSSL=false";
    private static String userName = "root";
    private static String password = "qy1537";

    public static void main(String[] args) {
        Connection connection = getConnection();
        String sql = "SELECT * FROM api_info \n" +
                "WHERE name=\"AQUA.getIndex\"\n" +
                "  AND project_name=\"poi\"\n" +
                "  AND class_name=\"IndexedColors\"\n" +
                "  AND package_name LIKE \"%\"\n" +
                "  AND return_type LIKE \"%\";";
        HashMap<String, String> result = executeSQL(connection, sql);

        closeConnection(connection);
    }

    public static Connection getConnection() {
        Connection connection = null;
        try {
            Class.forName(driver);
            connection = DriverManager.getConnection(URL + databaseName + URLProperties, userName, password);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException se) {
            se.printStackTrace();
        }
        return connection;
    }

    public static void closeConnection(Connection connection) {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException se) {
            se.printStackTrace();
        }
    }

    public static HashMap<String, String> executeSQL(Connection connection, String sql) {
        HashMap<String, String> resultMap = null;
        try {
            Statement stmt = connection.createStatement();
            boolean hasResultSet = stmt.execute(sql);
            if (hasResultSet) {
                resultMap = new HashMap<String, String>();
                ResultSet resultSet = stmt.getResultSet();

                List<String> columnNameList = new ArrayList<String>();
                ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
                for (int i=1; i<=resultSetMetaData.getColumnCount(); i++) {
                    columnNameList.add(resultSetMetaData.getColumnName(i));
                }

                int size = 0;
                while (resultSet.next()) {
                    size++;
                    // TODO: deal with result set whose size > 1
                    if (size > 1) {
                        resultMap = null;
                        return resultMap;
                    }
                    for (String columnName: columnNameList) {
                        resultMap.put(columnName, resultSet.getString(columnName));
                    }
                }
            }
            stmt.close();
        } catch (SQLException se) {
            se.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultMap;
    }

}
