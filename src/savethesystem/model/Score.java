/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package savethesystem.model;

/**
 *
 * @author LENOVO
 */
public class Score {
    private String username;
    private int finalScore;
    private String createdAt;

    public Score(String username, int finalScore, String createdAt) {
        this.username = username;
        this.finalScore = finalScore;
        this.createdAt = createdAt;
    }

    public String getUsername() {
        return username;
    }

    public int getFinalScore() {
        return finalScore;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
