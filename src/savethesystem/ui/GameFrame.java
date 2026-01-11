/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package savethesystem.ui;
import savethesystem.model.User;
import javax.swing.JOptionPane;
import java.util.Random;
import savethesystem.ui.LeaderboardFrame;
import savethesystem.dao.SessionDAO;
import savethesystem.dao.ScoreDAO;
import savethesystem.logic.Problem;
import savethesystem.logic.ProblemGenerator;
import savethesystem.dao.LogDAO;
import savethesystem.ui.YouWinFrame;


/**
 *
 * @author LENOVO
 */
public class GameFrame extends javax.swing.JFrame {
    private User currentUser;
    private int sessionId;
    private int stability = 100;
    private int performance = 100;
    private int security = 100;

    private int round = 1;
    private int score = 0;
    private final int WIN_ROUND = 10; 
    private final int MIN_SECURITY = 20;
    private boolean helpShown = false;

    
    private ProblemGenerator generator = new ProblemGenerator();
    private Problem currentProblem;

    
    private String[] problems = {
    "Server overload detected!",
    "Database connection lost!",
    "Suspicious login detected!",
    "Memory leak detected!",
    "CPU usage critical!",
    "Network latency high!"
};
    
    /**
     * Creates new form GameFrame
     */
    public GameFrame() {
        setContentPane(new GradientPanel());
        
        initComponents();
        makeTransparent(getContentPane());
       
        setLocationRelativeTo(null);
        setTitle("Save The System - Game");
    }
    
    private void showHowToPlay() {
    String msg =
        "HOW TO PLAY (Save The System)\n\n" +
        "Fix = fix the problem (generally the safest)\n" +
        "Add Resource = add resources (increase performance, but stability may decrease)\n" +
        "Ignore = ignore the problem (high risk, bar may drop)\n\n" +
        "If one of the bars (Stability/Performance/Security) runs out (0) = LOSE\n" +
        "Win if you successfully complete 10 rounds with Security ≥ 20\n\n" +
        "Tip: Keep Security from dropping too low!";

    JOptionPane.showMessageDialog(this, msg, "How to Play", JOptionPane.INFORMATION_MESSAGE);
}

    
    private void generateNextProblem() {
    currentProblem = generator.getRandomProblem();
    lblProblem.setText("Problem: " + currentProblem.getTitle());
    }
    
    private void applyDecision(String decision) {
    if (currentProblem == null) {
        generateNextProblem();
    }

    // ✅ simpan nilai sebelum berubah
     int beforeStab = stability;
     int beforePerf = performance;
     int beforeSec  = security;
     
     // =============================
    // efek problem (yang random)
    stability += currentProblem.getStabilityImpact();
    performance += currentProblem.getPerformanceImpact();
    security += currentProblem.getSecurityImpact();
    
    // efek tambahan berdasarkan pilihan user
    if (decision.equals("FIX")) {
        stability += 15;
        performance += 5;
        score += 10;
    } else if (decision.equals("ADD_RESOURCE")) {
        performance += 15;
        stability -= 5;
        score += 8;
    } else if (decision.equals("IGNORE")) {
        stability -= 10;
        performance -= 10;
        security -= 10;
        score -= 5;
    }
    
     // =============================
    // clamp
    stability = clamp(stability);
    performance = clamp(performance);
    security = clamp(security);

    // ✅ hitung delta (selisih)
    int dStab = stability - beforeStab;
    int dPerf = performance - beforePerf;
    int dSec  = security - beforeSec;
    
    String action = decision;

    if(decision.equals("FIX")) action = "Fix";
    else if(decision.equals("ADD_RESOURCE")) action = "Add Resource";
    else if(decision.equals("IGNORE")) action = "Ignore";
    
    // 1) LOG dulu pakai round sekarang
    LogDAO logDAO = new LogDAO();
    logDAO.insertLog(sessionId, round, currentProblem.getTitle(), action, dStab, dPerf, dSec);
    
    // 2) Baru round naik
    round++; 
    
    // 3) update current_round di sessions
    SessionDAO sessionDAO = new SessionDAO();
    sessionDAO.updateRound(sessionId, round);
    
    // ✅ CEK WIN / LOSE DULU
    if (checkEndGame()) return;
    
    // 4) problem baru + update UI
    generateNextProblem();
    updateUI();
}

    

