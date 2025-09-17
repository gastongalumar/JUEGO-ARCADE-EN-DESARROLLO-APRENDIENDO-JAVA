package Clases;

public class Boss extends Enemy {
    private int health;

    public Boss(int x, int y, int width, int height, int initialHealth) {
        super(x, y, width, height, 2, 0);
        health = initialHealth;
    }

    public void update(int groundY) {
        rect.x += vx;
        if (rect.x <= 0 || rect.x >= 5000 - rect.width) {
            vx = -vx;
        }
    }

    public void damage() {
        health--;
    }

    public boolean isAlive() { return health > 0; }
    public int getHealth() { return health; }
}