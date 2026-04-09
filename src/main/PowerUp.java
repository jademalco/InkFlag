package main;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PowerUp {

    public enum Type {
        SPEED,        // Yellow - extra speed for 5 seconds
        SPLASH_BOMB,  // Green  - splash player color within a limited radius
        BOMB          // Black  - erase player color within a limited radius
    }

    public int x, y;       
    public Type type;
    public boolean active = true;
    public int size;       

    public PowerUp(int x, int y, Type type, int tileSize) {
        this.x    = x;
        this.y    = y;
        this.type = type;
        this.size = tileSize;
    }

    public Color getColor() {
        return switch (type) {
            case SPEED -> new Color(255, 220, 0);
            case SPLASH_BOMB -> new Color(0, 200, 80);
            case BOMB -> new Color(30, 30, 30);
            default -> Color.WHITE;
        };
    }

    public void draw(Graphics2D g2) {
        if (!active) return;
        // Outer glow ring
        g2.setColor(getColor().brighter());
        g2.fillOval(x + 2, y + 2, size - 4, size - 4);
        // Inner filled circle
        g2.setColor(getColor());
        g2.fillOval(x + 5, y + 5, size - 10, size - 10);
        // White center dot for readability
        g2.setColor(new Color(255, 255, 255, 180));
        g2.fillOval(x + size / 2 - 4, y + size / 2 - 4, 8, 8);
    }

    // Spawn a set of power-ups randomly on the map
    public static List<PowerUp> spawnRandom(int count, int maxCol, int maxRow,
                                             int tileSize, int flagCol, int flagRow,
                                             int flagCols, int flagRows) {
        List<PowerUp> list = new ArrayList<>();
        Random rand = new Random();
        Type[] types = Type.values();
        int attempts = 0;

        while (list.size() < count && attempts < 500) {
            attempts++;
            int col = 1 + rand.nextInt(maxCol - 2);
            int row = 1 + rand.nextInt(maxRow - 2);

            // Avoid spawning in the middle (flag area)
            if (col >= flagCol && col < flagCol + flagCols &&
                row >= flagRow && row < flagRow + flagRows) continue;

            // Avoid duplicate positions
            boolean dup = false;
            for (PowerUp p : list) {
                if (p.x == col * tileSize && p.y == row * tileSize) { dup = true; break; }
            }
            if (dup) continue;

            Type t = types[rand.nextInt(types.length)];
            list.add(new PowerUp(col * tileSize, row * tileSize, t, tileSize));
        }
        return list;
    }
}






