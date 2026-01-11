/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package savethesystem.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import savethesystem.db.DBConnection;
/**
 *
 * @author LENOVO
 */
public class LogDAO {
    public void insertLog(int sessionId, int roundNo, String problem, String action,
                          int dStab, int dPerf, int dSec) {

        String sql = "INSERT INTO logs(session_id, round_no, problem, action, delta_stability, delta_performance, delta_security) " +
                     "VALUES(?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, sessionId);
            ps.setInt(2, roundNo);
            ps.setString(3, problem);
            ps.setString(4, action);
            ps.setInt(5, dStab);
            ps.setInt(6, dPerf);
            ps.setInt(7, dSec);
            ps.executeUpdate();

        } catch (Exception e) {
            System.out.println("insertLog error: " + e.getMessage());
        }
    }
}
