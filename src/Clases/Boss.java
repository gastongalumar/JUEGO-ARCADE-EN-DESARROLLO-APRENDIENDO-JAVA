package Clases;

public class Boss extends Enemy {
    private int health;
    private static final int BOSS_GROUND_Y = 650;

    public Boss(int x, int y, int width, int height, int initialHealth) {
        super(x, y, width, height, 2, 0);
        health = initialHealth;
        rect.y = BOSS_GROUND_Y - rect.height;
    }

    public void update(int groundY) {
        rect.x += vx;
        if (rect.x <= 0 || rect.x >= 5000 - rect.width) {
            vx = -vx;
        }

        rect.y = BOSS_GROUND_Y - rect.height;
        vy = 0;
    }

    public void damage() {
        health--;
    }

    public boolean isAlive() { return health > 0; }
    public int getHealth() { return health; }
}