    public GameFrame(User user) {
        setContentPane(new GradientPanel());
        initComponents();
        makeTransparent(getContentPane());
        setTextWhite();
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        this.currentUser = user;

        SessionDAO sessionDAO = new SessionDAO();
        this.sessionId = sessionDAO.createSession(currentUser.getUserId());
       
        if (sessionId == -1) {
            JOptionPane.showMessageDialog(this, 
                "Session creation failed! Make sure the database is running and the connection is valid.");
            this.dispose();
            return;
        }
        
         // ✅ tampilkan tutorial sekali
        if (!helpShown) {
            helpShown = true;
            showHowToPlay();
        }

        System.out.println("SESSION CREATED: " + sessionId); 
        // ✅ AUTO FINISH session kalau user klik X
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                SessionDAO dao = new SessionDAO();
                dao.finishSession(sessionId);
            }
        });

        setLocationRelativeTo(null);
        lblWelcome.setText("Welcome, " + user.getUsername());
        setTitle("Save The System - Game");

//        nextProblem();
//        updateUI();
        
        generateNextProblem();
        updateUI();
}

    private void updateUI() {
    pbStability.setValue(stability);
    pbPerformance.setValue(performance);
    pbSecurity.setValue(security);

    lblRound.setText("Round: " + round);
    lblScore.setText("Score: " + score);
}



    private boolean checkEndGame() {
    // ✅ LOSE dulu
    if (stability <= 0 || performance <= 0 || security <= 0) {
        ScoreDAO scoreDAO = new ScoreDAO();
        scoreDAO.saveScore(sessionId, score, round - 1, "LOSE");

        SessionDAO sessionDAO = new SessionDAO();
        sessionDAO.finishSession(sessionId);

        GameOverFrame go = new GameOverFrame(currentUser, sessionId, score);
        go.setVisible(true);
        this.dispose();
        return true;
    }

    // ✅ WIN kalau sudah selesai ronde 10
    if ((round - 1) >= WIN_ROUND && security >= MIN_SECURITY) {
        ScoreDAO scoreDAO = new ScoreDAO();
        scoreDAO.saveScore(sessionId, score, round - 1, "WIN");

        SessionDAO sessionDAO = new SessionDAO();
        sessionDAO.finishSession(sessionId);

        YouWinFrame yw = new YouWinFrame(currentUser, sessionId, score);
        yw.setVisible(true);

        this.dispose();
        return true;
    }

    return false;
}

    private int clamp(int value) {
        if (value > 100) return 100;
        if (value < 0) return 0;
        return value;
}
        
