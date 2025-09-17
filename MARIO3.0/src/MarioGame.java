import Clases.*;
import Enums.CharacterType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;

public class MarioGame extends JPanel implements ActionListener, KeyListener {
    private final int WIDTH = 1360;
    private final int HEIGHT = 768;
    private final int GROUND_Y = HEIGHT - 50;
    private final int WORLD_WIDTH = 5000;

    private Player mario;
    private ArrayList<Enemy> enemies;
    private ArrayList<Bullet> bullets;
    private Boss boss;
    private Environment environment;
    private GameAssets assets;

    private int score;
    private int enemyWaveCount;
    private int lives;
    private boolean gameStarted;
    private boolean gameOver;
    private Random rand;
    private int cameraX;

    private String waveMessage = "";
    private int waveMessageTimer = 0;
    private CharacterType selectedCharacter = CharacterType.MARIO;
    private boolean characterSelected = false;

    private Timer timer, spawnTimer, waveTimer;

    public MarioGame() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(new Color(135, 206, 235));
        setFocusable(true);
        addKeyListener(this);

        rand = new Random();
        assets = new GameAssets();
        environment = new Environment(GROUND_Y, WORLD_WIDTH);

        initGame();
    }

    private void initGame() {
        mario = new Player(60, GROUND_Y, selectedCharacter);
        enemies = new ArrayList<>();
        bullets = new ArrayList<>();
        boss = null;

        score = 0;
        lives = 3;
        enemyWaveCount = 1;
        gameStarted = false;
        gameOver = false;
        cameraX = 0;

        timer = new Timer(16, this);
        spawnTimer = new Timer(500, e -> spawnEnemy());
        waveTimer = new Timer(10000, e -> {
            enemyWaveCount++;
            waveMessage = "¡Oleada " + enemyWaveCount + "!";
            waveMessageTimer = 120;
            if (enemyWaveCount % 3 == 0) spawnBoss();
        });
    }

    private void showCharacterSelection(Graphics2D g2d) {
        g2d.setColor(new Color(0, 0, 0, 200));
        g2d.fillRect(0, 0, WIDTH, HEIGHT);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 40));
        g2d.drawString("SELECCIONA TU PERSONAJE", WIDTH/2 - 250, 100);

        int x = 150;
        int y = 200;
        int index = 0;

        for (CharacterType character : CharacterType.values()) {
            if (selectedCharacter == character) {
                g2d.setColor(Color.YELLOW);
                g2d.fillRect(x - 10, y - 10, 180, 180);
            }

            g2d.setColor(Color.WHITE);
            g2d.drawRect(x, y, 160, 160);

            if(assets != null) {
                BufferedImage[] frames = assets.getCharacterFrames(character);
                // ... resto del código
            } else {
                System.err.println("Error: Clases.GameAssets no está inicializado");
            }


            g2d.setFont(new Font("Arial", Font.PLAIN, 20));
            g2d.drawString(character.getName(), x + 10, y + 140);

            x += 220;
            if (++index % 3 == 0) {
                x = 150;
                y += 200;
            }
        }
    }

    private void startGame() {
        gameStarted = true;
        gameOver = false;
        timer.start();
        spawnTimer.start();
        waveTimer.start();
    }

    private void resetGame() {
        timer.stop();
        spawnTimer.stop();
        waveTimer.stop();
        initGame();
    }

    private void spawnEnemy() {
        for (int i = 0; i < enemyWaveCount; i++) {
            int y = 50 + rand.nextInt(GROUND_Y - 150);
            boolean fromRight = rand.nextBoolean();
            int x = fromRight ? WORLD_WIDTH : -80;
            int speed = fromRight ? -3 : 3;
            enemies.add(new Enemy(x, y, 80, 80, speed, 2 + rand.nextInt(3)));
        }
    }

    private void spawnBoss() {
        boss = new Boss(WORLD_WIDTH / 2 - 100, 100, 200, 200, 10);
        waveMessage = "¡BOSS! Oleada " + enemyWaveCount;
        waveMessageTimer = 120;
    }

    private void shoot() {
        int dir = mario.isFacingRight() ? 1 : -1;
        Rectangle r = mario.getRect();
        bullets.add(new Bullet(r.x + (dir > 0 ? r.width : -10), r.y + r.height / 2 - 5, dir));
    }

    private void updateBullets() {
        ArrayList<Bullet> toRemove = new ArrayList<>();
        for (Bullet b : bullets) {
            b.update();

            for (int i = 0; i < enemies.size(); i++) {
                if (b.getRect().intersects(enemies.get(i).getRect())) {
                    toRemove.add(b);
                    enemies.remove(i);
                    score += 10;
                    break;
                }
            }

            if (boss != null && boss.isAlive() && b.getRect().intersects(boss.getRect())) {
                toRemove.add(b);
                boss.damage();
                if (!boss.isAlive()) {
                    score += 100;
                    waveMessage = "¡BOSS DERROTADO!";
                    waveMessageTimer = 120;
                }
            }

            if (b.getRect().x < 0 || b.getRect().x > WORLD_WIDTH) toRemove.add(b);
        }
        bullets.removeAll(toRemove);
    }

    private void updateEnemies() {
        ArrayList<Enemy> toRemove = new ArrayList<>();
        for (Enemy e : enemies) {
            e.update(GROUND_Y);
            if (mario.getRect().intersects(e.getRect())) {
                toRemove.add(e);
                lives--;
                if (lives <= 0) endGame();
            }
        }
        enemies.removeAll(toRemove);

        if (boss != null && boss.isAlive()) {
            boss.update(GROUND_Y);
            if (mario.getRect().intersects(boss.getRect())) {
                lives--;
                if (lives <= 0) endGame();
            }
        }
    }

    private void endGame() {
        gameOver = true;
        timer.stop();
        spawnTimer.stop();
        waveTimer.stop();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        if (!characterSelected) {
            showCharacterSelection(g2d);
            return;
        }

        if (!gameStarted || gameOver) {
            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("Arial", Font.BOLD, 30));
            String msg = gameOver ? "Game Over. Score: " + score : "Presiona ENTER para jugar";
            g2d.drawString(msg, WIDTH / 2 - 150, HEIGHT / 2);
            return;
        }

        // Fondo
        if (assets.sun != null) {
            g2d.drawImage(assets.sun, WIDTH - 150 - cameraX / 2, 50, 300, 300, null);
        }

        // Nubes
        if (assets.cloud != null) {
            for (Rectangle c : environment.getClouds()) {
                g2d.drawImage(assets.cloud, c.x - cameraX / 2, c.y, c.width, c.height, null);
            }
        }

        // Montañas
        if (assets.mountain != null) {
            for (Rectangle m : environment.getMountains()) {
                g2d.drawImage(assets.mountain, m.x - cameraX / 1, m.y, m.width, m.height, null);
            }
        }

        // Árboles
        if (assets.tree != null) {
            for (Rectangle t : environment.getTrees()) {
                g2d.drawImage(assets.tree, t.x - cameraX, GROUND_Y - t.height, t.width, t.height, null);
            }
        }

        // Castillos
        if (assets.castle != null) {
            for (Rectangle c : environment.getCastles()) {
                g2d.drawImage(assets.castle, c.x - cameraX, c.y, c.width, c.height, null);
            }
        }

        // Suelo
        if (assets.ground != null) {
            int tileWidth = assets.ground.getWidth();
            int tileHeight = assets.ground.getHeight();
            for (int x = -cameraX; x < WORLD_WIDTH; x += tileWidth) {
                g2d.drawImage(assets.ground, x, GROUND_Y, tileWidth, tileHeight, null);
            }
        }

        // Enemigos
        for (Enemy e : enemies) {
            g2d.drawImage(assets.mushroom, e.getRect().x - cameraX, e.getRect().y,
                    e.getRect().width, e.getRect().height, null);
        }

        // Clases.Boss
        if (boss != null && boss.isAlive()) {
            g2d.drawImage(assets.boss, boss.getRect().x - cameraX, boss.getRect().y,
                    boss.getRect().width, boss.getRect().height, null);
            g2d.setColor(Color.RED);
            g2d.fillRect(boss.getRect().x - cameraX, boss.getRect().y - 20, boss.getRect().width, 10);
            g2d.setColor(Color.GREEN);
            g2d.fillRect(boss.getRect().x - cameraX, boss.getRect().y - 20,
                    (int) (boss.getRect().width * ((double) boss.getHealth() / 10)), 10);
        }

        // Balas
        g2d.setColor(Color.RED);
        for (Bullet b : bullets) {
            g2d.fillRect(b.getRect().x - cameraX, b.getRect().y, b.getRect().width, b.getRect().height);
        }

        // Clases.Player
        Rectangle r = mario.getRect();
        BufferedImage[] frames = assets.getCharacterFrames(mario.getCurrentCharacter());
        BufferedImage currentFrame;

        if (!mario.isMoving()) {
            currentFrame = frames[0];
        } else {
            currentFrame = (mario.getAnimationFrame() < mario.getAnimationDelay()) ?
                    frames[0] : frames[1];
        }

        if (mario.isFacingRight()) {
            g2d.drawImage(currentFrame, r.x - cameraX, r.y, r.width, r.height, null);
        } else {
            g2d.drawImage(currentFrame, r.x - cameraX + r.width, r.y, -r.width, r.height, null);
        }

        // HUD
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        g2d.drawString("Score: " + score, 10, 25);
        g2d.drawString("Oleada: " + enemyWaveCount, 10, 50);

        for (int i = 0; i < lives; i++) {
            g2d.drawImage(assets.heart, 10 + i * 35, 70, 40, 40, null);
        }

        if (waveMessageTimer > 0) {
            g2d.setFont(new Font("Arial", Font.BOLD, 30));
            g2d.drawString(waveMessage, WIDTH / 2 - 100, HEIGHT / 2 - 100);
            waveMessageTimer--;
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameStarted || gameOver || !characterSelected) return;

        mario.update();
        environment.updateClouds();

        Rectangle r = mario.getRect();
        if (r.x < 0) r.x = 0;
        if (r.x > WORLD_WIDTH - r.width) r.x = WORLD_WIDTH - r.width;

        cameraX = r.x - WIDTH / 2;
        if (cameraX < 0) cameraX = 0;
        if (cameraX > WORLD_WIDTH - WIDTH) cameraX = WORLD_WIDTH - WIDTH;

        updateBullets();
        updateEnemies();
        repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (!characterSelected) {
            handleCharacterSelection(e);
            return;
        }

        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            if (!gameStarted || gameOver) {
                resetGame();
                startGame();
            }
            return;
        }
        if (!gameStarted || gameOver) return;

        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT -> mario.moveLeft();
            case KeyEvent.VK_RIGHT -> mario.moveRight();
            case KeyEvent.VK_SPACE -> mario.jump();
            case KeyEvent.VK_Z -> shoot();
            case KeyEvent.VK_W -> mario.setFacingRight(!mario.isFacingRight());
        }
    }

    private void handleCharacterSelection(KeyEvent e) {
        CharacterType[] characters = CharacterType.values();

        switch (e.getKeyCode()) {
            case KeyEvent.VK_RIGHT:
                selectedCharacter = characters[(selectedCharacter.ordinal() + 1) % characters.length];
                break;
            case KeyEvent.VK_LEFT:
                int index = selectedCharacter.ordinal() - 1;
                if (index < 0) index = characters.length - 1;
                selectedCharacter = characters[index];
                break;
            case KeyEvent.VK_ENTER:
                characterSelected = true;
                mario = new Player(60, GROUND_Y, selectedCharacter);
                startGame();
                break;
        }
        repaint();
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_RIGHT) mario.stop();
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Mario vs Hongos");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(new MarioGame());
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}