/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package savethesystem.ui;
import java.awt.*;
import javax.swing.JPanel;
import java.awt.LinearGradientPaint;
import java.awt.Point;
/**
 *
 * @author LENOVO
 */
public class GradientPanel extends JPanel {
   private float[] dist;
    private Color[] colors;
    // Default (kalau gak dikasih warna)
    public GradientPanel() {
        this(
            new float[]{0.0f, 0.6f, 1.0f},
            new Color[]{
                new Color(11, 16, 32),   // biru gelap
                new Color(15, 42, 95),   // biru deep
                new Color(14, 165, 233)  // cyan
            }
        );
    }
    public GradientPanel(float[] dist, Color[] colors) {
        setOpaque(true);
        setFocusable(false);
        this.dist = dist;
        this.colors = colors;
    }

@Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        LinearGradientPaint lgp = new LinearGradientPaint(
                new Point(0, 0),
                new Point(0, getHeight()),
                dist,
                colors
        );

        g2.setPaint(lgp);
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
    }
}