//    private void nextProblem() {
//    Random rand = new Random();
//    String p = problems[rand.nextInt(problems.length)];
//    lblProblem.setText("Problem: " + p);
//}
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblWelcome = new javax.swing.JLabel();
        lblTitle = new javax.swing.JLabel();
        lblStability = new javax.swing.JLabel();
        pbStability = new javax.swing.JProgressBar();
        lblPerformance = new javax.swing.JLabel();
        pbPerformance = new javax.swing.JProgressBar();
        lblSecurity = new javax.swing.JLabel();
        pbSecurity = new javax.swing.JProgressBar();
        lblRound = new javax.swing.JLabel();
        lblScore = new javax.swing.JLabel();
        btnFix = new javax.swing.JButton();
        btnAddResource = new javax.swing.JButton();
        btnIgnore = new javax.swing.JButton();
        lblProblem = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        btnHelp = new javax.swing.JButton();
        btnLogout = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        lblWelcome.setFont(new java.awt.Font("OCR A Extended", 1, 24)); // NOI18N
        lblWelcome.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblWelcome.setText("Welcome !");

        lblTitle.setFont(new java.awt.Font("Juice ITC", 1, 48)); // NOI18N
        lblTitle.setText("SAVE THE SYSTEM");

        lblStability.setFont(new java.awt.Font("OCR A Extended", 0, 18)); // NOI18N
        lblStability.setText("Stability");

        pbStability.setValue(100);

        lblPerformance.setFont(new java.awt.Font("OCR A Extended", 0, 18)); // NOI18N
        lblPerformance.setText("Performance");

        pbPerformance.setValue(100);

        lblSecurity.setFont(new java.awt.Font("OCR A Extended", 0, 18)); // NOI18N
        lblSecurity.setText("Security");

        pbSecurity.setValue(100);

        lblRound.setFont(new java.awt.Font("OCR A Extended", 1, 24)); // NOI18N
        lblRound.setText("Round: 1");

        lblScore.setFont(new java.awt.Font("OCR A Extended", 1, 24)); // NOI18N
        lblScore.setText("Score: 0");

        btnFix.setFont(new java.awt.Font("OCR A Extended", 0, 24)); // NOI18N
        btnFix.setForeground(new java.awt.Color(0, 204, 0));
        btnFix.setText("Fix");
        btnFix.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFixActionPerformed(evt);
            }
        });

        btnAddResource.setFont(new java.awt.Font("OCR A Extended", 0, 24)); // NOI18N
        btnAddResource.setForeground(new java.awt.Color(0, 102, 255));
        btnAddResource.setText("Add Resource");
        btnAddResource.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddResourceActionPerformed(evt);
            }
        });

        btnIgnore.setFont(new java.awt.Font("OCR A Extended", 0, 24)); // NOI18N
        btnIgnore.setForeground(new java.awt.Color(204, 0, 0));
        btnIgnore.setText("Ignore");
        btnIgnore.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnIgnoreActionPerformed(evt);
            }
        });

        lblProblem.setFont(new java.awt.Font("OCR A Extended", 1, 24)); // NOI18N
        lblProblem.setText("Problem: Server overload detected! ");

        jButton1.setFont(new java.awt.Font("Eras Demi ITC", 1, 18)); // NOI18N
        jButton1.setForeground(new java.awt.Color(0, 102, 51));
        jButton1.setText("Leaderboard");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        btnHelp.setFont(new java.awt.Font("Eras Demi ITC", 1, 18)); // NOI18N
        btnHelp.setForeground(new java.awt.Color(204, 51, 0));
        btnHelp.setText("Help");
        btnHelp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnHelpActionPerformed(evt);
            }
        });

        btnLogout.setFont(new java.awt.Font("Eras Demi ITC", 1, 18)); // NOI18N
        btnLogout.setForeground(new java.awt.Color(204, 51, 0));
        btnLogout.setText("Logout");
        btnLogout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLogoutActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(209, 209, 209)
                .addComponent(lblProblem)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(49, 49, 49)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(btnFix)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btnAddResource)
                                .addGap(224, 224, 224)
                                .addComponent(btnIgnore))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(lblRound)
                                        .addGap(588, 588, 588)
                                        .addComponent(lblScore))
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                        .addGroup(layout.createSequentialGroup()
                                            .addComponent(btnHelp)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(btnLogout))
                                        .addGroup(layout.createSequentialGroup()
                                            .addComponent(lblStability)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(pbStability, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addGap(31, 31, 31)
                                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addGroup(layout.createSequentialGroup()
                                                    .addComponent(lblPerformance)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(pbPerformance, javax.swing.GroupLayout.PREFERRED_SIZE, 166, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addComponent(lblTitle))
                                            .addGap(18, 18, 18)
                                            .addComponent(lblSecurity)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(pbSecurity, javax.swing.GroupLayout.PREFERRED_SIZE, 187, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                .addGap(0, 0, Short.MAX_VALUE))))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(386, 386, 386)
                        .addComponent(lblWelcome))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(420, 420, 420)
                        .addComponent(jButton1)))
                .addGap(59, 59, 59))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnHelp)
                    .addComponent(btnLogout))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblTitle)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblWelcome)
                .addGap(128, 128, 128)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(lblStability)
                    .addComponent(pbStability, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblPerformance)
                    .addComponent(pbPerformance, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(pbSecurity, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblSecurity))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(42, 42, 42)
                        .addComponent(lblRound))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(54, 54, 54)
                        .addComponent(lblScore)))
                .addGap(40, 40, 40)
                .addComponent(lblProblem)
                .addGap(80, 80, 80)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnFix)
                    .addComponent(btnAddResource)
                    .addComponent(btnIgnore))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 120, Short.MAX_VALUE)
                .addComponent(jButton1)
                .addGap(103, 103, 103))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:
    int confirm = JOptionPane.showConfirmDialog(
        this,
        "Are you sure you want to log out?",
        "Logout Confirmation",
        JOptionPane.YES_NO_OPTION
    );

    if (confirm == JOptionPane.YES_OPTION) {
        SessionDAO sessionDAO = new SessionDAO();
        sessionDAO.finishSession(sessionId);

        LoginFrame lf = new LoginFrame();
        lf.setVisible(true);
        this.dispose();
    }

    }//GEN-LAST:event_jButton2ActionPerformed

    private void btnFixActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFixActionPerformed
        // TODO add your handling code here:
        applyDecision("FIX");
    }//GEN-LAST:event_btnFixActionPerformed

    private void btnAddResourceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddResourceActionPerformed
        // TODO add your handling code here:
        applyDecision("ADD_RESOURCE");
    }//GEN-LAST:event_btnAddResourceActionPerformed

    private void setTextWhite() {
    java.awt.Color white = java.awt.Color.WHITE;

    lblTitle.setForeground(white);
    lblWelcome.setForeground(white);
    lblStability.setForeground(white);
    lblPerformance.setForeground(white);
    lblSecurity.setForeground(white);
    lblRound.setForeground(white);
    lblScore.setForeground(white);
    lblProblem.setForeground(white);
}
    private void makeTransparent(java.awt.Container c) {
    for (java.awt.Component comp : c.getComponents()) {
        if (comp instanceof javax.swing.JComponent) {
            ((javax.swing.JComponent) comp).setOpaque(false);
        }
        if (comp instanceof java.awt.Container) {
            makeTransparent((java.awt.Container) comp);
        }
    }
}
    private void btnIgnoreActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnIgnoreActionPerformed
        // TODO add your handling code here:
        applyDecision("IGNORE");
    }//GEN-LAST:event_btnIgnoreActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        LeaderboardFrame lb = new LeaderboardFrame();
        lb.setVisible(true);
         // optional: supaya gameframe gak bisa diklik dulu
        this.setEnabled(false);

        // ketika leaderboard ditutup, gameframe aktif lagi
        lb.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                GameFrame.this.setEnabled(true);
                GameFrame.this.toFront();
        }
    });
    }//GEN-LAST:event_jButton1ActionPerformed

    private void btnHelpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnHelpActionPerformed
        // TODO add your handling code here:
        showHowToPlay();
    }//GEN-LAST:event_btnHelpActionPerformed

    private void btnLogoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLogoutActionPerformed
        // TODO add your handling code here:
        int confirm = JOptionPane.showConfirmDialog(
        this,
        "Are you sure you want to logout?",
        "Logout Confirmation",
        JOptionPane.YES_NO_OPTION
    );

    if (confirm == JOptionPane.YES_OPTION) {
        SessionDAO sessionDAO = new SessionDAO();
        sessionDAO.finishSession(sessionId);

        LoginFrame lf = new LoginFrame();
        lf.setVisible(true);
        this.dispose();
    }
    }//GEN-LAST:event_btnLogoutActionPerformed

    /**
     * @param args the command line arguments
     */
