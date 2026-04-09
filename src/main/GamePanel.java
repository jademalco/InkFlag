package main;

import java.awt.*;
import javax.swing.*;
import main.MenuPanel.MenuResult;

/**
 * GamePanel is a CardLayout container.
 * "menu" card  — MenuPanel Swing UI
 * "game" card  — GameCanvas (actual rendering + game loop)
 */
public class GamePanel extends JPanel {

    private static final String CARD_MENU = "menu";
    private static final String CARD_GAME = "game";

    private final CardLayout cardLayout = new CardLayout();
    private MenuPanel  menuPanel;
    private GameCanvas gameCanvas;

    static final int ORIGINAL_TILE = 16;
    static final int SCALE         = 3;
    static final int TILE_SIZE     = ORIGINAL_TILE * SCALE;
    static final int MAX_COL       = 16;
    static final int MAX_ROW       = 12;
    static final int SCREEN_W      = TILE_SIZE * MAX_COL;
    static final int SCREEN_H      = TILE_SIZE * MAX_ROW;

    public GamePanel() {
        setLayout(cardLayout);
        setPreferredSize(new Dimension(SCREEN_W, SCREEN_H));
        showMenu();
    }

    private void showMenu() {
        if (menuPanel != null) remove(menuPanel);
        menuPanel = new MenuPanel(SCREEN_W, SCREEN_H, this::startGame, this::returnToMainMenu);
        add(menuPanel, CARD_MENU);
        cardLayout.show(this, CARD_MENU);
        revalidate();
        repaint();
    }

    private void returnToMainMenu() {
        // Get the parent JFrame and swap GamePanel back to MainMenu
        JFrame window = (JFrame) SwingUtilities.getWindowAncestor(this);
        if (window != null) {
            window.remove(this);
            MainMenu mainMenu = new MainMenu();
            window.add(mainMenu);
            window.revalidate();
            window.repaint();
        }
    }

    void startGame(MenuResult result) {
        if (gameCanvas != null) {
            gameCanvas.stopThread();
            remove(gameCanvas);
        }
        gameCanvas = new GameCanvas(result, this);
        add(gameCanvas, CARD_GAME);
        cardLayout.show(this, CARD_GAME);
        revalidate();
        repaint();
        gameCanvas.requestFocusInWindow();
        gameCanvas.startGameThread();
    }

    /** Called by GameCanvas post-game buttons. */
    void returnToMenu(boolean jumpToLevel, MenuResult lastResult) {
        if (gameCanvas != null) {
            gameCanvas.stopThread();
            remove(gameCanvas);
            gameCanvas = null;
        }
        if (menuPanel != null) remove(menuPanel);
        menuPanel = new MenuPanel(SCREEN_W, SCREEN_H, this::startGame, this::returnToMainMenu);
        if (lastResult != null) menuPanel.prefill(lastResult);
        if (jumpToLevel)        menuPanel.goToLevelPage();
        add(menuPanel, CARD_MENU);
        cardLayout.show(this, CARD_MENU);
        revalidate();
        repaint();
    }

    /** Called from Main — nothing needed; GameCanvas manages its own thread. */
    public void startGameThread() {}
}