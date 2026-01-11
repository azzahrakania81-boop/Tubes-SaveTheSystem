/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package savethesystem.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.security.MessageDigest;
import savethesystem.db.DBConnection;
import savethesystem.model.User;
import java.security.MessageDigest;

/**
 *
 * @author LENOVO
 */
public class UserDAO {
        private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(password.getBytes("UTF-8"));

            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }

    

    public boolean register(String username, String passwordPlain) {
        String sql = "INSERT INTO users(username, password_hash) VALUES(?, ?)";
        try (Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            String hashedPassword = hashPassword(passwordPlain);

            ps.setString(1, username);
            ps.setString(2, hashedPassword);
            ps.executeUpdate();
            return true;

        } catch (Exception e) {
            // kalau username sudah dipakai, akan error UNIQUE
            System.out.println("Register gagal: " + e.getMessage());
            e.printStackTrace(); // ✅ ini bikin error lengkap tampil
            return false;
        }
    }

    public User login(String username, String passwordPlain) {
        String sql = "SELECT user_id, username FROM users WHERE username=? AND password_hash=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, hashPassword(passwordPlain));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(rs.getInt("user_id"), rs.getString("username"));
                }
            }
            return null;

        } catch (Exception e) {
            System.out.println("Login error: " + e.getMessage());
            return null;
        }
    }
}

