package Clases;

import java.awt.Rectangle;

public class Bullet {
    private Rectangle rect;
    private int dir;
    private final int BULLET_SPEED = 25;

    public Bullet(int x, int y, int direction) {
        rect = new Rectangle(x, y, 20, 10);
        dir = direction;
    }

    public void update() {
        rect.x += BULLET_SPEED * dir;
    }

    public Rectangle getRect() { return rect; }
}