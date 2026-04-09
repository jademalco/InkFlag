package main;

import java.awt.*;
import javax.swing.*;
import main.MenuPanel.MenuResult;

public class ResultsPanel extends JPanel {

    public ResultsPanel(MenuResult result, int[] scores, java.util.function.Consumer<MenuResult> onRematch, Runnable onMenu, GamePanel gp) {
        // 1. Setup Panel
        setLayout(null); 
        setBackground(new Color(245, 245, 245)); // Light clean gray
        setPreferredSize(new Dimension(GamePanel.SCREEN_W, GamePanel.SCREEN_H));

        // 2. Calculate Stats
        int totalCells = GamePanel.MAX_COL * GamePanel.MAX_ROW;
        int p1Percent = (scores[0] * 100) / totalCells;
        int p2Percent = (scores[1] * 100) / totalCells;

        // 3. Winner Announcement
        String winnerText;
        Color winnerColor;
        if (p1Percent > p2Percent) {
            winnerText = result.p1Name + " WINS!";
            winnerColor = result.p1Color;
        } else if (p2Percent > p1Percent) {
            winnerText = result.p2Name + " WINS!";
            winnerColor = result.p2Color;
        } else {
            winnerText = "IT'S A DRAW!";
            winnerColor = Color.DARK_GRAY;
        }

        JLabel winLbl = new JLabel(winnerText, SwingConstants.CENTER);
        winLbl.setFont(new Font("Arial", Font.BOLD, 50));
        winLbl.setForeground(winnerColor);
        winLbl.setBounds(0, 80, GamePanel.SCREEN_W, 60);
        add(winLbl);

        // 4. Score Detail Label
        JLabel scoreLbl = new JLabel(p1Percent + "%  vs  " + p2Percent + "%", SwingConstants.CENTER);
        scoreLbl.setFont(new Font("Arial", Font.PLAIN, 24));
        scoreLbl.setForeground(Color.GRAY);
        scoreLbl.setBounds(0, 150, GamePanel.SCREEN_W, 30);
        add(scoreLbl);

        // 5. Action Buttons
        int btnW = 250;
        int btnH = 50;
        int centerX = (GamePanel.SCREEN_W / 2) - (btnW / 2);

        // Rematch Button
        JButton rematchBtn = createStyledBtn("PLAY AGAIN", new Color(0x00B487), centerX, 280, btnW, btnH);
        rematchBtn.addActionListener(e -> onRematch.accept(result));
        add(rematchBtn);

        // Change Level (Goes back to Menu but jumps to level select)
        JButton levelBtn = createStyledBtn("CHANGE LEVEL", new Color(0xFFC549), centerX, 350, btnW, btnH);
        levelBtn.setForeground(Color.DARK_GRAY); // Make text dark for yellow button
        levelBtn.addActionListener(e -> gp.returnToMenu(true, result));
        add(levelBtn);

        // Main Menu Button
        JButton menuBtn = createStyledBtn("EXIT TO MENU", new Color(0xF23F3A), centerX, 420, btnW, btnH);
        menuBtn.addActionListener(e -> onMenu.run());
        add(menuBtn);
    }

    private JButton createStyledBtn(String text, Color bg, int x, int y, int w, int h) {
        JButton b = new JButton(text);
        b.setBounds(x, y, w, h);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Arial", Font.BOLD, 16));
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder());
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }
}