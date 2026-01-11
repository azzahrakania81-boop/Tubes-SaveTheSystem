/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package savethesystem.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import savethesystem.db.DBConnection;
import savethesystem.model.Score;
/**
 *
 * @author LENOVO
 */
public class ScoreDAO {
    public void saveScore(int sessionId, int finalScore, int roundsSurvived) {
        String sql = "INSERT INTO scores(session_id, final_score, rounds_survived) VALUES(?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, sessionId);
            ps.setInt(2, finalScore);
            ps.setInt(3, roundsSurvived);
            ps.executeUpdate();

        } catch (Exception e) {
            System.out.println("saveScore error: " + e.getMessage());
        }
    }

    public List<Score> getTopScores(int limit) {
        String sql =
            "SELECT u.username, s.final_score, s.created_at " +
            "FROM scores s " +
            "JOIN sessions se ON s.session_id = se.session_id " +
            "JOIN users u ON se.user_id = u.user_id " +
            "ORDER BY s.final_score DESC " +
            "LIMIT ?";

        List<Score> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String username = rs.getString("username");
                    int score = rs.getInt("final_score");
                    String createdAt = rs.getString("created_at");
                    list.add(new Score(username, score, createdAt));
                }
            }

        } catch (Exception e) {
            System.out.println("getTopScores error: " + e.getMessage());
        }

        return list;
    }
}
