package main;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;
import javax.imageio.ImageIO;
import javax.swing.*;

/**
 * MenuPanel — shown before the game starts.
 * Collects: character (boy/girl), trail color, player name, and level for each player.
 */
public class MenuPanel extends JPanel {

    // Callback fired when both players are ready
    private final Consumer<MenuResult> onReady;

    // ── State ─────────────────────────────────────────────────────────────────
    private Color p1Trail    = new Color(0xF23F3A);
    private int   p1ColorIdx = 0;

    private Color p2Trail    = new Color(0x775AFF);
    private int   p2ColorIdx = 5;

    private int selectedLevel = 1;

    // Menu page: 0=P1 setup  1=P2 setup  2=level select
    private int page = 0;

    // Add these new fields here:
    private final Image p1bg; // Your Player 1 background
    private final Image p2bg; // Your Player 2 background
    private Image currentBG; // The "active" background

    // Text fields
    private final JTextField p1NameField = new JTextField("Player 1", 10);
    private final JTextField p2NameField = new JTextField("Player 2", 10);

    private static final Color[] TRAIL_COLORS = {
        new Color(0xF23F3A),
        new Color(0xFF76B4),
        new Color(0xFFC549),
        new Color(0x00B487),
        new Color(0x6DD0F0),
        new Color(0x775AFF)
    };
    private static final String[] COLOR_NAMES = {
        "Red", "Pink", "Yellow", "Green", "Sky", "Purple"
    };

    public MenuPanel(int width, int height, Consumer<MenuResult> onReady) {
        this.onReady = onReady;
        setPreferredSize(new Dimension(width, height));
        setBackground(Color.WHITE);

        // Load your actual files from res/
        try {
            p1bg = ImageIO.read(new File("res/p1bg.png"));
            p2bg = ImageIO.read(new File("res/p2bg.png"));
        } catch (IOException ex) {
            throw new RuntimeException("Unable to load menu background images", ex);
        }

        // Start by pointing to the first background
        currentBG = p1bg;

        setLayout(null);
        buildPage();
    }

    /** Jump straight to the level-select page (called from post-game overlay). */
    public void goToLevelPage() {
        page = 2;
        buildPage();
    }

    /** Pre-fill player names and colors from a previous session. */
    public void prefill(MenuResult r) {
        p1NameField.setText(r.p1Name);
        p2NameField.setText(r.p2Name);
        p1Trail = r.p1Color;
        p2Trail = r.p2Color;
        // find matching color index
        for (int i = 0; i < TRAIL_COLORS.length; i++) {
            if (TRAIL_COLORS[i].equals(r.p1Color)) p1ColorIdx = i;
            if (TRAIL_COLORS[i].equals(r.p2Color)) p2ColorIdx = i;
        }
    }

    // ── Build UI pages ────────────────────────────────────────────────────────

    private void buildPage() {
        removeAll();
        switch (page) {
            case 0 -> buildPlayerPage(true);
            case 1 -> buildPlayerPage(false);
            case 2 -> buildLevelPage();
        }
        revalidate();
        repaint();
    }

