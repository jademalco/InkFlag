package main;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyHandler implements KeyListener {

    // Player 1 — WASD
    public boolean upPressed, downPressed, leftPressed, rightPressed;

    // Player 2 — Arrow keys
    public boolean up2Pressed, down2Pressed, left2Pressed, right2Pressed;

    // Shared
    public boolean enterPressed;

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();

        if (code == KeyEvent.VK_W) upPressed      = true;
        if (code == KeyEvent.VK_S) downPressed    = true;
        if (code == KeyEvent.VK_A) leftPressed    = true;
        if (code == KeyEvent.VK_D) rightPressed   = true;

        if (code == KeyEvent.VK_UP)    up2Pressed    = true;
        if (code == KeyEvent.VK_DOWN)  down2Pressed  = true;
        if (code == KeyEvent.VK_LEFT)  left2Pressed  = true;
        if (code == KeyEvent.VK_RIGHT) right2Pressed = true;

        if (code == KeyEvent.VK_ENTER) enterPressed = true;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();

        if (code == KeyEvent.VK_W) upPressed      = false;
        if (code == KeyEvent.VK_S) downPressed    = false;
        if (code == KeyEvent.VK_A) leftPressed    = false;
        if (code == KeyEvent.VK_D) rightPressed   = false;

        if (code == KeyEvent.VK_UP)    up2Pressed    = false;
        if (code == KeyEvent.VK_DOWN)  down2Pressed  = false;
        if (code == KeyEvent.VK_LEFT)  left2Pressed  = false;
        if (code == KeyEvent.VK_RIGHT) right2Pressed = false;

        if (code == KeyEvent.VK_ENTER) enterPressed = false;
    }
}