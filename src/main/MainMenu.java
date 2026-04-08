package main;

import java.awt.*;
import javax.swing.*;

public class MainMenu extends JPanel {
    private final Image background;

    public MainMenu() {

//--------BACKGROUND----------
        this.setPreferredSize(new Dimension(800, 600));
        // 1. Load the background from your root 'res' folder
        background = new ImageIcon("res/bg1.png").getImage();
        // 2. Set layout to null so you can use exact Figma coordinates (X, Y)
        this.setLayout(null);
//--------LOGO----------    
        // 4. Create your Logo using your Figma export
        JLabel logo = new JLabel(new ImageIcon("res/logo.png"));
        logo.setBounds(300, 50, 200, 175); // Change these numbers to match Figma
        this.add(logo);

//--------BUTTONS--------------
    //--------START BUTTON--------------
        // 3. Create your Play Button using your Figma export
        // Change these numbers (X, Y, Width, Height) to match Figma
        JButton playButton = createFigmaButton("res/start.png", 355, 280, 90, 40);
        
        // Add a simple action to test it
        playButton.addActionListener(e -> {
            // Find the main window (JFrame) that holds this menu
            JFrame window = (JFrame) SwingUtilities.getWindowAncestor(this);

                if (window != null) {
                // Remove the MainMenu panel
                window.remove(this);

                // Create the GamePanel (this is your actual game)
                GamePanel gamePanel = new GamePanel();
                window.add(gamePanel);

                // Tell Swing to refresh the layout and redraw the screen
                window.revalidate();
                window.repaint();
        
                // Give the game focus so keyboard controls work immediately
                gamePanel.requestFocusInWindow();
        
                // Start the game loop
                gamePanel.startGameThread();
        
            System.out.println("InkFlag Started!");
            }
        });

        this.add(playButton);
    //--------INSTRUCTIONS BUTTON--------------
        // 2. Instructions Button
        // Uses same X (355) and size (90x40) to match the Start button
        JButton instrButton = createFigmaButton("res/instr.png", 330, 340, 140, 40);

        // Placeholder action
        instrButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, 
            "Instructions: \n- For player 1: Use WASD to move\n- For player 2: Use arrow keys to move\n- Collect the flags!", 
            "How to Play", 
            JOptionPane.INFORMATION_MESSAGE);
        });

        this.add(instrButton);

    //--------OPTIONS BUTTON--------------
        // Use X=345 and size 110x50 (the "crop-fix" size we used for Instr)
        JButton optionsButton = createFigmaButton("res/options.png", 345, 400, 110, 50);

        optionsButton.addActionListener(e -> {
            // For now, we'll just show a message. 
            // Later, this will open your settings panel!
            JOptionPane.showMessageDialog(this, 
            "Options Menu:\n- Volume: [||||||||]\n- Controls: WASD\n- Graphics: High", 
            "Settings", 
            JOptionPane.PLAIN_MESSAGE);
        });

        this.add(optionsButton);

    //--------EXIT BUTTON--------------
        // X=345 keeps it centered, Y=460 puts it at the bottom
        JButton exitButton = createFigmaButton("res/exit.png", 345, 460, 110, 50);

        exitButton.addActionListener(e -> {
            // Standard way to close a Java application
            System.exit(0); 
        });

        this.add(exitButton);
    }
        

//--------PAINT COMPONENT----------------
    // This method draws the background image behind your buttons
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (background != null) {
            g.drawImage(background, 0, 0, getWidth(), getHeight(), this);
        }
    }

    // Helper method to make your buttons look like the Figma design
    private JButton createFigmaButton(String path, int x, int y, int w, int h) {
        JButton btn = new JButton(new ImageIcon(path));
        btn.setBounds(x, y, w, h);
        
        // These 4 lines remove the "Windows/Mac" button look
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        
        return btn;
    }
}