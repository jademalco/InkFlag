package main;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import main.MenuPanel.MenuResult;
import main.entity.Player;
/**
 * GameCanvas handles the game loop, update logic, and all rendering.
 * It is a JPanel added to GamePanel's CardLayout when a game starts.
 */
public class GameCanvas extends JPanel implements Runnable {

    // ── Back-reference to swap cards ─────────────────────────────────────────
    private final GamePanel gp;

    // ── Screen ────────────────────────────────────────────────────────────────
    public final int tileSize    = GamePanel.TILE_SIZE;
    public final int maxScreenCol = GamePanel.MAX_COL;
    public final int maxScreenRow = GamePanel.MAX_ROW;
    public final int screenWidth  = GamePanel.SCREEN_W;
    public final int screenHeight = GamePanel.SCREEN_H;

    // ── Flag zone ─────────────────────────────────────────────────────────────
    final int flagCols = 2;
    final int flagRows = 2;
    final int flagCol  = (maxScreenCol / 2) - (flagCols / 2);
    final int flagRow  = (maxScreenRow / 2) - (flagRows / 2);

    // ── Game state ────────────────────────────────────────────────────────────
    enum State { PLAYING, GAME_OVER }
    State state = State.PLAYING;

    // ── Ink ───────────────────────────────────────────────────────────────────
    public int[][] inkGrid = new int[maxScreenRow][maxScreenCol];

    // ── Timer ─────────────────────────────────────────────────────────────────
    public int secondsLeft = 120;
    private long timerAccumulator = 0;
    public boolean gameOver = false;

    // ── Session info ──────────────────────────────────────────────────────────
    public int currentLevel;
    public Color p1Color, p2Color;
    public String p1Name, p2Name;
    private final MenuResult lastResult;

    // ── Power-ups ─────────────────────────────────────────────────────────────
    private List<PowerUp> powerUps = new ArrayList<>();
    private static final int POWERUP_COUNT = 8;
    private static final int SPLASH_RADIUS = 2;
    private static final int BOMB_RADIUS   = 2;

    // ── Collision cooldown ────────────────────────────────────────────────────
    private int collisionCooldown = 0;
    private static final int COLLISION_CD = 60;

    // ── Input & players ───────────────────────────────────────────────────────
    KeyHandler keyH = new KeyHandler();
    public Player player1, player2;

    // ── Game loop ─────────────────────────────────────────────────────────────
    private Thread gameThread;
    private static final int FPS = 60;
    private volatile boolean running = false;

    // ── Post-game overlay shown flag ──────────────────────────────────────────
    private boolean overlayShown = false;

    public GameCanvas(MenuResult result, GamePanel gp) {
        this.gp = gp;
        this.lastResult = result;
        setPreferredSize(new Dimension(screenWidth, screenHeight));
        setBackground(Color.WHITE);
        setDoubleBuffered(true);
        addKeyListener(keyH);
        setFocusable(true);
        applyResult(result);
        initGame();
    }

    private void applyResult(MenuResult r) {
        p1Name       = r.p1Name;
        p2Name       = r.p2Name;
        p1Color      = r.p1Color;
        p2Color      = r.p2Color;
        currentLevel = r.level;
    }

    private void initGame() {
        inkGrid          = new int[maxScreenRow][maxScreenCol];
        secondsLeft      = 120;
        gameOver         = false;
        timerAccumulator = 0;
        collisionCooldown = 0;
        overlayShown     = false;
        powerUps.clear();
        state = State.PLAYING;

        player1 = new Player(this, keyH, true);
        player2 = new Player(this, keyH, false);
        player1.trailColor = p1Color;
        player2.trailColor = p2Color;

        if (currentLevel >= 2) spawnPowerUps();
    }

    private void spawnPowerUps() {
        powerUps = PowerUp.spawnRandom(POWERUP_COUNT, maxScreenCol, maxScreenRow,
                                       tileSize, flagCol, flagRow, flagCols, flagRows);
    }

    // ── Thread ────────────────────────────────────────────────────────────────