//    public static void main(String args[]) {
//        /* Set the Nimbus look and feel */
//        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
//        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
//         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
//         */
//        try {
//            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
//                if ("Nimbus".equals(info.getName())) {
//                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
//                    break;
//                }
//            }
//        } catch (ClassNotFoundException ex) {
//            java.util.logging.Logger.getLogger(GameFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        } catch (InstantiationException ex) {
//            java.util.logging.Logger.getLogger(GameFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        } catch (IllegalAccessException ex) {
//            java.util.logging.Logger.getLogger(GameFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
//            java.util.logging.Logger.getLogger(GameFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        }
//        //</editor-fold>
//
//        /* Create and display the form */
//        java.awt.EventQueue.invokeLater(new Runnable() {
//            public void run() {
//                new GameFrame().setVisible(true);
//            }
//        });
//    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddResource;
    private javax.swing.JButton btnFix;
    private javax.swing.JButton btnHelp;
    private javax.swing.JButton btnIgnore;
    private javax.swing.JButton btnLogout;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel lblPerformance;
    private javax.swing.JLabel lblProblem;
    private javax.swing.JLabel lblRound;
    private javax.swing.JLabel lblScore;
    private javax.swing.JLabel lblSecurity;
    private javax.swing.JLabel lblStability;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JLabel lblWelcome;
    private javax.swing.JProgressBar pbPerformance;
    private javax.swing.JProgressBar pbSecurity;
    private javax.swing.JProgressBar pbStability;
    // End of variables declaration//GEN-END:variables
}
