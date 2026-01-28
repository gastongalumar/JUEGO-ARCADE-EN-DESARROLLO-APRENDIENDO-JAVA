import Clases.*;
import Enums.CharacterType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;

public class MarioGame extends JPanel implements ActionListener, KeyListener {
    private enum GameState {
        TITLE_SCREEN, CHARACTER_SELECTION, PLAYING, GAME_OVER, PAUSE
    }

    private static final int WIDTH = 1360;
    private static final int HEIGHT = 768;
    private static final int GROUND_Y = HEIGHT - 50;
    private static final int WORLD_WIDTH = 5000;

    private Player player;
    private ArrayList<Enemy> enemies;
    private ArrayList<Bullet> bullets;
    private Boss boss;
    private Environment environment;
    private GameAssets assets;
    private Random rand;

    private GameState gameState = GameState.TITLE_SCREEN;
    private int score;
    private int enemyWaveCount;
    private int lives;
    private int cameraX;
    private CharacterType selectedCharacter = CharacterType.MARIO;

    private Timer gameTimer, spawnTimer, waveTimer, titleTimer;

    private String waveMessage = "";
    private int waveMessageTimer = 0;
    private int titleAnimationFrame = 0;
    private int characterSelectIndex = 0;
    private int blinkTimer = 0; // Timer para controlar el parpadeo

    // Variables para el menú mejorado
    private int[] characterPositions;
    private int[] characterSpeeds;
    private boolean[] characterDirections;
    private ArrayList<Cloud> clouds;
    private int sunPulse = 0;
    private float sunPulseDirection = 0.1f;

    private boolean[] keys = new boolean[256];

    public MarioGame() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(new Color(135, 206, 235));
        setFocusable(true);
        addKeyListener(this);

        rand = new Random();
        assets = new GameAssets();

