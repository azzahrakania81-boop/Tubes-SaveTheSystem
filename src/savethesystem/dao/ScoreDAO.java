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
   public void saveScore(int sessionId, int finalScore, int roundsSurvived, String result) {
    String check = "SELECT session_id FROM scores WHERE session_id = ?";
    String sql   = "INSERT INTO scores(session_id, final_score, rounds_survived, result) VALUES(?, ?, ?, ?)";

    try (Connection conn = DBConnection.getConnection()) {

        PreparedStatement psCheck = conn.prepareStatement(check);
        psCheck.setInt(1, sessionId);

        ResultSet rs = psCheck.executeQuery();
        if (rs.next()) {
            System.out.println("Scores are in for the session: " + sessionId);
            return;
        }

        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, sessionId);
        ps.setInt(2, finalScore);
        ps.setInt(3, roundsSurvived);
        ps.setString(4, result);
        ps.executeUpdate();

        System.out.println("Score saved session: " + sessionId);

    } catch (Exception e) {
        System.out.println("saveScore error: " + e.getMessage());
    }
}


  public List<Score> getBestScoresPerUser(int limit) {
    List<Score> list = new ArrayList<>();

    String sql =
        "SELECT u.username, x.final_score, x.result " +
        "FROM users u " +
        "JOIN ( " +
        "   SELECT se.user_id, MAX(s.final_score) AS final_score, 'WIN' AS result " +
        "   FROM scores s " +
        "   JOIN sessions se ON s.session_id = se.session_id " +
        "   WHERE s.result = 'WIN' " +
        "   GROUP BY se.user_id " +

        "   UNION ALL " +

        "   SELECT se.user_id, MAX(s.final_score) AS final_score, 'LOSE' AS result " +
        "   FROM scores s " +
        "   JOIN sessions se ON s.session_id = se.session_id " +
        "   WHERE s.result = 'LOSE' " +
        "     AND se.user_id NOT IN ( " +
        "         SELECT se2.user_id " +
        "         FROM scores s2 " +
        "         JOIN sessions se2 ON s2.session_id = se2.session_id " +
        "         WHERE s2.result = 'WIN' " +
        "     ) " +
        "   GROUP BY se.user_id " +
        ") x ON x.user_id = u.user_id " +
        "ORDER BY CASE WHEN x.result='WIN' THEN 0 ELSE 1 END, x.final_score DESC " +
        "LIMIT ?";

    try (Connection conn = DBConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setInt(1, limit);

        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Score(
                    rs.getString("username"),
                    rs.getInt("final_score"),
                    rs.getString("result")
                ));
            }
        }

    } catch (Exception e) {
        System.out.println("getBestScoresPerUser error: " + e.getMessage());
    }

    return list;
}
}