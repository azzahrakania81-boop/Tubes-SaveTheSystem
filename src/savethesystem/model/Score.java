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
    private String result;


    public Score(String username, int finalScore, String result) {
    this.username = username;
    this.finalScore = finalScore;
    this.result = result;
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
    
    public String getResult() {
    return result;
    }   

    public void setResult(String result) {
        this.result = result;
    }

}
