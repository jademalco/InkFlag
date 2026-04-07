package main.entity;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import main.GameCanvas;
import main.KeyHandler;

public class Player extends Entity {

    GameCanvas gc;
    KeyHandler keyH;
    public boolean isPlayerOne;

    int spriteCounter = 0;
    int spriteNum = 1;

    public Color trailColor;

    // Power-up state
    public boolean speedActive = false;
    public int     speedTimer  = 0;
    public static final int SPEED_FRAMES = 60 * 5;
    private static final int BASE_SPEED  = 4;
    private static final int BOOST_SPEED = 8;

    // Hit flash
    public boolean hitFlash = false;
    public int     hitTimer = 0;
    private static final int HIT_FLASH_FRAMES = 30;

    public Player(GameCanvas gc, KeyHandler keyH, boolean isPlayerOne) {
        this.gc          = gc;
        this.keyH        = keyH;
        this.isPlayerOne = isPlayerOne;
        setDefaultValues();
        getPlayerImage();
    }

    public void setDefaultValues() {
        if (isPlayerOne) {
            x = 100;
            y = 100;
        } else {
            x = gc.screenWidth  - 100 - gc.tileSize;
            y = gc.screenHeight - 100 - gc.tileSize;
        }
        speed     = BASE_SPEED;
        direction = "down";
        trailColor = isPlayerOne ? gc.p1Color : gc.p2Color;
    }

    public void getPlayerImage() {
        try {
            String folder = isPlayerOne ? "/Player/" : "/Player2/";
            up     = ImageIO.read(getClass().getResourceAsStream(folder + "up.png"));
            up1    = ImageIO.read(getClass().getResourceAsStream(folder + "up1.png"));
            down   = ImageIO.read(getClass().getResourceAsStream(folder + "down.png"));
            down1  = ImageIO.read(getClass().getResourceAsStream(folder + "down1.png"));
            left   = ImageIO.read(getClass().getResourceAsStream(folder + "left.png"));
            left1  = ImageIO.read(getClass().getResourceAsStream(folder + "left1.png"));
            right  = ImageIO.read(getClass().getResourceAsStream(folder + "right.png"));
            right1 = ImageIO.read(getClass().getResourceAsStream(folder + "right1.png"));
        } catch (Exception e) {
            // No sprites — will draw colored box
        }
    }

    public void update() {
        boolean moving = false;

        if (speedActive) {
            speedTimer--;
            if (speedTimer <= 0) { speedActive = false; speed = BASE_SPEED; }
        }

        if (hitFlash) {
            hitTimer--;
            if (hitTimer <= 0) hitFlash = false;
        }

        if (isPlayerOne) {
            if (keyH.upPressed)    { direction = "up";    y -= speed; moving = true; }
            if (keyH.downPressed)  { direction = "down";  y += speed; moving = true; }
            if (keyH.leftPressed)  { direction = "left";  x -= speed; moving = true; }
            if (keyH.rightPressed) { direction = "right"; x += speed; moving = true; }
        } else {
            if (keyH.up2Pressed)    { direction = "up";    y -= speed; moving = true; }
            if (keyH.down2Pressed)  { direction = "down";  y += speed; moving = true; }
            if (keyH.left2Pressed)  { direction = "left";  x -= speed; moving = true; }
            if (keyH.right2Pressed) { direction = "right"; x += speed; moving = true; }
        }

        if (moving) {
            spriteCounter++;
            if (spriteCounter > 12) { spriteNum = (spriteNum == 1) ? 2 : 1; spriteCounter = 0; }
        }

        x = Math.max(0, Math.min(x, gc.screenWidth  - gc.tileSize));
        y = Math.max(0, Math.min(y, gc.screenHeight - gc.tileSize));
    }

    public void activateSpeed() {
        speedActive = true;
        speedTimer  = SPEED_FRAMES;
        speed       = BOOST_SPEED;
    }

    public void triggerHit() {
        hitFlash = true;
        hitTimer = HIT_FLASH_FRAMES;
    }

    public void draw(Graphics2D g2) {
        BufferedImage image = null;
        switch (direction) {
            case "up":    image = (spriteNum == 1) ? up    : up1;    break;
            case "down":  image = (spriteNum == 1) ? down  : down1;  break;
            case "left":  image = (spriteNum == 1) ? left  : left1;  break;
            case "right": image = (spriteNum == 1) ? right : right1; break;
        }

        if (image == null) {
            Color drawColor = hitFlash ? Color.RED : trailColor;
            g2.setColor(drawColor);
            g2.fillRect(x, y, gc.tileSize, gc.tileSize);
            g2.setColor(drawColor.darker());
            g2.drawRect(x, y, gc.tileSize, gc.tileSize);
            if (speedActive) {
                g2.setColor(new Color(255, 220, 0, 180));
                g2.setStroke(new BasicStroke(3));
                g2.drawRect(x+2, y+2, gc.tileSize-4, gc.tileSize-4);
                g2.setStroke(new BasicStroke(1));
            }
        } else {
            g2.drawImage(image, x, y, gc.tileSize, gc.tileSize, null);
        }
    }
}