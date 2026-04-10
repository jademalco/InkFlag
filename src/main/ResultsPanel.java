package main;

import java.awt.*;
import javax.swing.*;
import main.MenuPanel.MenuResult;

public class ResultsPanel extends JPanel {
        
    private Image bgImage = new ImageIcon("res/rbg.png").getImage();

    public ResultsPanel(MenuResult result, int[] scores, java.util.function.Consumer<MenuResult> onRematch, Runnable onMenu, GamePanel gp) {
        setLayout(null);
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

        // New way (Replace "res/play_again.png" with your actual file names)
        JButton rematchBtn = createCustomBtn("res/playagain.png", centerX, 280, btnW, btnH);
        rematchBtn.addActionListener(e -> {
            playSound("res/click.wav");
            onRematch.accept(result);
        });
        add(rematchBtn);

        JButton levelBtn = createCustomBtn("res/changelvl.png", centerX, 350, btnW, btnH);
        levelBtn.addActionListener(e -> {
            playSound("res/click.wav");
            gp.returnToMenu(true, result);
        });
        add(levelBtn);

        JButton menuBtn = createCustomBtn("res/backtomm.png", centerX, 420, btnW, btnH);
        menuBtn.addActionListener(e -> {
            playSound("res/click.wav");
            onMenu.run();
        });
        add(menuBtn);
        }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (bgImage != null) {
            g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
        }
    }

    private void playSound(String soundFile) {
    try {
        java.io.File file = new java.io.File(soundFile);
        if (file.exists()) {
            javax.sound.sampled.AudioInputStream audioInput = javax.sound.sampled.AudioSystem.getAudioInputStream(file);
            javax.sound.sampled.Clip clip = javax.sound.sampled.AudioSystem.getClip();
            clip.open(audioInput);
            clip.start();
        } else {
            System.out.println("Sound file not found: " + soundFile);
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
}

    private JButton createCustomBtn(String imgPath, int x, int y, int w, int h) {
    // 1. Load the image
    ImageIcon icon = new ImageIcon(imgPath);
    
    // This gets the actual width and height of your PNG file
    //int w = icon.getIconWidth();
    //int h = icon.getIconHeight();

    JButton b = new JButton(icon);
    int centeredX = (GamePanel.SCREEN_W / 2) - (w / 2);
    b.setBounds(centeredX, y, w, h);
    
    // 2. Remove all default Java styling so ONLY the PNG shows
    b.setBorderPainted(false);
    b.setContentAreaFilled(false);
    b.setFocusPainted(false);
    b.setOpaque(false);
    b.setCursor(new Cursor(Cursor.HAND_CURSOR));

    // 3. Optional: Add a "Hover" effect if you have a second PNG
    // b.setRolloverIcon(new ImageIcon("res/btn_hover.png"));
    
    return b;
}
}