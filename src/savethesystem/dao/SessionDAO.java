/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package savethesystem.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import savethesystem.db.DBConnection;
/**
 *
 * @author LENOVO
 */
public class SessionDAO {
    public int createSession(int userId) {
        String sql = "INSERT INTO sessions(user_id, current_round, status) VALUES(?, 1, 'ACTIVE')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, userId);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
            return -1;

        } catch (Exception e) {
            System.out.println("createSession error: " + e.getMessage());
            return -1;
        }
    }

    public void updateRound(int sessionId, int roundNo) {
        String sql = "UPDATE sessions SET current_round=? WHERE session_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, roundNo);
            ps.setInt(2, sessionId);
            ps.executeUpdate();

        } catch (Exception e) {
            System.out.println("updateRound error: " + e.getMessage());
        }
    }

    public void finishSession(int sessionId) {
        String sql = "UPDATE sessions SET status='FINISHED' WHERE session_id=? AND status='ACTIVE'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, sessionId);
            ps.executeUpdate();

        } catch (Exception e) {
            System.out.println("finishSession error: " + e.getMessage());
        }
    }
}
