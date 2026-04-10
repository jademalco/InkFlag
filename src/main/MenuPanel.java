package main;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;
import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import javax.swing.*;

/**
 * MenuPanel — shown before the game starts.
 * Collects: character (boy/girl), trail color, player name, and level for each player.
 */
public final class MenuPanel extends JPanel {

    private Clip bgMusic;

    // --- OPTIONS OVERLAY FIELDS ---
    private JPanel optionsPanel;
    private JCheckBox musicMute, sfxMute;
    private JSlider volumeSlider;
    private Image optionsBG;

    // Callback fired when both players are ready
    private final Consumer<MenuResult> onReady;
    private final Runnable onHome; // Callback to return to main menu


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
        this(width, height, onReady, null);
    }

    public MenuPanel(int width, int height, Consumer<MenuResult> onReady, Runnable onHome) {
        this.onReady = onReady;
        this.onHome = onHome;
        setPreferredSize(new Dimension(width, height));
        setBackground(Color.WHITE);

    

        // Load your actual files from res/
        try {
            p1bg = ImageIO.read(new File("res/p1bg.png"));
            p2bg = ImageIO.read(new File("res/p2bg.png"));
            optionsBG = ImageIO.read(new File("res/opt_pop.png")); // Add this
        } catch (IOException ex) {
            throw new RuntimeException("Unable to load menu background images", ex);
        }

        // Start by pointing to the first background
        currentBG = p1bg;

        playMenuMusic("bgmusic.wav");
        setLayout(null);
        // --- BUILD THE PANEL HERE ---
        setupOptionsPanel(); // Call the setup method
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

        // Home button (top left) - Match the coordinates from buildPlayerPage
        JButton homeBtn = new JButton(new ImageIcon("res/home.png"));
        homeBtn.setBounds(20, 20, 50, 50); // Exact same spot as Step 1 & 2
        homeBtn.setBorderPainted(false);
        homeBtn.setContentAreaFilled(false);
        homeBtn.setFocusPainted(false);
        homeBtn.setOpaque(false);
        homeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        homeBtn.addActionListener(e -> {
            playSound("click.wav"); // <--- Play sound first
            if (onHome != null) {
                onHome.run(); // Goes back to the Main Menu
            } else {
                page = 0; // Fallback to Player 1 Setup
            }
            buildPage();
        });
        add(homeBtn);

        // 2. INSERT: THE SETTINGS BUTTON (Top Right)
        JButton optBtn = new JButton(new ImageIcon("res/settings.png")); 
        optBtn.setBounds(getPreferredSize().width - 80, 20, 50, 50); // Top Right corner
        optBtn.setBorderPainted(false);
        optBtn.setContentAreaFilled(false);
        optBtn.setFocusPainted(false);
        optBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        optBtn.addActionListener(e -> {
            playSound("click.wav");
            optionsPanel.setVisible(true); // Show the Figma popup
            setComponentZOrder(optionsPanel, 0); // Force it to the front layer
            repaint();
        });
        add(optBtn);

        switch (page) {
            case 0 -> buildPlayerPage(true);
            case 1 -> buildPlayerPage(false);
            case 2 -> buildLevelPage();
        }

        // 3. THE FIX: RE-ADD THE POPUP
        if (optionsPanel != null) {
            add(optionsPanel);
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

        // Home button (top left)
        JButton homeBtn = new JButton(new ImageIcon("res/home.png"));
        homeBtn.setBounds(20, 20, 50, 50);
        homeBtn.setBorderPainted(false);
        homeBtn.setContentAreaFilled(false);
        homeBtn.setFocusPainted(false);
        homeBtn.setOpaque(false);
        homeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        homeBtn.addActionListener(e -> {
            if (onHome != null) {
                onHome.run();
            } else {
                page = 0;
                buildPage();
            }
        });
        add(homeBtn);

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

        // Color options
        int colorStartX = w/2 - 140;
        for (int i = 0; i < TRAIL_COLORS.length; i++) {
            final int idx = i;
            int cx = colorStartX + i * 80;

            // ─── ADD THIS LINE HERE ───
            boolean isTaken = (!isP1 && p1ColorIdx == idx);
            // MODIFY THIS LINE (just add the 'isTaken' variable at the end)
            JPanel swatch = colorSwatch(TRAIL_COLORS[i], COLOR_NAMES[i], 
                isP1 ? (p1ColorIdx == i) : (p2ColorIdx == i), accentColor, isTaken);

            swatch.setBounds(cx, 205, 70, 55);

            // ─── WRAP YOUR EXISTING MOUSE LISTENER IN THIS IF STATEMENT ───
        if (!isTaken) {
            swatch.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (isP1) { p1ColorIdx = idx; p1Trail = TRAIL_COLORS[idx]; }
                    else       { p2ColorIdx = idx; p2Trail = TRAIL_COLORS[idx]; }
                    buildPage();
                }
            }); 
        }
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
        JButton nextBtn = new JButton(new ImageIcon("res/next.png"));
        nextBtn.setBounds(w/2 - 110, h - 70, 220, 44);
        nextBtn.setBorderPainted(false);
        nextBtn.setContentAreaFilled(false);
        nextBtn.setFocusPainted(false);
        nextBtn.setOpaque(false);
        nextBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        nextBtn.addActionListener(e -> {
            playSound("click.wav"); // <── Add this!
            page++;
            buildPage();

            if (page == 1) {
                currentBG = p2bg;
                repaint();
            }
        });
        add(nextBtn);

        // Back button (only on Player 2 setup, left of Next button)
        if (!isP1) {
            JButton backBtn = new JButton(new ImageIcon("res/back.png"));
            backBtn.setBounds(w/2 - 110 - 70, h - 70, 60, 44);
            backBtn.setBorderPainted(false);
            backBtn.setContentAreaFilled(false);
            backBtn.setFocusPainted(false);
            backBtn.setOpaque(false);
            backBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            backBtn.addActionListener(e -> {
                playSound("click.wav"); // <── Add this!
                page--;
                buildPage();
                currentBG = p1bg;
                repaint();
            });
            add(backBtn);
        }

        // Page indicator
        //JLabel pageInd = styledLabel("Step " + (page + 1) + " of 3", 13, Font.PLAIN, Color.GRAY);
        //pageInd.setBounds(w/2 - 50, h - 22, 100, 18);
        //add(pageInd);
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

        JButton backBtn = new JButton(new ImageIcon("res/back.png"));
        backBtn.setBounds(w/2 - 180, h - 70, 60, 44);
        //backBtn.setBounds(groupX, buttonY, backWidth, 44);
        backBtn.setBorderPainted(false);
        backBtn.setContentAreaFilled(false);
        backBtn.setFocusPainted(false);
        backBtn.setOpaque(false);
        backBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backBtn.addActionListener(e -> {
            playSound("click.wav"); // <── Add this!
            page = 1;
            buildPage();
            currentBG = p2bg;
            repaint();
        });
        add(backBtn);

        JButton playBtn = new JButton(new ImageIcon("res/play.png"));
        playBtn.setBounds(w/2 - 110, h - 70, 220, 44);
        //playBtn.setBounds(groupX + backWidth + gap, buttonY, playWidth, 44);
        playBtn.setBorderPainted(false);
        playBtn.setContentAreaFilled(false);
        playBtn.setFocusPainted(false);
        playBtn.setOpaque(false);
        playBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
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

        //JLabel pageInd = styledLabel("Step 3 of 3", 13, Font.PLAIN, Color.GRAY);
        //pageInd.setBounds(w/2 - 50, h - 22, 100, 18);
        //add(pageInd);
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

    private JPanel colorSwatch(Color color, String name, boolean selected, Color accent, boolean isTaken) {
        JPanel p = new JPanel(new BorderLayout());

        // ADDED: Visual logic to make it look "Disabled"
    if (isTaken) {
        p.setBackground(new Color(210, 210, 210)); // Light Gray
        p.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
    } else {
        p.setBackground(selected ? new Color(240, 240, 240) : Color.WHITE);
        p.setBorder(BorderFactory.createLineBorder(
            selected ? accent : new Color(200, 200, 200), selected ? 3 : 1));
    }

    // ADDED: Change the cursor so it doesn't look clickable
    p.setCursor(Cursor.getPredefinedCursor(isTaken ? Cursor.DEFAULT_CURSOR : Cursor.HAND_CURSOR));

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

    private void playSound(String soundFile) {
        if (sfxMute != null && sfxMute.isSelected()) {
        return; 
    }

    try {
        File file = new File("res/" + soundFile);
        AudioInputStream audioIn = AudioSystem.getAudioInputStream(file);
        Clip clip = AudioSystem.getClip();
        clip.open(audioIn);

        // --- VOLUME CONTROL START ---
        // Gain is measured in decibels. 
        // 0.0 is normal, -10.0 is quieter, -20.0 is very quiet.
        if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            
            // Get slider value (0-100) and convert to a 0.0 to 1.0 range
            float sliderVal = volumeSlider.getValue() / 100f;
            
            // Convert linear scale to Decibels (Logarithmic)
            // This makes the slider feel "natural" to the human ear
            float dB = (float) (Math.log10(Math.max(sliderVal, 0.0001)) * 20.0);
            
            // Clamp value to prevent errors (Master Gain usually range -80 to 6)
            dB = Math.max(gainControl.getMinimum(), Math.min(gainControl.getMaximum(), dB));
            
            gainControl.setValue(dB);
        }

        clip.start();
        
        clip.addLineListener(event -> {
            if (event.getType() == LineEvent.Type.STOP) {
                clip.close();
            }
        });
    } catch (IOException | LineUnavailableException | UnsupportedAudioFileException e) {
        System.err.println("Sound Error: " + e.getMessage());
    }
}

    public void playMenuMusic(String soundFile) {
    // 1. Check if bgMusic is already running. 
    // If it is, "return" (stop here) so we don't start a second copy.
    if (bgMusic != null && bgMusic.isRunning()) {
        return; 
    }

    try {
        File file = new File("res/" + soundFile);
        AudioInputStream audioIn = AudioSystem.getAudioInputStream(file);
        bgMusic = AudioSystem.getClip();
        bgMusic.open(audioIn);

        FloatControl gainControl = (FloatControl) bgMusic.getControl(FloatControl.Type.MASTER_GAIN);
        gainControl.setValue(-15.0f); 

        bgMusic.loop(Clip.LOOP_CONTINUOUSLY); 
        bgMusic.start();
    } catch (IOException | LineUnavailableException | UnsupportedAudioFileException e) {
        System.err.println("Music Error: " + e.getMessage());
    }
}

    private void setupOptionsPanel() {
    optionsPanel = new JPanel() {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (optionsBG != null) {
                g.drawImage(optionsBG, 0, 0, getWidth(), getHeight(), this);
            }
        }
    };
    
    optionsPanel.setLayout(null);
    optionsPanel.setOpaque(false);
    optionsPanel.setVisible(false); // Start hidden
    
    // Position it in the center of the screen
    int pW = 400; int pH = 300;
    optionsPanel.setBounds((getPreferredSize().width - pW)/2, (getPreferredSize().height - pH)/2, pW, pH);

    // Initialize Checkboxes
    musicMute = new JCheckBox("Mute Music");
    musicMute.setBounds(50, 70, 300, 30);
    musicMute.setForeground(Color.BLACK);
    musicMute.setOpaque(false);
    musicMute.addActionListener(e -> {
        if (musicMute.isSelected()) bgMusic.stop(); 
        else bgMusic.start();
    });

    sfxMute = new JCheckBox("Mute SFX");
    sfxMute.setBounds(50, 110, 300, 30);
    sfxMute.setForeground(Color.BLACK);
    sfxMute.setOpaque(false);

    // Initialize Slider
    volumeSlider = new JSlider(0, 100, 50);
    volumeSlider.setUI(new CustomSliderUI(volumeSlider)); // We'll add the UI class below
    volumeSlider.setBounds(50, 170, 300, 40);
    volumeSlider.setOpaque(false);

    volumeSlider.addChangeListener(e -> {
        if (bgMusic != null && bgMusic.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            FloatControl gainControl = (FloatControl) bgMusic.getControl(FloatControl.Type.MASTER_GAIN);
            float sliderVal = volumeSlider.getValue() / 100f;
            float dB = (float) (Math.log10(Math.max(sliderVal, 0.0001)) * 20.0);
            dB = Math.max(gainControl.getMinimum(), Math.min(gainControl.getMaximum(), dB));
            gainControl.setValue(dB);
        }
    });
    
    // Close Button
    JButton closeBtn = new JButton("CLOSE");
    closeBtn.setBounds(150, 230, 100, 30);
    closeBtn.addActionListener(e -> optionsPanel.setVisible(false));

    optionsPanel.add(musicMute);
    optionsPanel.add(sfxMute);
    optionsPanel.add(volumeSlider);
    optionsPanel.add(closeBtn);

    add(optionsPanel); // Add it to the MenuPanel
    setComponentZOrder(optionsPanel, 0); // Add this line here too
}
    // ── Result DTO ────────────────────────────────────────────────────────────

    public static class MenuResult {
        public String p1Name;
        public String p2Name;
        public Color  p1Color;
        public Color  p2Color;
        public int    level;
    }

    class CustomSliderUI extends javax.swing.plaf.basic.BasicSliderUI {
    private Image thumbImg = new ImageIcon("res/vol_thumb.png").getImage();
    private Image trackImg = new ImageIcon("res/vol_track.png").getImage();

    public CustomSliderUI(JSlider b) { super(b); }

    @Override
    public void paintTrack(Graphics g) {
        g.drawImage(trackImg, trackRect.x, trackRect.y, trackRect.width, trackRect.height, null);
    }

    @Override
    public void paintThumb(Graphics g) {
        g.drawImage(thumbImg, thumbRect.x, thumbRect.y, thumbRect.width, thumbRect.height, null);
    }
}
}