        initializeGame();
        initializeTitleScreen();
    }

    private void initializeGame() {
        player = null;
        enemies = new ArrayList<>();
        bullets = new ArrayList<>();
        boss = null;
        environment = new Environment(GROUND_Y, WORLD_WIDTH);

        score = 0;
        lives = 3;
        enemyWaveCount = 1;
        cameraX = 0;

        gameTimer = new Timer(16, this); // 60 FPS
        spawnTimer = new Timer(500, e -> spawnEnemy());
        waveTimer = new Timer(10000, e -> advanceWave());
        titleTimer = new Timer(100, e -> updateTitleScreen());

        titleTimer.start();
    }

    private void initializeTitleScreen() {
        // Inicializar posiciones de personajes corriendo
        CharacterType[] characters = CharacterType.values();
        characterPositions = new int[characters.length];
        characterSpeeds = new int[characters.length];
        characterDirections = new boolean[characters.length]; // true = derecha, false = izquierda

        for (int i = 0; i < characters.length; i++) {
            characterPositions[i] = rand.nextInt(WIDTH);
            characterSpeeds[i] = 2 + rand.nextInt(3);
            characterDirections[i] = rand.nextBoolean();
        }

        // Inicializar nubes
        clouds = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            clouds.add(new Cloud(
                    rand.nextInt(WIDTH),
                    rand.nextInt(200) + 50,
                    1 + rand.nextInt(2),
                    rand.nextBoolean()
            ));
        }
    }

    private void updateTitleScreen() {
        // Actualizar animación de parpadeo
        titleAnimationFrame = (titleAnimationFrame + 1) % 8;
        blinkTimer = (blinkTimer + 1) % 40;

        // Actualizar posición de personajes
        CharacterType[] characters = CharacterType.values();
        for (int i = 0; i < characters.length; i++) {
            if (characterDirections[i]) {
                characterPositions[i] += characterSpeeds[i];
                if (characterPositions[i] > WIDTH + 200) {
                    characterPositions[i] = -200;
                    characterSpeeds[i] = 2 + rand.nextInt(3);
                }
            } else {
                characterPositions[i] -= characterSpeeds[i];
                if (characterPositions[i] < -200) {
                    characterPositions[i] = WIDTH + 200;
                    characterSpeeds[i] = 2 + rand.nextInt(3);
                }
            }
        }

        // Actualizar nubes
        for (Cloud cloud : clouds) {
            cloud.update();
        }

        // Actualizar efecto de pulso del sol
        sunPulse += sunPulseDirection * 2;
        if (sunPulse > 10 || sunPulse < -10) {
            sunPulseDirection *= -1;
        }

        repaint();
    }

    private void startNewGame() {
        player = new Player(60, GROUND_Y, selectedCharacter);
        player.setCharacterFrames(assets.getCharacterFrames(selectedCharacter));

        enemies.clear();
        bullets.clear();
        boss = null;

        score = 0;
        lives = 3;
        enemyWaveCount = 1;
        cameraX = 0;

        gameTimer.start();
        spawnTimer.start();
        waveTimer.start();
        titleTimer.stop();
        gameState = GameState.PLAYING;

        waveMessage = "¡COMIENZA LA AVENTURA!";
        waveMessageTimer = 120;
    }

    private void advanceWave() {
        enemyWaveCount++;
        waveMessage = "¡OLEADA " + enemyWaveCount + "!";
        waveMessageTimer = 120;

        if (enemyWaveCount % 3 == 0) {
            spawnBoss();
        }
    }

    private void spawnEnemy() {
        if (gameState != GameState.PLAYING || player == null) return;

        for (int i = 0; i < enemyWaveCount; i++) {
            int y = 50 + rand.nextInt(GROUND_Y - 200);
            boolean fromRight = rand.nextBoolean();
            int x = fromRight ? WORLD_WIDTH : -80;
            int speed = fromRight ? -3 : 3;
            enemies.add(new Enemy(x, y, 80, 80, speed, 2 + rand.nextInt(3)));
        }
    }

    private void spawnBoss() {
        int bossY = GROUND_Y - 200;
        boss = new Boss(WORLD_WIDTH / 2 - 100, bossY, 200, 200, 10);
        waveMessage = "¡BOSS HA APARECIDO!";
        waveMessageTimer = 120;
    }

    private void updateGame() {
        if (player == null) return;

        // Actualizar jugador con plataformas para colisiones
        player.update(environment.getPlatforms());

        // Controlar cámara
        Rectangle playerRect = player.getRect();
        cameraX = playerRect.x - WIDTH / 2;
        cameraX = Math.max(0, Math.min(cameraX, WORLD_WIDTH - WIDTH));

        // Actualizar entrada del jugador
        handlePlayerInput();

        // Actualizar enemigos
        updateEnemies();

        // Actualizar balas
        updateBullets();

        // Actualizar boss
        if (boss != null && boss.isAlive()) {
            boss.update(GROUND_Y);

            // Verificar colisión con el jugador
            if (playerRect.intersects(boss.getRect())) {
                if (player.takeDamage()) {
                    lives--;
                    player.getRect().x -= 100;
                    if (lives <= 0) {
                        gameOver();
                    }
                }
            }
        }

        // Actualizar nubes del entorno
        if (environment != null) {
            environment.updateClouds();
        }

        // Actualizar mensaje de oleada
        if (waveMessageTimer > 0) {
            waveMessageTimer--;
        }

        // Verificar si el boss fue derrotado
        if (boss != null && !boss.isAlive()) {
            score += 1000;
            boss = null;
            waveMessage = "¡BOSS DERROTADO! +1000 PUNTOS";
            waveMessageTimer = 120;
        }

        // Verificar si el jugador cayó al vacío
        if (playerRect.y > HEIGHT + 100) {
            lives = 0;
            gameOver();
        }
    }

    private void handlePlayerInput() {
        if (keys[KeyEvent.VK_LEFT]) {
            player.moveLeft();
        } else if (keys[KeyEvent.VK_RIGHT]) {
            player.moveRight();
        } else {
            player.stop();
        }
    }

    private void updateEnemies() {
        ArrayList<Enemy> enemiesToRemove = new ArrayList<>();

        for (Enemy enemy : enemies) {
            enemy.update(GROUND_Y);

            // Verificar colisión con el jugador
            if (player.getRect().intersects(enemy.getRect())) {
                if (player.takeDamage()) {
                    lives--;
                    player.getRect().x -= 50;
                    if (lives <= 0) {
                        gameOver();
                    }
                }
                enemiesToRemove.add(enemy);
                score += 10;
            }

            // Eliminar enemigos fuera de pantalla
            if (enemy.getRect().x < -100 || enemy.getRect().x > WORLD_WIDTH + 100) {
                enemiesToRemove.add(enemy);
            }
        }

        enemies.removeAll(enemiesToRemove);
    }

    private void updateBullets() {
        ArrayList<Bullet> bulletsToRemove = new ArrayList<>();

        for (Bullet bullet : bullets) {
            bullet.update();

            // Verificar colisión con enemigos
            for (int i = 0; i < enemies.size(); i++) {
                if (bullet.getRect().intersects(enemies.get(i).getRect())) {
                    bulletsToRemove.add(bullet);
                    enemies.remove(i);
                    score += 10;
                    break;
                }
            }

            // Verificar colisión con el boss
            if (boss != null && boss.isAlive() && bullet.getRect().intersects(boss.getRect())) {
                bulletsToRemove.add(bullet);
                boss.damage();
                score += 100;
            }

            // Eliminar balas fuera de pantalla
            if (bullet.getRect().x < -50 || bullet.getRect().x > WORLD_WIDTH + 50) {
                bulletsToRemove.add(bullet);
            }
        }

        bullets.removeAll(bulletsToRemove);
    }

    private void gameOver() {
        gameState = GameState.GAME_OVER;
        gameTimer.stop();
        spawnTimer.stop();
        waveTimer.stop();
        titleTimer.start();
    }

    private void shoot() {
        if (player == null || gameState != GameState.PLAYING) return;

        int direction = player.isFacingRight() ? 1 : -1;
        Rectangle rect = player.getRect();
        int x = direction > 0 ? rect.x + rect.width : rect.x;
        int y = rect.y + rect.height / 2 - 5;

        bullets.add(new Bullet(x, y, direction));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        switch (gameState) {
            case TITLE_SCREEN -> drawTitleScreen(g2d);
            case CHARACTER_SELECTION -> drawCharacterSelection(g2d);
            case PLAYING -> drawGame(g2d);
            case GAME_OVER -> drawGameOver(g2d);
            case PAUSE -> {
                drawGame(g2d);
                drawPauseScreen(g2d);
            }
        }
    }

    private void drawTitleScreen(Graphics2D g2d) {
        // Fondo del cielo
        GradientPaint skyGradient = new GradientPaint(
                0, 0, new Color(135, 206, 235),
                0, HEIGHT, new Color(100, 149, 237)
        );
        g2d.setPaint(skyGradient);
        g2d.fillRect(0, 0, WIDTH, HEIGHT);

        // Montañas al fondo
        drawMountains(g2d);

        // Sol con efecto de pulso
        int sunSize = 120 + sunPulse;
        if (assets.sun != null) {
            g2d.drawImage(assets.sun, WIDTH - 200, 50, sunSize, sunSize, null);
        } else {
            g2d.setColor(Color.YELLOW);
            g2d.fillOval(WIDTH - 200, 50, sunSize, sunSize);
        }

        // Nubes del menú
        for (Cloud cloud : clouds) {
            cloud.draw(g2d, assets);
        }

        // Castillos decorativos
        drawCastles(g2d);

        // Suelo
        drawGround(g2d);

        // Personajes corriendo por el suelo - ¡CORREGIDO CON ANIMACIÓN!
        drawRunningCharacters(g2d);

        // Título principal
        drawMainTitle(g2d);

        // Instrucciones parpadeantes
        if (blinkTimer < 30) {
            drawInstructions(g2d);
        }

        // Créditos
        drawCredits(g2d);
    }

    private void drawMountains(Graphics2D g2d) {
        // Montañas grandes al fondo
        g2d.setColor(new Color(80, 80, 80, 180));
        int[] mountain1 = {0, 400, 800, 0};
        int[] mountain1y = {HEIGHT, 200, HEIGHT, HEIGHT};
        g2d.fillPolygon(mountain1, mountain1y, 4);

        g2d.setColor(new Color(90, 90, 90, 200));
        int[] mountain2 = {600, 1000, 1400, 600};
        int[] mountain2y = {HEIGHT, 250, HEIGHT, HEIGHT};
        g2d.fillPolygon(mountain2, mountain2y, 4);
    }

    private void drawCastles(Graphics2D g2d) {
        if (assets.castle != null) {
            g2d.drawImage(assets.castle, 100, GROUND_Y - 300, 250, 300, null);
            g2d.drawImage(assets.castle, WIDTH - 350, GROUND_Y - 280, 250, 280, null);
        } else {
            g2d.setColor(new Color(169, 169, 169));
            g2d.fillRect(100, GROUND_Y - 300, 200, 300);
            g2d.fillRect(WIDTH - 300, GROUND_Y - 280, 200, 280);
        }
    }

    private void drawGround(Graphics2D g2d) {
        // Suelo principal
        g2d.setColor(new Color(139, 69, 19));
        g2d.fillRect(0, GROUND_Y, WIDTH, HEIGHT - GROUND_Y);

        // Textura del suelo
        g2d.setColor(new Color(160, 82, 45));
        for (int i = 0; i < WIDTH; i += 40) {
            g2d.fillRect(i, GROUND_Y, 20, HEIGHT - GROUND_Y);
        }

        // Hierba en el borde
        g2d.setColor(new Color(34, 139, 34));
        for (int i = 0; i < WIDTH; i += 20) {
            int grassHeight = 10 + (i % 40) / 2;
            g2d.fillRect(i, GROUND_Y - grassHeight, 15, grassHeight);
        }
    }

    private void drawRunningCharacters(Graphics2D g2d) {
        CharacterType[] characters = CharacterType.values();

        for (int i = 0; i < characters.length; i++) {
            int x = characterPositions[i];
            int y = GROUND_Y - 150;

            // OBTENER FRAMES DE ANIMACIÓN - ¡ESTO ES LO IMPORTANTE!
            BufferedImage[] frames = assets.getCharacterFrames(characters[i]);

            if (frames != null && frames.length > 1) {
                // Usar animación basada en la posición del personaje
                // Cada personaje tiene su propia animación sincronizada con su movimiento
                int animationIndex = (characterPositions[i] / 30) % 2;
                BufferedImage currentFrame = frames[animationIndex];

                int drawWidth = 120;
                int drawHeight = 120;

                if (characterDirections[i]) {
                    // Mueve hacia la derecha - imagen normal
                    g2d.drawImage(currentFrame, x, y, drawWidth, drawHeight, null);
                } else {
                    // Mueve hacia la izquierda - voltear horizontalmente
                    g2d.drawImage(currentFrame, x + drawWidth, y, -drawWidth, drawHeight, null);
                }
            } else {
                // Fallback a imagen estática
                BufferedImage charImage = assets.getCharacterTitleImage(characters[i]);
                int drawWidth = 120;
                int drawHeight = 120;

                if (characterDirections[i]) {
                    g2d.drawImage(charImage, x, y, drawWidth, drawHeight, null);
                } else {
                    g2d.drawImage(charImage, x + drawWidth, y, -drawWidth, drawHeight, null);
                }
            }

            // Sombra del personaje
            g2d.setColor(new Color(0, 0, 0, 100));
            g2d.fillOval(x + 20, GROUND_Y - 20, 80, 30);

            // Nombre del personaje
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 14));
            g2d.drawString(characters[i].getName(), x + 30, y - 10);
        }
    }

    private void drawMainTitle(Graphics2D g2d) {
        // Sombra del título
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.setFont(new Font("Arial", Font.BOLD, 100));

        String title = "MARIO VS HONGOS";
        FontMetrics fm = g2d.getFontMetrics();
        int titleWidth = fm.stringWidth(title);
        int titleX = (WIDTH - titleWidth) / 2;
        int titleY = 180;

        // Sombra múltiple para efecto 3D
        for (int i = 0; i < 5; i++) {
            g2d.drawString(title, titleX + i, titleY + i);
        }

        // Título principal con gradiente
        GradientPaint titleGradient = new GradientPaint(
                titleX, titleY - 50, Color.RED,
                titleX + titleWidth, titleY + 50, Color.YELLOW
        );
        g2d.setPaint(titleGradient);
        g2d.drawString(title, titleX, titleY);

        // Borde del título
        g2d.setColor(Color.YELLOW);
        g2d.setStroke(new BasicStroke(3));
        g2d.drawString(title, titleX, titleY);

        // Subtítulo
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 40));
        String subtitle = "¡LA BATALLA ÉPICA!";
        int subtitleWidth = g2d.getFontMetrics().stringWidth(subtitle);
        g2d.drawString(subtitle, (WIDTH - subtitleWidth) / 2, 250);

        // Efecto de brillo intermitente
        if (blinkTimer < 10) {
            g2d.setColor(new Color(255, 255, 100, 150));
            g2d.fillRoundRect((WIDTH - subtitleWidth) / 2 - 20, 210, subtitleWidth + 40, 50, 25, 25);
        }
    }

    private void drawInstructions(Graphics2D g2d) {
        g2d.setColor(Color.CYAN);
        g2d.setFont(new Font("Arial", Font.BOLD, 30));

        String pressEnter = "PRESIONA ENTER PARA COMENZAR";
        int textWidth = g2d.getFontMetrics().stringWidth(pressEnter);
        int textX = (WIDTH - textWidth) / 2;
        int textY = HEIGHT - 150;

        // Fondo para el texto
        g2d.setColor(new Color(0, 0, 0, 100));
        g2d.fillRoundRect(textX - 20, textY - 35, textWidth + 40, 50, 25, 25);

        // Texto con efecto de brillo
        GradientPaint textGradient = new GradientPaint(
                textX, textY - 10, Color.CYAN,
                textX + textWidth, textY + 10, Color.WHITE
        );
        g2d.setPaint(textGradient);
        g2d.drawString(pressEnter, textX, textY);

        // Instrucciones adicionales
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.PLAIN, 20));
        g2d.drawString("Selecciona tu personaje favorito para la batalla", WIDTH/2 - 200, HEIGHT - 100);
    }

    private void drawCredits(Graphics2D g2d) {
        g2d.setColor(new Color(200, 200, 255, 150));
        g2d.setFont(new Font("Arial", Font.PLAIN, 16));
        g2d.drawString("© 2026 - Mario vs Hongos - Todos los derechos reservados", WIDTH/2 - 200, HEIGHT - 30);
    }

    private void drawCharacterSelection(Graphics2D g2d) {
        // Fondo similar al título
        drawTitleScreenBackground(g2d);

        // Título de selección
        g2d.setColor(Color.YELLOW);
        g2d.setFont(new Font("Arial", Font.BOLD, 60));
        String selectText = "SELECCIONA TU PERSONAJE";
        int textWidth = g2d.getFontMetrics().stringWidth(selectText);
        g2d.drawString(selectText, WIDTH/2 - textWidth/2, 80);

        CharacterType[] characters = CharacterType.values();
        int startX = WIDTH/2 - (characters.length * 220) / 2;
        int y = 200;

        for (int i = 0; i < characters.length; i++) {
            CharacterType character = characters[i];

            // Marco del personaje seleccionado con efectos
            if (i == characterSelectIndex) {
                g2d.setColor(Color.YELLOW);
                g2d.setStroke(new BasicStroke(6));
                g2d.drawRoundRect(startX + i * 220 - 15, y - 15, 220 + 30, 220 + 30, 25, 25);

                g2d.setColor(new Color(255, 255, 100, 80));
                for (int j = 0; j < 3; j++) {
                    g2d.drawRoundRect(startX + i * 220 - 10 + j, y - 10 + j,
                            220 + 20 - j*2, 220 + 20 - j*2, 20, 20);
                }

                g2d.setColor(new Color(255, 255, 100, 30));
                g2d.fillRoundRect(startX + i * 220 - 10, y - 10, 220 + 20, 220 + 20, 20, 20);
            }

            // Dibujar personaje con animación
            BufferedImage[] frames = assets.getCharacterFrames(character);
            if (frames != null && frames.length > 0) {
                // Animación de selección
                BufferedImage frame = (titleAnimationFrame < 4) ? frames[0] : frames[1];
                g2d.drawImage(frame, startX + i * 220, y, 200, 200, null);
            } else {
                g2d.setColor(getCharacterColor(character));
                g2d.fillRoundRect(startX + i * 220, y, 200, 200, 20, 20);
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Arial", Font.BOLD, 24));
                drawCenteredString(g2d, character.getName(),
                        new Rectangle(startX + i * 220, y, 200, 200));
            }

            // Nombre del personaje
            g2d.setColor(i == characterSelectIndex ? Color.YELLOW : Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 22));
            String name = character.getName();
            int nameWidth = g2d.getFontMetrics().stringWidth(name);
            g2d.drawString(name, startX + i * 220 + (200 - nameWidth)/2, y + 240);

            // Indicador de selección
            if (i == characterSelectIndex && blinkTimer < 20) {
                g2d.setColor(Color.GREEN);
                g2d.setFont(new Font("Arial", Font.BOLD, 18));
                String selectedText = "← SELECCIONADO →";
                int selWidth = g2d.getFontMetrics().stringWidth(selectedText);
                g2d.drawString(selectedText, startX + i * 220 + (200 - selWidth)/2, y + 270);
            }
        }

        // Instrucciones con parpadeo
        if (blinkTimer < 30) {
            g2d.setColor(Color.CYAN);
            g2d.setFont(new Font("Arial", Font.BOLD, 28));

            String[] instructions = {
                    "Usa ← → para seleccionar",
                    "Presiona ENTER para jugar",
                    "ESC para volver al título"
            };

            for (int i = 0; i < instructions.length; i++) {
                int textW = g2d.getFontMetrics().stringWidth(instructions[i]);
                g2d.drawString(instructions[i], WIDTH/2 - textW/2, 500 + i * 40);
            }
        }

        // Descripción del personaje seleccionado
        drawCharacterDescription(g2d, characters[characterSelectIndex]);
    }

    private void drawTitleScreenBackground(Graphics2D g2d) {
        GradientPaint skyGradient = new GradientPaint(
                0, 0, new Color(30, 30, 70),
                0, HEIGHT, Color.BLACK
        );
        g2d.setPaint(skyGradient);
        g2d.fillRect(0, 0, WIDTH, HEIGHT);

        // Estrellas
        g2d.setColor(Color.WHITE);
        for (int i = 0; i < 50; i++) {
            int x = rand.nextInt(WIDTH);
            int y = rand.nextInt(400);
            int size = 1 + rand.nextInt(3);
            g2d.fillOval(x, y, size, size);

            if (rand.nextFloat() < 0.3 && blinkTimer < 20) {
                g2d.setColor(new Color(255, 255, 255, 100 + rand.nextInt(155)));
                g2d.fillOval(x - 1, y - 1, size + 2, size + 2);
                g2d.setColor(Color.WHITE);
            }
        }
    }

    private void drawCharacterDescription(Graphics2D g2d, CharacterType character) {
        String description = getCharacterDescription(character);
        g2d.setColor(new Color(200, 200, 255));
        g2d.setFont(new Font("Arial", Font.PLAIN, 18));

        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRoundRect(WIDTH/2 - 300, 620, 600, 100, 20, 20);

        g2d.setColor(Color.WHITE);
        drawCenteredString(g2d, description, new Rectangle(WIDTH/2 - 300, 620, 600, 100));
    }

    private String getCharacterDescription(CharacterType character) {
        switch (character) {
            case MARIO: return "El fontanero más famoso del mundo. ¡Su salto es legendario!";
            case PEPPA: return "La cerdita más aventurera. ¡Saltar en charcos es su especialidad!";
            case HOMERO: return "¡D'oh! El padre de familia más torpe pero con gran corazón (y hambre).";
            case JOHN_WICK: return "El asesino más letal. Nadie toca a su perro... ¡ni a sus hongos!";
            case EMPANADA: return "La empanada más gaucha del mundo. ¡Es pura tradición y sabor!";
            default: return "Un valiente héroe listo para la batalla.";
        }
    }

    private Color getCharacterColor(CharacterType character) {
        switch (character) {
            case MARIO: return Color.RED;
            case PEPPA: return new Color(255, 182, 193);
            case HOMERO: return Color.YELLOW;
            case JOHN_WICK: return new Color(50, 50, 50);
            case EMPANADA: return new Color(160, 82, 45);
            default: return Color.BLUE;
        }
    }

    private void drawGame(Graphics2D g2d) {
        // Aplicar transformación de cámara
        g2d.translate(-cameraX, 0);

        // Dibujar mundo
        drawWorld(g2d);

        // Dibujar entidades
        drawEntities(g2d);

        // Restaurar transformación
        g2d.translate(cameraX, 0);

        // Dibujar HUD
        drawHUD(g2d);

        // Dibujar mensaje de oleada
        if (waveMessageTimer > 0) {
            drawWaveMessage(g2d);
        }
    }

    private void drawWorld(Graphics2D g2d) {
        // Cielo
        g2d.setColor(new Color(135, 206, 235));
        g2d.fillRect(cameraX, 0, WORLD_WIDTH, HEIGHT);

        // Sol
        if (assets.sun != null) {
            g2d.drawImage(assets.sun, WORLD_WIDTH - 200, 50, 100, 100, null);
        }

        // Nubes del entorno
        if (assets.cloud != null) {
            for (Rectangle cloud : environment.getClouds()) {
                g2d.drawImage(assets.cloud, cloud.x, cloud.y, cloud.width, cloud.height, null);
            }
        }

        // Montañas del entorno
        if (assets.mountain != null) {
            for (Rectangle mountain : environment.getMountains()) {
                g2d.drawImage(assets.mountain, mountain.x, mountain.y,
                        mountain.width, mountain.height, null);
            }
        }

        // Castillos del entorno
        if (assets.castle != null) {
            for (Rectangle castle : environment.getCastles()) {
                g2d.drawImage(assets.castle, castle.x, castle.y,
                        castle.width, castle.height, null);
            }
        }

        // Árboles del entorno
        if (assets.tree != null) {
            for (Rectangle tree : environment.getTrees()) {
                g2d.drawImage(assets.tree, tree.x, GROUND_Y - tree.height,
                        tree.width, tree.height, null);
            }
        }

        // Suelo del entorno
        if (assets.ground != null) {
            int tileWidth = assets.ground.getWidth();
            for (int x = 0; x < WORLD_WIDTH; x += tileWidth) {
                g2d.drawImage(assets.ground, x, GROUND_Y, tileWidth, 50, null);
            }
        } else {
            g2d.setColor(new Color(139, 69, 19));
            g2d.fillRect(0, GROUND_Y, WORLD_WIDTH, 50);
        }
    }

    private void drawEntities(Graphics2D g2d) {
        // Enemigos
        for (Enemy enemy : enemies) {
            if (assets.mushroom != null) {
                g2d.drawImage(assets.mushroom, enemy.getRect().x, enemy.getRect().y,
                        enemy.getRect().width, enemy.getRect().height, null);
            } else {
                g2d.setColor(Color.RED);
                g2d.fillRect(enemy.getRect().x, enemy.getRect().y,
                        enemy.getRect().width, enemy.getRect().height);
            }
        }

        // Boss
        if (boss != null && boss.isAlive()) {
            if (assets.boss != null) {
                g2d.drawImage(assets.boss, boss.getRect().x, boss.getRect().y,
                        boss.getRect().width, boss.getRect().height, null);
            } else {
                g2d.setColor(Color.GRAY);
                g2d.fillRect(boss.getRect().x, boss.getRect().y,
                        boss.getRect().width, boss.getRect().height);
            }

            // Barra de salud del boss
            drawHealthBar(g2d, boss.getRect().x, boss.getRect().y - 20,
                    boss.getRect().width, 15, boss.getHealth(), 10);
        }

        // Balas
        g2d.setColor(Color.ORANGE);
        for (Bullet bullet : bullets) {
            g2d.fillRect(bullet.getRect().x, bullet.getRect().y,
                    bullet.getRect().width, bullet.getRect().height);
        }

        // Jugador
        if (player != null) {
            Rectangle rect = player.getRect();

            boolean shouldDraw = !player.isInvulnerable() ||
                    (player.isInvulnerable() && (player.getInvulnerabilityTimer() / 10) % 2 == 0);

            if (shouldDraw) {
                BufferedImage currentFrame = player.getCurrentFrame();

                if (currentFrame != null) {
                    if (player.isFacingRight()) {
                        g2d.drawImage(currentFrame, rect.x, rect.y, rect.width, rect.height, null);
                    } else {
                        g2d.drawImage(currentFrame, rect.x + rect.width, rect.y,
                                -rect.width, rect.height, null);
                    }
                } else {
                    g2d.setColor(player.isInvulnerable() ?
                            new Color(255, 255, 0, 180) : Color.BLUE);
                    g2d.fillRect(rect.x, rect.y, rect.width, rect.height);

                    g2d.setColor(player.isInvulnerable() ? Color.YELLOW : Color.BLUE.darker());
                    g2d.drawRect(rect.x, rect.y, rect.width, rect.height);
                }

                // Sombra del jugador
                g2d.setColor(new Color(0, 0, 0, 100));
                g2d.fillOval(rect.x + 20, rect.y + rect.height - 10, 60, 20);
            }
        }
    }

    private void drawHealthBar(Graphics2D g2d, int x, int y, int width, int height, int current, int max) {
        // Fondo de la barra
        g2d.setColor(Color.BLACK);
        g2d.fillRect(x - 1, y - 1, width + 2, height + 2);

        // Barra de salud
        int healthWidth = (width * current) / max;
        if (current > max * 0.6) {
            g2d.setColor(Color.GREEN);
        } else if (current > max * 0.3) {
            g2d.setColor(Color.YELLOW);
        } else {
            g2d.setColor(Color.RED);
        }
        g2d.fillRect(x, y, healthWidth, height);

        // Texto de salud
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        String healthText = current + "/" + max;
        int textWidth = g2d.getFontMetrics().stringWidth(healthText);
        g2d.drawString(healthText, x + (width - textWidth) / 2, y + height - 3);

        // Borde
        g2d.setColor(Color.WHITE);
        g2d.drawRect(x, y, width, height);
    }

    private void drawHUD(Graphics2D g2d) {
        // Fondo semi-transparente redondeado
        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.fillRoundRect(15, 15, 350, 130, 20, 20);

        // Borde del HUD
        g2d.setColor(new Color(255, 255, 255, 100));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(15, 15, 350, 130, 20, 20);

        // Información del juego
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 28));
        g2d.drawString("PUNTUACIÓN: " + score, 30, 50);
        g2d.drawString("OLEADA: " + enemyWaveCount, 30, 85);
        g2d.drawString("VIDAS: ", 30, 120);

        // Dibujar corazones para las vidas
        int heartX = 130;
        for (int i = 0; i < lives; i++) {
            if (assets.heart != null) {
                g2d.drawImage(assets.heart, heartX + i * 40, 95, 35, 35, null);
            } else {
                g2d.setColor(Color.RED);
                g2d.fillOval(heartX + i * 40, 95, 30, 30);
                g2d.fillOval(heartX + i * 40 + 15, 95, 30, 30);
                g2d.fillRect(heartX + i * 40 + 10, 100, 25, 25);
            }
        }

        // Controles
        g2d.setColor(new Color(200, 200, 255, 200));
        g2d.setFont(new Font("Arial", Font.PLAIN, 16));
        g2d.drawString("Controles: ← → Mover | SPACE Saltar | Z Disparar | P Pausa | ESC Menú", 20, HEIGHT - 20);
    }

    private void drawWaveMessage(Graphics2D g2d) {
        int alpha = Math.min(200, waveMessageTimer * 2);
        g2d.setColor(new Color(0, 0, 0, alpha));
        g2d.fillRoundRect(WIDTH/2 - 250, 120, 500, 70, 35, 35);

        g2d.setColor(new Color(255, 255, 100, alpha));
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRoundRect(WIDTH/2 - 250, 120, 500, 70, 35, 35);

        if (waveMessageTimer % 20 < 15) {
            g2d.setColor(Color.YELLOW);
            g2d.setFont(new Font("Arial", Font.BOLD, 36));

            g2d.setColor(new Color(0, 0, 0, 150));
            g2d.drawString(waveMessage, WIDTH/2 - 240 + 2, 170 + 2);

            g2d.setColor(Color.YELLOW);
            g2d.drawString(waveMessage, WIDTH/2 - 240, 170);
        }
    }

    private void drawGameOver(Graphics2D g2d) {
        g2d.setColor(new Color(0, 0, 0, 220));
        g2d.fillRect(0, 0, WIDTH, HEIGHT);

        g2d.setColor(new Color(50, 50, 50, 200));
        g2d.fillRoundRect(WIDTH/2 - 400, HEIGHT/2 - 200, 800, 400, 40, 40);

        g2d.setColor(Color.RED);
        g2d.setStroke(new BasicStroke(6));
        g2d.drawRoundRect(WIDTH/2 - 400, HEIGHT/2 - 200, 800, 400, 40, 40);

        g2d.setFont(new Font("Arial", Font.BOLD, 80));

        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.drawString("GAME OVER", WIDTH/2 - 230, HEIGHT/2 - 80);

        GradientPaint gameOverGradient = new GradientPaint(
                WIDTH/2 - 230, HEIGHT/2 - 130, Color.RED,
                WIDTH/2 + 230, HEIGHT/2 - 50, new Color(139, 0, 0)
        );
        g2d.setPaint(gameOverGradient);
        g2d.drawString("GAME OVER", WIDTH/2 - 232, HEIGHT/2 - 82);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 40));
        g2d.drawString("Puntuación Final: " + score, WIDTH/2 - 180, HEIGHT/2 + 10);

        g2d.setFont(new Font("Arial", Font.PLAIN, 30));
        g2d.setColor(new Color(200, 255, 200));
        g2d.drawString("Presiona R para reiniciar", WIDTH/2 - 180, HEIGHT/2 + 80);
        g2d.setColor(new Color(255, 200, 200));
        g2d.drawString("Presiona ESC para salir", WIDTH/2 - 180, HEIGHT/2 + 130);

        if (player != null && blinkTimer < 20) {
            BufferedImage currentFrame = player.getCurrentFrame();
            if (currentFrame != null) {
                g2d.drawImage(currentFrame, WIDTH/2 - 60, HEIGHT/2 - 180, 120, 120, null);
            }
        }
    }

    private void drawPauseScreen(Graphics2D g2d) {
        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.fillRect(0, 0, WIDTH, HEIGHT);

        g2d.setColor(new Color(50, 50, 100, 200));
        g2d.fillRoundRect(WIDTH/2 - 300, HEIGHT/2 - 150, 600, 300, 30, 30);

        g2d.setColor(Color.CYAN);
        g2d.setStroke(new BasicStroke(4));
        g2d.drawRoundRect(WIDTH/2 - 300, HEIGHT/2 - 150, 600, 300, 30, 30);

        g2d.setFont(new Font("Arial", Font.BOLD, 80));

        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.drawString("PAUSA", WIDTH/2 - 130, HEIGHT/2 - 50);

        GradientPaint pauseGradient = new GradientPaint(
                WIDTH/2 - 130, HEIGHT/2 - 100, Color.CYAN,
                WIDTH/2 + 130, HEIGHT/2, Color.BLUE
        );
        g2d.setPaint(pauseGradient);
        g2d.drawString("PAUSA", WIDTH/2 - 132, HEIGHT/2 - 52);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.PLAIN, 30));
        g2d.drawString("Presiona P para continuar", WIDTH/2 - 180, HEIGHT/2 + 30);
        g2d.drawString("Presiona ESC para salir al menú", WIDTH/2 - 230, HEIGHT/2 + 80);

        if (player != null && blinkTimer < 20) {
            BufferedImage currentFrame = player.getCurrentFrame();
            if (currentFrame != null) {
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
                g2d.drawImage(currentFrame, WIDTH/2 - 50, HEIGHT/2 - 140, 100, 100, null);
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));

                g2d.setColor(Color.YELLOW);
                g2d.setFont(new Font("Arial", Font.BOLD, 40));
                g2d.drawString("Zzz...", WIDTH/2 - 30, HEIGHT/2 - 160);
            }
        }
    }

    private void drawCenteredString(Graphics2D g, String text, Rectangle rect) {
        FontMetrics metrics = g.getFontMetrics();
        int x = rect.x + (rect.width - metrics.stringWidth(text)) / 2;
        int y = rect.y + ((rect.height - metrics.getHeight()) / 2) + metrics.getAscent();
        g.drawString(text, x, y);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        keys[keyCode] = true;

        switch (gameState) {
            case TITLE_SCREEN -> handleTitleScreenInput(e);
            case CHARACTER_SELECTION -> handleCharacterSelectionInput(e);
            case PLAYING -> handleGameInput(e);
            case GAME_OVER -> handleGameOverInput(e);
            case PAUSE -> handlePauseInput(e);
        }
    }

    private void handleTitleScreenInput(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            gameState = GameState.CHARACTER_SELECTION;
            repaint();
        }
    }

    private void handleCharacterSelectionInput(KeyEvent e) {
        CharacterType[] characters = CharacterType.values();

        switch (e.getKeyCode()) {
            case KeyEvent.VK_RIGHT -> {
                characterSelectIndex = (characterSelectIndex + 1) % characters.length;
                selectedCharacter = characters[characterSelectIndex];
                repaint();
            }
            case KeyEvent.VK_LEFT -> {
                characterSelectIndex = (characterSelectIndex - 1 + characters.length) % characters.length;
                selectedCharacter = characters[characterSelectIndex];
                repaint();
            }
            case KeyEvent.VK_ENTER -> {
                startNewGame();
                titleTimer.stop();
                repaint();
            }
            case KeyEvent.VK_ESCAPE -> {
                gameState = GameState.TITLE_SCREEN;
                repaint();
            }
        }
    }

    private void handleGameInput(KeyEvent e) {
        if (player == null) return;

        switch (e.getKeyCode()) {
            case KeyEvent.VK_SPACE -> player.jump();
            case KeyEvent.VK_Z -> shoot();
            case KeyEvent.VK_P -> {
                gameState = GameState.PAUSE;
                gameTimer.stop();
                spawnTimer.stop();
                waveTimer.stop();
                titleTimer.start();
                repaint();
            }
            case KeyEvent.VK_ESCAPE -> returnToTitle();
        }
    }

    private void handlePauseInput(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_P -> {
                gameState = GameState.PLAYING;
                gameTimer.start();
                spawnTimer.start();
                waveTimer.start();
                titleTimer.stop();
                repaint();
            }
            case KeyEvent.VK_ESCAPE -> returnToTitle();
        }
    }

    private void handleGameOverInput(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_R -> {
                gameState = GameState.TITLE_SCREEN;
                titleTimer.start();
                initializeGame();
                repaint();
            }
            case KeyEvent.VK_ESCAPE -> System.exit(0);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        keys[e.getKeyCode()] = false;
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    private void returnToTitle() {
        gameTimer.stop();
        spawnTimer.stop();
        waveTimer.stop();
        titleTimer.start();
        gameState = GameState.TITLE_SCREEN;
        repaint();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (gameState == GameState.PLAYING) {
            updateGame();
        }
        repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("MARIO VS HONGOS - LA BATALLA ÉPICA");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            MarioGame game = new MarioGame();
            frame.add(game);

            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setResizable(false);
            frame.setVisible(true);

            game.requestFocusInWindow();
        });
    }

    // Clase interna para manejar nubes en el menú
    private class Cloud {
        int x, y;
        int speed;
        boolean direction; // true = derecha, false = izquierda

        Cloud(int x, int y, int speed, boolean direction) {
            this.x = x;
            this.y = y;
            this.speed = speed;
            this.direction = direction;
        }

        void update() {
            if (direction) {
                x += speed;
                if (x > WIDTH + 200) {
                    x = -200;
                    y = rand.nextInt(200) + 50;
                    speed = 1 + rand.nextInt(2);
                }
            } else {
                x -= speed;
                if (x < -200) {
                    x = WIDTH + 200;
                    y = rand.nextInt(200) + 50;
                    speed = 1 + rand.nextInt(2);
                }
            }
        }

        void draw(Graphics2D g2d, GameAssets assets) {
            if (assets.cloud != null) {
                int cloudWidth = 150 + (int)(Math.sin(x * 0.01) * 20);
                int cloudHeight = 120;
                g2d.drawImage(assets.cloud, x, y, cloudWidth, cloudHeight, null);
            } else {
                g2d.setColor(new Color(255, 255, 255, 220));
                g2d.fillOval(x, y, 100, 60);
                g2d.fillOval(x + 40, y - 20, 120, 70);
                g2d.fillOval(x + 80, y + 10, 90, 50);
            }
        }
    }
}