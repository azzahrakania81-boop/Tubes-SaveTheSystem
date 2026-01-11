/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package savethesystem.db;

import java.sql.Connection;
import java.sql.DriverManager;
/**
 *
 * @author LENOVO
 */
public class DBConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/save_the_system";
    private static final String USER = "root";
    private static final String PASS = ""; // XAMPP biasanya kosong

    public static Connection getConnection() throws Exception {
        // WAJIB untuk MySQL Connector 5.x
        Class.forName("com.mysql.jdbc.Driver");
        Connection conn = DriverManager.getConnection(URL, USER, PASS);
        System.out.println("CONNECTED DB: " + conn.getCatalog());
        return conn;
    }
}
