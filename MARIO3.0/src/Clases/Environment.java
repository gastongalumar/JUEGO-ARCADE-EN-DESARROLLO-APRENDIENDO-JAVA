package Clases;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Random;

public class Environment {
    private ArrayList<Rectangle> trees;
    private ArrayList<Rectangle> mountains;
    private ArrayList<Rectangle> castles;
    private ArrayList<Rectangle> clouds;
    private Random rand;
    private final int GROUND_Y;
    private final int WORLD_WIDTH;

    public Environment(int groundY, int worldWidth) {
        this.GROUND_Y = groundY;
        this.WORLD_WIDTH = worldWidth;
        rand = new Random();
        trees = new ArrayList<>();
        mountains = new ArrayList<>();
        castles = new ArrayList<>();
        clouds = new ArrayList<>();
        initEnvironment();
    }

    private void initEnvironment() {
        // Árboles
        for (int i = 0; i < 15; i++) {
            trees.add(new Rectangle(i*900 + rand.nextInt(500), GROUND_Y - 290, 280, 290));
        }

        // Montañas
        for (int i = 0; i < 5; i++) {
            mountains.add(new Rectangle(i*900 + rand.nextInt(500), GROUND_Y - 270, 480, 300));
        }

        // Castillos
        for (int i = 0; i < 3; i++) {
            castles.add(new Rectangle(100 + i*1700 + rand.nextInt(200), GROUND_Y - 430, 600, 500));
        }

        // Nubes
        for (int i = 0; i < 15; i++) {
            clouds.add(new Rectangle(rand.nextInt(WORLD_WIDTH), rand.nextInt(200), 150, 120));
        }
    }

    public void updateClouds() {
        for (Rectangle c : clouds) {
            c.x -= 1;
            if (c.x + c.width < 0) c.x = WORLD_WIDTH;
        }
    }

    // Getters
    public ArrayList<Rectangle> getTrees() { return trees; }
    public ArrayList<Rectangle> getMountains() { return mountains; }
    public ArrayList<Rectangle> getCastles() { return castles; }
    public ArrayList<Rectangle> getClouds() { return clouds; }
}