    public void startGameThread() {
        running = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    public void stopThread() {
        running = false;
        if (gameThread != null) gameThread.interrupt();
    }

    @Override
    public void run() {
        double drawInterval = 1_000_000_000.0 / FPS;
        double delta  = 0;
        long lastTime = System.nanoTime();
        long fpsTimer = 0;
        int  frames   = 0;

        while (running) {
            long now     = System.nanoTime();
            long elapsed = now - lastTime;
            lastTime     = now;

            delta            += elapsed / drawInterval;
            fpsTimer         += elapsed;
            timerAccumulator += elapsed;

            if (timerAccumulator >= 1_000_000_000L) {
                timerAccumulator -= 1_000_000_000L;
                if (state == State.PLAYING && !gameOver && secondsLeft > 0) {
                    secondsLeft--;
                    if (secondsLeft == 0) gameOver = true;
                }
            }

            if (delta >= 1) {
                update();
                repaint();
                delta--;
                frames++;
            }

            if (fpsTimer >= 1_000_000_000L) {
                System.out.println("FPS: " + frames);
                frames   = 0;
                fpsTimer = 0;
            }
        }
    }

    // ── Update ────────────────────────────────────────────────────────────────

    private void update() {
        if (state == State.PLAYING) {
            if (gameOver) {
                state = State.GAME_OVER;
                if (!overlayShown) {
                    overlayShown = true;
                    SwingUtilities.invokeLater(this::showPostGameOverlay);
                }
                return;
            }

            player1.update();
            player2.update();

            paintInk(player1.x + tileHalf(), player1.y + tileHalf(), 1);
            paintInk(player2.x + tileHalf(), player2.y + tileHalf(), 2);

            if (currentLevel == 1 && playersOverlap()) {
                eraseCellUnder(player1.x + tileHalf(), player1.y + tileHalf());
                eraseCellUnder(player2.x + tileHalf(), player2.y + tileHalf());
            }

            if (currentLevel >= 2) {
                checkPowerUps();
                if (powerUps.stream().noneMatch(p -> p.active)) spawnPowerUps();
            }

            if (currentLevel == 3) checkPlayerCollision();

            if (collisionCooldown > 0) collisionCooldown--;
        }
    }

    // ── Power-ups ─────────────────────────────────────────────────────────────

    private void checkPowerUps() {
        for (PowerUp pu : powerUps) {
            if (!pu.active) continue;
            if (playerTouches(player1, pu)) { applyPowerUp(player1, 1, pu); pu.active = false; }
            else if (playerTouches(player2, pu)) { applyPowerUp(player2, 2, pu); pu.active = false; }
        }
    }

    private boolean playerTouches(Player p, PowerUp pu) {
        return p.x < pu.x + tileSize && p.x + tileSize > pu.x &&
               p.y < pu.y + tileSize && p.y + tileSize > pu.y;
    }

    private void applyPowerUp(Player p, int owner, PowerUp pu) {
        switch (pu.type) {
            case SPEED -> p.activateSpeed();
            case SPLASH_BOMB -> applyRadiusInk(p.x + tileHalf(), p.y + tileHalf(), SPLASH_RADIUS, owner);
            case BOMB -> eraseRadiusInk(p.x + tileHalf(), p.y + tileHalf(), BOMB_RADIUS, owner);
        }
    }

    private void applyRadiusInk(int px, int py, int radius, int owner) {
        int cc = px / tileSize, cr = py / tileSize;
        for (int dr = -radius; dr <= radius; dr++)
            for (int dc = -radius; dc <= radius; dc++)
                if (dr*dr + dc*dc <= radius*radius) {
                    int r = cr+dr, c = cc+dc;
                    if (r>=0 && r<maxScreenRow && c>=0 && c<maxScreenCol) inkGrid[r][c] = owner;
                }
    }

    private void eraseRadiusInk(int px, int py, int radius, int owner) {
        int cc = px / tileSize, cr = py / tileSize;
        for (int dr = -radius; dr <= radius; dr++)
            for (int dc = -radius; dc <= radius; dc++)
                if (dr*dr + dc*dc <= radius*radius) {
                    int r = cr+dr, c = cc+dc;
                    if (r>=0 && r<maxScreenRow && c>=0 && c<maxScreenCol && inkGrid[r][c]==owner)
                        inkGrid[r][c] = 0;
                }
    }

    // ── Level 3 collision ─────────────────────────────────────────────────────

    private void checkPlayerCollision() {
        if (collisionCooldown > 0 || !playersOverlap()) return;
        collisionCooldown = COLLISION_CD;
        player1.triggerHit();
        player2.triggerHit();
        eraseRadiusInk(player1.x + tileHalf(), player1.y + tileHalf(), 1, 1);
        eraseRadiusInk(player2.x + tileHalf(), player2.y + tileHalf(), 1, 2);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private int tileHalf() { return tileSize / 2; }

    void paintInk(int px, int py, int owner) {
        int col = px / tileSize, row = py / tileSize;
        if (row>=0 && row<maxScreenRow && col>=0 && col<maxScreenCol) inkGrid[row][col] = owner;
    }

    void eraseCellUnder(int px, int py) {
        int col = px / tileSize, row = py / tileSize;
        if (row>=0 && row<maxScreenRow && col>=0 && col<maxScreenCol) inkGrid[row][col] = 0;
    }

    boolean playersOverlap() {
        return Math.abs(player1.x - player2.x) < tileSize &&
               Math.abs(player1.y - player2.y) < tileSize;
    }

    public Color getFlagColor() {
        int[] cov = getCoverage();
        if (cov[0] > cov[1]) return p1Color;
        if (cov[1] > cov[0]) return p2Color;
        return new Color(200, 200, 200);
    }

    public int[] getCoverage() {
        int p1 = 0, p2 = 0;
        for (int[] row : inkGrid)
            for (int cell : row) { if (cell==1) p1++; else if (cell==2) p2++; }
        return new int[]{p1, p2};
    }

    // ── Rendering ─────────────────────────────────────────────────────────────

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // Ink grid
        for (int row = 0; row < maxScreenRow; row++) {
            for (int col = 0; col < maxScreenCol; col++) {
                if (inkGrid[row][col] == 1) {
                    g2.setColor(new Color(p1Color.getRed(), p1Color.getGreen(), p1Color.getBlue(), 160));
                    g2.fillRect(col*tileSize, row*tileSize, tileSize, tileSize);
                } else if (inkGrid[row][col] == 2) {
                    g2.setColor(new Color(p2Color.getRed(), p2Color.getGreen(), p2Color.getBlue(), 160));
                    g2.fillRect(col*tileSize, row*tileSize, tileSize, tileSize);
                }
            }
        }

        // Flag zone
        int fpx = flagCol*tileSize, fpy = flagRow*tileSize;
        int fpw = flagCols*tileSize, fph = flagRows*tileSize;
        g2.setColor(getFlagColor());
        g2.fillRect(fpx, fpy, fpw, fph);
        g2.setColor(Color.DARK_GRAY);
        g2.setStroke(new BasicStroke(3));
        g2.drawRect(fpx, fpy, fpw, fph);

        // Power-ups
        if (currentLevel >= 2)
            for (PowerUp pu : powerUps) pu.draw(g2);

        // Players
        if (player1 != null) player1.draw(g2);
        if (player2 != null) player2.draw(g2);

        // HUD
        drawHUD(g2);
        g2.dispose();
    }

    private void drawHUD(Graphics2D g2) {
        int[] cov  = getCoverage();
        int   total = maxScreenCol * maxScreenRow;

        // Timer
        g2.setColor(Color.DARK_GRAY);
        g2.setFont(new Font("Arial", Font.BOLD, 24));
        drawCentered(g2, String.format("%d:%02d", secondsLeft/60, secondsLeft%60), screenWidth/2, 32);

        // Level badge
        g2.setFont(new Font("Arial", Font.BOLD, 14));
        g2.setColor(new Color(100, 100, 140));
        drawCentered(g2, "LVL " + currentLevel, screenWidth/2, 52);

        // P1 bar (left)
        int barW = 200, barH = 18, barY = 8;
        g2.setColor(new Color(220, 220, 220));
        g2.fillRect(10, barY, barW, barH);
        g2.setColor(p1Color);
        g2.fillRect(10, barY, (int)(barW * ((float)cov[0]/total)), barH);
        g2.setColor(Color.DARK_GRAY);
        g2.setStroke(new BasicStroke(1));
        g2.drawRect(10, barY, barW, barH);
        g2.setFont(new Font("Arial", Font.BOLD, 14));
        g2.drawString(p1Name + ": " + (100*cov[0]/total) + "%", 14, barY+14);
        if (player1 != null && player1.speedActive) {
            g2.setColor(new Color(255,200,0));
            g2.setFont(new Font("Arial", Font.BOLD, 12));
            g2.drawString("SPEED", 14, barY+barH+14);
        }

        // P2 bar (right)
        int p2x = screenWidth - 10 - barW;
        g2.setColor(new Color(220, 220, 220));
        g2.fillRect(p2x, barY, barW, barH);
        g2.setColor(p2Color);
        g2.fillRect(p2x, barY, (int)(barW * ((float)cov[1]/total)), barH);
        g2.setColor(Color.DARK_GRAY);
        g2.drawRect(p2x, barY, barW, barH);
        g2.setFont(new Font("Arial", Font.BOLD, 14));
        String p2text = p2Name + ": " + (100*cov[1]/total) + "%";
        g2.drawString(p2text, p2x + barW - g2.getFontMetrics().stringWidth(p2text) - 4, barY+14);
        if (player2 != null && player2.speedActive) {
            g2.setColor(new Color(255,200,0));
            g2.setFont(new Font("Arial", Font.BOLD, 12));
            String spd = "SPEED";
            g2.drawString(spd, p2x + barW - g2.getFontMetrics().stringWidth(spd) - 4, barY+barH+14);
        }

        // Power-up legend
        if (currentLevel >= 2) {
            int lx = screenWidth/2 - 80, ly = screenHeight - 22;
            g2.setFont(new Font("Arial", Font.PLAIN, 11));
            g2.setColor(new Color(255,220,0)); g2.fillOval(lx, ly, 10, 10);
            g2.setColor(Color.DARK_GRAY);      g2.drawString("Speed",  lx+13, ly+10);
            g2.setColor(new Color(0,200,80));  g2.fillOval(lx+65, ly, 10, 10);
            g2.setColor(Color.DARK_GRAY);      g2.drawString("Splash", lx+78, ly+10);
            g2.setColor(new Color(30,30,30));  g2.fillOval(lx+135, ly, 10, 10);
            g2.setColor(Color.DARK_GRAY);      g2.drawString("Erase",  lx+148, ly+10);
        }
    }

    private void drawCentered(Graphics2D g2, String text, int cx, int y) {
        g2.drawString(text, cx - g2.getFontMetrics().stringWidth(text)/2, y);
    }

    // ── Post-game overlay ─────────────────────────────────────────────────────

    private void showPostGameOverlay() {
        int[]  cov   = getCoverage();
        int    total = maxScreenCol * maxScreenRow;
        String winner = cov[0] > cov[1] ? p1Name + " Wins!" :
                        cov[1] > cov[0] ? p2Name + " Wins!" : "It's a Draw!";
        String score  = String.format("%s %d%%  —  %s %d%%",
            p1Name, 100*cov[0]/total, p2Name, 100*cov[1]/total);
        Color winColor = cov[0] > cov[1] ? p1Color : cov[1] > cov[0] ? p2Color : Color.DARK_GRAY;

        // Full-screen semi-transparent overlay
        JPanel overlay = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(255, 255, 255, 200));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        overlay.setOpaque(false);
        overlay.setBounds(0, 0, screenWidth, screenHeight);

        // Centered white card
        int cw = 380, ch = 250;
        JPanel card = new JPanel(null);
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 2));
        card.setBounds(screenWidth/2 - cw/2, screenHeight/2 - ch/2, cw, ch);

        JLabel winLbl = new JLabel(winner, SwingConstants.CENTER);
        winLbl.setFont(new Font("Arial", Font.BOLD, 30));
        winLbl.setForeground(winColor);
        winLbl.setBounds(10, 14, cw-20, 38);
        card.add(winLbl);

        JLabel scoreLbl = new JLabel(score, SwingConstants.CENTER);
        scoreLbl.setFont(new Font("Arial", Font.PLAIN, 14));
        scoreLbl.setForeground(Color.DARK_GRAY);
        scoreLbl.setBounds(10, 56, cw-20, 22);
        card.add(scoreLbl);

        JLabel askLbl = new JLabel("What would you like to do?", SwingConstants.CENTER);
        askLbl.setFont(new Font("Arial", Font.PLAIN, 12));
        askLbl.setForeground(Color.GRAY);
        askLbl.setBounds(10, 82, cw-20, 18);
        card.add(askLbl);

        // Play Again
        JButton playAgainBtn = postBtn("Play Again", new Color(0x00B487), Color.WHITE);
        playAgainBtn.setBounds(20, 112, cw-40, 36);
        playAgainBtn.addActionListener(e -> {
            setLayout(null);
            remove(overlay);
            initGame();
            repaint();
            requestFocusInWindow();
        });
        card.add(playAgainBtn);

        // Change Level
        JButton levelBtn = postBtn("Change Level", new Color(0xFFC549), Color.DARK_GRAY);
        levelBtn.setBounds(20, 156, cw-40, 36);
        levelBtn.addActionListener(e -> gp.returnToMenu(true, lastResult));
        card.add(levelBtn);

        // Main Menu
        JButton menuBtn = postBtn("Main Menu", new Color(0xF23F3A), Color.WHITE);
        menuBtn.setBounds(20, 200, cw-40, 36);
        menuBtn.addActionListener(e -> gp.returnToMenu(false, null));
        card.add(menuBtn);

        overlay.add(card);
        setLayout(null);
        add(overlay);
        revalidate();
        repaint();
    }

    private JButton postBtn(String text, Color bg, Color fg) {
        JButton b = new JButton(text);
        b.setFont(new Font("Arial", Font.BOLD, 14));
        b.setBackground(bg);
        b.setForeground(fg);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }
}