    private void buildPlayerPage(boolean isP1) {
        int w = getPreferredSize().width;
        int h = getPreferredSize().height;
        Color accentColor = isP1 ? new Color(0xF23F3A) : new Color(0x775AFF);
        String pLabel = isP1 ? "Player 1" : "Player 2";

        // Title
        JLabel title = styledLabel(pLabel + " Setup", 28, Font.BOLD, accentColor);
        title.setBounds(w/2 - 150, 40, 300, 40);
        add(title);

        // Name field
        JLabel nameLabel = styledLabel("Name:", 16, Font.PLAIN, Color.DARK_GRAY);
        nameLabel.setBounds(w/2 - 140, 110, 80, 30);
        add(nameLabel);

        JTextField nameField = isP1 ? p1NameField : p2NameField;
        styleTextField(nameField, accentColor);
        nameField.setBounds(w/2 - 55, 108, 160, 34);
        add(nameField);

        // Trail color
        JLabel colorLabel = styledLabel("Trail Color:", 16, Font.PLAIN, Color.DARK_GRAY);
        colorLabel.setBounds(w/2 - 140, 175, 120, 30);
        add(colorLabel);

        int colorStartX = w/2 - 140;
        for (int i = 0; i < TRAIL_COLORS.length; i++) {
            final int idx = i;
            int cx = colorStartX + i * 80;
            JPanel swatch = colorSwatch(TRAIL_COLORS[i], COLOR_NAMES[i],
                isP1 ? (p1ColorIdx == i) : (p2ColorIdx == i), accentColor);
            swatch.setBounds(cx, 205, 70, 55);
            swatch.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (isP1) { p1ColorIdx = idx; p1Trail = TRAIL_COLORS[idx]; }
                    else       { p2ColorIdx = idx; p2Trail = TRAIL_COLORS[idx]; }
                    buildPage();
                }
            });
            add(swatch);
        }

        // Preview swatch
        Color previewColor = isP1 ? p1Trail : p2Trail;
        JPanel preview = new JPanel();
        preview.setBackground(previewColor);
        preview.setBounds(w/2 - 40, 290, 80, 50);
        preview.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
        add(preview);
        JLabel previewLabel = styledLabel("Your color", 11, Font.PLAIN, Color.GRAY);
        previewLabel.setBounds(w/2 - 50, 344, 100, 18);
        add(previewLabel);

        // Next button
        String btnText = (page == 0) ? "Next → P2 Setup" : "Next → Level Select";
        JButton nextBtn = accentButton(btnText, accentColor);
        nextBtn.setBounds(w/2 - 110, h - 70, 220, 44);
        nextBtn.addActionListener(e -> {
        page++;
        buildPage();

            if (page == 1) {
            currentBG = p2bg;
            repaint();
            }
        });
        add(nextBtn);

        // Page indicator
        JLabel pageInd = styledLabel("Step " + (page + 1) + " of 3", 13, Font.PLAIN, Color.GRAY);
        pageInd.setBounds(w/2 - 50, h - 22, 100, 18);
        add(pageInd);
    }

    private void buildLevelPage() {
        int w = getPreferredSize().width;
        int h = getPreferredSize().height;

        JLabel title = styledLabel("Select Level", 28, Font.BOLD, new Color(0x222222));
        title.setBounds(w/2 - 150, 40, 300, 40);
        add(title);

        String[] levelTitles = { "Level 1 — Classic", "Level 2 — Power-Ups", "Level 3 — Battle" };
        String[] levelDescs  = {
            "Standard ink battle. Claim the most territory!",
            "Power-ups appear! Speed boosts, splash bombs & erasers.",
            "Power-ups + players can bump each other to lose progress!"
        };
        Color[] levelColors = {
            new Color(0x00B487), new Color(0xFFC549), new Color(0xF23F3A)
        };

        for (int i = 0; i < 3; i++) {
            final int lvl = i + 1;
            boolean sel = (selectedLevel == lvl);
            JPanel card = levelCard(levelTitles[i], levelDescs[i], levelColors[i], sel);
            card.setBounds(w/2 - 220, 110 + i * 115, 440, 100);
            card.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) { selectedLevel = lvl; buildPage(); }
            });
            add(card);
        }

        Color playAccent = levelColors[selectedLevel - 1];
        JButton playBtn = accentButton("▶  Play!", playAccent);
        playBtn.setForeground(Color.WHITE);
        playBtn.setBounds(w/2 - 110, h - 70, 220, 44);
        playBtn.addActionListener(e -> {
            MenuResult result = new MenuResult();
            result.p1Name  = p1NameField.getText().trim().isEmpty() ? "Player 1" : p1NameField.getText().trim();
            result.p2Name  = p2NameField.getText().trim().isEmpty() ? "Player 2" : p2NameField.getText().trim();
            result.p1Color = p1Trail;
            result.p2Color = p2Trail;
            result.level   = selectedLevel;
            onReady.accept(result);
        });
        add(playBtn);

        JButton backBtn = ghostButton("← Back");
        backBtn.setBounds(20, h - 58, 100, 32);
        backBtn.addActionListener(e -> { page--; buildPage(); });
        add(backBtn);

        JLabel pageInd = styledLabel("Step 3 of 3", 13, Font.PLAIN, Color.GRAY);
        pageInd.setBounds(w/2 - 50, h - 22, 100, 18);
        add(pageInd);
    }

    // ── Component helpers ─────────────────────────────────────────────────────

    private JLabel styledLabel(String text, int size, int style, Color color) {
        JLabel lbl = new JLabel(text, SwingConstants.CENTER);
        lbl.setFont(new Font("SansSerif", style, size));
        lbl.setForeground(color);
        return lbl;
    }

    private void styleTextField(JTextField tf, Color accent) {
        tf.setBackground(Color.WHITE);
        tf.setForeground(Color.DARK_GRAY);
        tf.setCaretColor(Color.DARK_GRAY);
        tf.setFont(new Font("SansSerif", Font.PLAIN, 15));
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(accent, 2),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        tf.setHorizontalAlignment(SwingConstants.CENTER);
    }

    private JPanel colorSwatch(Color color, String name, boolean selected, Color accent) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(selected ? new Color(240, 240, 240) : Color.WHITE);
        p.setBorder(BorderFactory.createLineBorder(
            selected ? accent : new Color(200, 200, 200), selected ? 3 : 1));
        p.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel dot = new JPanel();
        dot.setBackground(color);
        dot.setPreferredSize(new Dimension(30, 30));
        dot.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));

        JLabel lbl = new JLabel(name, SwingConstants.CENTER);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 10));
        lbl.setForeground(selected ? accent.darker() : Color.GRAY);

        p.add(dot, BorderLayout.CENTER);
        p.add(lbl, BorderLayout.SOUTH);
        return p;
    }

    private JButton accentButton(String text, Color accent) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 16));
        btn.setBackground(accent);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton ghostButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.PLAIN, 14));
        btn.setBackground(Color.WHITE);
        btn.setForeground(Color.DARK_GRAY);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(new Color(180, 180, 180), 1));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JPanel levelCard(String title, String desc, Color accent, boolean selected) {
        JPanel card = new JPanel(null);
        card.setBackground(selected ? new Color(250, 250, 255) : Color.WHITE);
        card.setBorder(BorderFactory.createLineBorder(
            selected ? accent : new Color(210, 210, 210), selected ? 3 : 1));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel t = new JLabel(title);
        t.setFont(new Font("SansSerif", Font.BOLD, 18));
        t.setForeground(selected ? accent.darker() : Color.DARK_GRAY);
        t.setBounds(15, 10, 400, 30);
        card.add(t);

        JLabel d = new JLabel("<html>" + desc + "</html>");
        d.setFont(new Font("SansSerif", Font.PLAIN, 13));
        d.setForeground(new Color(100, 100, 110));
        d.setBounds(15, 45, 400, 45);
        card.add(d);

        return card;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (currentBG != null) {
            g.drawImage(currentBG, 0, 0, getWidth(), getHeight(), this);
        }
    }

    public JTextField getP2NameField() {
        return p2NameField;
    }

    // ── Result DTO ────────────────────────────────────────────────────────────

    public static class MenuResult {
        public String p1Name;
        public String p2Name;
        public Color  p1Color;
        public Color  p2Color;
        public int    level;
    }
}