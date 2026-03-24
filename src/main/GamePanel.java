package main;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JPanel;
import main.entity.Player;

public class GamePanel extends JPanel implements Runnable {

    public enum GameState {
        PLAYING, GAME_OVER
    }
    public GameState gameState = GameState.PLAYING;

    // Screen settings
    final int originalTileSize = 16;
    final int scale = 3;
    public final int tileSize     = originalTileSize * scale;
    final int maxScreenCol        = 16;
    final int maxScreenRow        = 12;
    public final int screenWidth  = tileSize * maxScreenCol;
    public final int screenHeight = tileSize * maxScreenRow;

    // Ink map
    public int[][] inkGrid = new int[maxScreenRow][maxScreenCol];

    // Tile in the middle
    final int flagCols = 2;
    final int flagRows = 2;
    final int flagCol  = (maxScreenCol / 2) - (flagCols / 2);
    final int flagRow  = (maxScreenRow / 2) - (flagRows / 2);

    // Game timer
    public int secondsLeft = 120;
    private long timerAccumulator = 0;
    public boolean gameOver = false;

    // PLAYER COLORS
    public final Color p1Color = new Color(0xFC83DA);
    public final Color p2Color = new Color(0xD096FF);

    int FPS = 60;

    KeyHandler keyH = new KeyHandler();
    Thread gameThread;

    public Player player1;
    public Player player2;

    public GamePanel() {
        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setBackground(Color.WHITE);
        this.setDoubleBuffered(true);
        this.addKeyListener(keyH);
        this.setFocusable(true);

        player1 = new Player(this, keyH, true);
        player2 = new Player(this, keyH, false);
    }

    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        double drawInterval = 1_000_000_000.0 / FPS;
        double delta   = 0;
        long lastTime  = System.nanoTime();
        long timer     = 0;
        int  drawCount = 0;

