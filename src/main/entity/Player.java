package main.entity;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import main.GamePanel;
import main.KeyHandler;

public class Player extends Entity {

    GamePanel gp;
    KeyHandler keyH;
    boolean isPlayerOne;

    int spriteCounter = 0;
    int spriteNum = 1;

    public Player(GamePanel gp, KeyHandler keyH, boolean isPlayerOne) {
        this.gp = gp;
        this.keyH = keyH;
        this.isPlayerOne = isPlayerOne;
        setDefaultValues();
        getPlayerImage();
    }

    public void setDefaultValues() {
        if (isPlayerOne) {
            x = 100;
            y = 100;
        } else {
            x = gp.screenWidth  - 100 - gp.tileSize;
            y = gp.screenHeight - 100 - gp.tileSize;
        }
        speed = 4;
        direction = "down";
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
            e.printStackTrace();
        }
    }

    public void update() {
        boolean moving = false;

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
            if (spriteCounter > 12) {
                spriteNum = (spriteNum == 1) ? 2 : 1;
                spriteCounter = 0;
            }
        }

        x = Math.max(0, Math.min(x, gp.screenWidth  - gp.tileSize));
        y = Math.max(0, Math.min(y, gp.screenHeight - gp.tileSize));
    }

    public void draw(Graphics2D g2) {
        BufferedImage image = null;

        switch (direction) {
            case "up":    image = (spriteNum == 1) ? up    : up1;    break;
            case "down":  image = (spriteNum == 1) ? down  : down1;  break;
            case "left":  image = (spriteNum == 1) ? left  : left1;  break;
            case "right": image = (spriteNum == 1) ? right : right1; break;
        }

        // Fallback colored box if sprites haven't been added yet
        if (image == null) {
            g2.setColor(isPlayerOne ? gp.p1Color : gp.p2Color);
            g2.fillRect(x, y, gp.tileSize, gp.tileSize);
        } else {
            g2.drawImage(image, x, y, gp.tileSize, gp.tileSize, null);
        }
    }
}