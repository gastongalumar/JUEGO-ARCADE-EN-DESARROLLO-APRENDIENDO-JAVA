package Clases;

import Enums.CharacterType;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.EnumMap;

public class GameAssets {
    private final EnumMap<CharacterType, BufferedImage[]> characterAnimations = new EnumMap<>(CharacterType.class);
    public BufferedImage mushroom, tree, mountain, castle, heart, boss, sun, cloud, ground;

    public GameAssets() {
        loadCharacterAssets();
        loadCommonAssets();
    }

    private void loadCharacterAssets() {
        for (CharacterType type : CharacterType.values()) {
            try {
                BufferedImage[] frames = new BufferedImage[2];
                frames[0] = ImageIO.read(getClass().getResourceAsStream(type.getFrame1Path()));
                frames[1] = ImageIO.read(getClass().getResourceAsStream(type.getFrame2Path()));
                characterAnimations.put(type, frames);
            } catch (IOException | IllegalArgumentException e) {
                System.err.println("Error loading character frames for " + type.getName() + ": " + e.getMessage());
                characterAnimations.put(type, createDefaultCharacterFrames(type));
            }
        }
    }

    private void loadCommonAssets() {
        try {
            mushroom = loadImage("/assets/hongoBoss.png", this::createMushroomImage);
            tree = loadImage("/assets/arbol.png", this::createTreeImage);
            mountain = loadImage("/assets/montaña.png", this::createMountainImage);
            castle = loadImage("/assets/castillo.png", this::createCastleImage);
            heart = loadImage("/assets/corazon.png", this::createHeartImage);
            boss = loadImage("/assets/raton.png", this::createBossImage);
            sun = loadImage("/assets/sol.png", this::createSunImage);
            cloud = loadImage("/assets/nube.png", this::createCloudImage);
            ground = loadImage("/assets/suelo.png", this::createGroundImage);
        } catch (Exception e) {
            System.err.println("Error loading common assets: " + e.getMessage());
        }
    }

    private BufferedImage loadImage(String path, ImageCreator creator) throws IOException {
        try {
            return ImageIO.read(getClass().getResourceAsStream(path));
        } catch (Exception e) {
            System.err.println("Creating placeholder for: " + path);
            return creator.create();
        }
    }

    // Métodos para crear placeholders
    private BufferedImage[] createDefaultCharacterFrames(CharacterType type) {
        return new BufferedImage[] {
                createCharacterPlaceholder(type.getName() + " 1", Color.RED),
                createCharacterPlaceholder(type.getName() + " 2", Color.BLUE)
        };
    }

    private BufferedImage createCharacterPlaceholder(String text, Color bgColor) {
        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();

        // Fondo
        g2d.setColor(bgColor);
        g2d.fillRect(0, 0, 100, 100);

        // Texto
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        drawCenteredString(g2d, text, new Rectangle(0, 0, 100, 100));

        g2d.dispose();
        return img;
    }

    private void drawCenteredString(Graphics2D g, String text, Rectangle rect) {
        FontMetrics metrics = g.getFontMetrics();
        int x = rect.x + (rect.width - metrics.stringWidth(text)) / 2;
        int y = rect.y + ((rect.height - metrics.getHeight()) / 2) + metrics.getAscent();
        g.drawString(text, x, y);
    }

    // Placeholders para assets comunes
    private interface ImageCreator {
        BufferedImage create();
    }

    private BufferedImage createMushroomImage() {
        BufferedImage img = new BufferedImage(40, 40, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setColor(Color.RED);
        g2d.fillOval(5, 5, 30, 25);
        g2d.setColor(Color.WHITE);
        g2d.fillOval(10, 10, 8, 8);
        g2d.fillOval(22, 15, 6, 6);
        g2d.setColor(new Color(222, 184, 135));
        g2d.fillRect(15, 25, 10, 10);
        g2d.dispose();
        return img;
    }

    private BufferedImage createTreeImage() {
        BufferedImage img = new BufferedImage(80, 150, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setColor(new Color(139, 69, 19));
        g2d.fillRect(30, 70, 20, 80);
        g2d.setColor(new Color(34, 139, 34));
        g2d.fillOval(10, 20, 60, 60);
        g2d.fillOval(0, 40, 80, 50);
        g2d.dispose();
        return img;
    }

    private BufferedImage createMountainImage() {
        BufferedImage img = new BufferedImage(400, 300, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setColor(new Color(70, 70, 70));
        int[] xP = {0, 200, 400}, yP = {300, 50, 300};
        g2d.fillPolygon(xP, yP, 3);
        g2d.setColor(Color.WHITE);
        int[] sx = {150, 200, 250}, sy = {100, 50, 100};
        g2d.fillPolygon(sx, sy, 3);
        g2d.dispose();
        return img;
    }

    private BufferedImage createCastleImage() {
        BufferedImage img = new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setColor(new Color(169, 169, 169));
        g2d.fillRect(0, 50, 200, 150);
        g2d.setColor(Color.DARK_GRAY);
        g2d.fillRect(20, 100, 40, 100);
        g2d.fillRect(140, 80, 40, 120);
        g2d.setColor(Color.RED);
        g2d.fillPolygon(new int[]{20, 40, 60}, new int[]{100, 60, 100}, 3);
        g2d.fillPolygon(new int[]{140, 160, 180}, new int[]{80, 40, 80}, 3);
        g2d.dispose();
        return img;
    }

    private BufferedImage createHeartImage() {
        BufferedImage img = new BufferedImage(30, 30, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setColor(Color.RED);
        g2d.fillOval(0, 0, 20, 20);
        g2d.fillOval(10, 0, 20, 20);
        g2d.fillRect(5, 5, 20, 20);
        g2d.dispose();
        return img;
    }

    private BufferedImage createBossImage() {
        BufferedImage img = new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setColor(Color.GRAY);
        g2d.fillOval(50, 50, 100, 100);
        g2d.fillOval(30, 30, 40, 40);
        g2d.fillOval(130, 30, 40, 40);
        g2d.setColor(Color.RED);
        g2d.fillOval(70, 80, 20, 20);
        g2d.fillOval(110, 80, 20, 20);
        g2d.setColor(Color.PINK);
        g2d.fillOval(85, 110, 30, 20);
        g2d.dispose();
        return img;
    }

    private BufferedImage createCloudImage() {
        BufferedImage img = new BufferedImage(100, 50, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setColor(Color.WHITE);
        g2d.fillOval(0, 10, 50, 30);
        g2d.fillOval(30, 0, 50, 40);
        g2d.dispose();
        return img;
    }

    private BufferedImage createSunImage() {
        BufferedImage img = new BufferedImage(80, 80, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setColor(Color.YELLOW);
        g2d.fillOval(0, 0, 80, 80);
        g2d.dispose();
        return img;
    }

    private BufferedImage createGroundImage() {
        BufferedImage img = new BufferedImage(1360, 50, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setColor(new Color(139, 69, 19));
        g2d.fillRect(0, 0, 1360, 50);
        g2d.dispose();
        return img;
    }

    public BufferedImage[] getCharacterFrames(CharacterType type) {
        return characterAnimations.getOrDefault(type, createDefaultCharacterFrames(type));
    }
}