        while (gameThread != null) {
            long currentTime = System.nanoTime();
            long elapsed     = currentTime - lastTime;
            lastTime         = currentTime;

            delta            += elapsed / drawInterval;
            timer            += elapsed;
            timerAccumulator += elapsed;

            if (timerAccumulator >= 1_000_000_000L) {
                timerAccumulator -= 1_000_000_000L;
                if (!gameOver && secondsLeft > 0) {
                    secondsLeft--;
                    if (secondsLeft == 0) gameOver = true;
                }
            }

            if (delta >= 1) {
                update();
                repaint();
                delta--;
                drawCount++;
            }

            if (timer >= 1_000_000_000L) {
                System.out.println("FPS: " + drawCount);
                drawCount = 0;
                timer = 0;
            }
        }
    }

    public void update() {
        if (gameState == GameState.PLAYING) {
            if (gameOver) { gameState = GameState.GAME_OVER; return; }

            player1.update();
            player2.update();

            // FIX: use the CENTER of the player to determine which tile they're on
            paintInk(player1.x + gp_tileHalf(), player1.y + gp_tileHalf(), 1);
            paintInk(player2.x + gp_tileHalf(), player2.y + gp_tileHalf(), 2);

            if (playersOverlap()) {
                eraseCellUnder(player1.x + gp_tileHalf(), player1.y + gp_tileHalf());
                eraseCellUnder(player2.x + gp_tileHalf(), player2.y + gp_tileHalf());
            }
            return;
        }

        if (gameState == GameState.GAME_OVER) {
            if (keyH.enterPressed) {
                inkGrid     = new int[maxScreenRow][maxScreenCol];
                secondsLeft = 120;
                gameOver    = false;
                player1     = new Player(this, keyH, true);
                player2     = new Player(this, keyH, false);
                gameState   = GameState.PLAYING;
            }
        }
    }

    private int gp_tileHalf() {
        return tileSize / 2;
    }

    void paintInk(int px, int py, int owner) {
        int col = px / tileSize;
        int row = py / tileSize;
        if (row >= 0 && row < maxScreenRow && col >= 0 && col < maxScreenCol)
            inkGrid[row][col] = owner;
    }

    void eraseCellUnder(int px, int py) {
        int col = px / tileSize;
        int row = py / tileSize;
        if (row >= 0 && row < maxScreenRow && col >= 0 && col < maxScreenCol)
            inkGrid[row][col] = 0;
    }

    boolean playersOverlap() {
        return Math.abs(player1.x - player2.x) < tileSize &&
               Math.abs(player1.y - player2.y) < tileSize;
    }

    // Flag shows the color of the dominant player in the map, grey if equal
    public Color getFlagColor() {
        int[] cov = getCoverage();
        if (cov[0] > cov[1]) return p1Color;
        if (cov[1] > cov[0]) return p2Color;
        return new Color(200, 200, 200); // gray when tied
    }

    public int[] getCoverage() {
        int p1 = 0, p2 = 0;
        for (int[] row : inkGrid)
            for (int cell : row) {
                if (cell == 1) p1++;
                else if (cell == 2) p2++;
            }
        return new int[]{p1, p2};
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // Draw ink grid — each cell aligned to tiles
        for (int row = 0; row < maxScreenRow; row++) {
            for (int col = 0; col < maxScreenCol; col++) {
                if (inkGrid[row][col] == 1) {
                    g2.setColor(new Color(p1Color.getRed(), p1Color.getGreen(), p1Color.getBlue(), 160));
                    g2.fillRect(col * tileSize, row * tileSize, tileSize, tileSize);
                } else if (inkGrid[row][col] == 2) {
                    g2.setColor(new Color(p2Color.getRed(), p2Color.getGreen(), p2Color.getBlue(), 160));
                    g2.fillRect(col * tileSize, row * tileSize, tileSize, tileSize);
                }
            }
        }

        // Draw flag zone
        int flagPixelX = flagCol * tileSize;
        int flagPixelY = flagRow * tileSize;
        int flagPixelW = flagCols * tileSize;
        int flagPixelH = flagRows * tileSize;
        g2.setColor(getFlagColor());
        g2.fillRect(flagPixelX, flagPixelY, flagPixelW, flagPixelH);
        g2.setColor(Color.DARK_GRAY);
        g2.setStroke(new BasicStroke(3));
        g2.drawRect(flagPixelX, flagPixelY, flagPixelW, flagPixelH);

        // Draw players
        if (player1 != null) player1.draw(g2);
        if (player2 != null) player2.draw(g2);

        // HUD on top
        drawHUD(g2);

        g2.dispose();
    }

    private void drawHUD(Graphics2D g2) {
        int[] cov = getCoverage();
        int total = maxScreenCol * maxScreenRow;

        // Timer
        g2.setColor(Color.DARK_GRAY);
        g2.setFont(new Font("Arial", Font.BOLD, 20));
        int mins = secondsLeft / 60;
        int secs = secondsLeft % 60;
        drawCentered(g2, String.format("%d:%02d", mins, secs), screenWidth / 2, 28);

        // P1 bar (left)
        int barW = 150, barH = 14, barY = 10;
        g2.setColor(new Color(220, 220, 220));
        g2.fillRect(10, barY, barW, barH);
        g2.setColor(p1Color);
        g2.fillRect(10, barY, (int)(barW * ((float) cov[0] / total)), barH);
        g2.setColor(Color.DARK_GRAY);
        g2.setFont(new Font("Arial", Font.PLAIN, 12));
        g2.drawString("P1: " + (100 * cov[0] / total) + "%", 14, barY + 11);

        // P2 bar (right)
        g2.setColor(new Color(220, 220, 220));
        g2.fillRect(screenWidth - 10 - barW, barY, barW, barH);
        g2.setColor(p2Color);
        g2.fillRect(screenWidth - 10 - barW, barY, (int)(barW * ((float) cov[1] / total)), barH);
        g2.setColor(Color.DARK_GRAY);
        g2.drawString("P2: " + (100 * cov[1] / total) + "%", screenWidth - 10 - barW + 4, barY + 11);

        // Game over overlay
        if (gameState == GameState.GAME_OVER) {
            g2.setColor(new Color(255, 255, 255, 210));
            g2.fillRect(0, 0, screenWidth, screenHeight);
            int[] finalCov = getCoverage();
            String winner = finalCov[0] > finalCov[1] ? "Player 1 Wins!" :
                            finalCov[1] > finalCov[0] ? "Player 2 Wins!" : "It's a Draw!";
            g2.setColor(finalCov[0] > finalCov[1] ? p1Color :
                        finalCov[1] > finalCov[0] ? p2Color : Color.DARK_GRAY);
            g2.setFont(new Font("Arial", Font.BOLD, 40));
            drawCentered(g2, winner, screenWidth / 2, screenHeight / 2 - 20);
            g2.setColor(Color.DARK_GRAY);
            g2.setFont(new Font("Arial", Font.PLAIN, 18));
            drawCentered(g2, "Press Enter to play again", screenWidth / 2, screenHeight / 2 + 24);
        }
    }

    private void drawCentered(Graphics2D g2, String text, int cx, int y) {
        int x = cx - g2.getFontMetrics().stringWidth(text) / 2;
        g2.drawString(text, x, y);
    }
}