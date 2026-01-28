package Clases;

import java.awt.Rectangle;

public class Enemy {
    protected Rectangle rect;
    protected int vy, vx;

    public Enemy(int x, int y, int width, int height, int speedX, int speedY) {
        rect = new Rectangle(x, y, width, height);
        vx = speedX;
        vy = speedY;
    }

    public void update(int groundY) {
        rect.x += vx;
        rect.y += vy;

        if (rect.y <= 50 || rect.y >= groundY - rect.height) {
            vy = -vy;
        }
    }

    public Rectangle getRect() { return rect; }
}