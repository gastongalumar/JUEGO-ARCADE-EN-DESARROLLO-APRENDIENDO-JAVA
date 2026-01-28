package Clases;

import Enums.CharacterType;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.EnumMap;

public class GameAssets {
    private final EnumMap<CharacterType, BufferedImage[]> characterAnimations = new EnumMap<>(CharacterType.class);
    public BufferedImage mushroom, tree, mountain, castle, heart, boss, sun, cloud, ground;

    public GameAssets() {
        System.out.println("=== CARGANDO IMÁGENES ===");
        loadCommonAssets();
        loadCharacterAssets();
        System.out.println("=== IMÁGENES CARGADAS ===");
    }

    private void loadCharacterAssets() {
        System.out.println("\n--- CARGANDO PERSONAJES ---");
        for (CharacterType type : CharacterType.values()) {
            System.out.println("Personaje: " + type.getName());

            try {
                BufferedImage[] frames = new BufferedImage[2];

                // Cargar frame 1
                frames[0] = loadImageFromAssets(type.getFrame1Path());
                if (frames[0] == null) {
                    System.out.println("  ✗ No se encontró: " + type.getFrame1Path());
                    frames[0] = createSimplePlaceholder(type.getName() + " 1", getCharacterColor(type));
                } else {
                    System.out.println("  ✓ Frame 1 cargado: " + type.getFrame1Path());
                }

                // Cargar frame 2
                frames[1] = loadImageFromAssets(type.getFrame2Path());
                if (frames[1] == null) {
                    System.out.println("  ✗ No se encontró: " + type.getFrame2Path());
                    frames[1] = createSimplePlaceholder(type.getName() + " 2", getCharacterColor(type).brighter());
                } else {
                    System.out.println("  ✓ Frame 2 cargado: " + type.getFrame2Path());
                }

                characterAnimations.put(type, frames);

            } catch (Exception e) {
                System.err.println("  ERROR: " + e.getMessage());
                // Crear placeholders de emergencia
                BufferedImage[] placeholderFrames = new BufferedImage[2];
                placeholderFrames[0] = createSimplePlaceholder(type.getName(), getCharacterColor(type));
                placeholderFrames[1] = createSimplePlaceholder(type.getName(), getCharacterColor(type).brighter());
                characterAnimations.put(type, placeholderFrames);
            }
        }
    }

    private void loadCommonAssets() {
        System.out.println("\n--- CARGANDO ASSETS COMUNES ---");

        // Lista de assets comunes
        String[] assetFiles = {
                "hongoBoss.png", "arbol.png", "montaña.png", "castillo.png",
                "corazon.png", "raton.png", "sol.png", "nube.png", "suelo.png"
        };

        for (String filename : assetFiles) {
            try {
                BufferedImage img = loadImageFromAssets("/" + filename);
                if (img != null) {
                    System.out.println("  ✓ " + filename + " cargado");

                    // Asignar a la variable correspondiente
                    switch (filename) {
                        case "hongoBoss.png": mushroom = img; break;
                        case "arbol.png": tree = img; break;
                        case "montaña.png": mountain = img; break;
                        case "castillo.png": castle = img; break;
                        case "corazon.png": heart = img; break;
                        case "raton.png": boss = img; break;
                        case "sol.png": sun = img; break;
                        case "nube.png": cloud = img; break;
                        case "suelo.png": ground = img; break;
                    }
                } else {
                    System.out.println("  ✗ " + filename + " NO encontrado");
                }
            } catch (Exception e) {
                System.err.println("  ERROR cargando " + filename + ": " + e.getMessage());
            }
        }
    }

    private BufferedImage loadImageFromAssets(String filename) {
        try {
            // Primero intentar desde el directorio 'assets' en la raíz del proyecto
            File file = new File("assets" + filename);
            if (file.exists()) {
                return ImageIO.read(file);
            }

            // Intentar desde src/assets
            file = new File("src/assets" + filename);
            if (file.exists()) {
                return ImageIO.read(file);
            }

            // Intentar desde el classpath
            return ImageIO.read(getClass().getResourceAsStream(filename));

        } catch (IOException e) {
            System.err.println("Error IO cargando " + filename + ": " + e.getMessage());
            return null;
        } catch (Exception e) {
            System.err.println("Error cargando " + filename + ": " + e.getMessage());
            return null;
        }
    }

    private Color getCharacterColor(CharacterType type) {
        switch (type) {
            case MARIO: return Color.RED;
            case PEPPA: return Color.PINK;
            case HOMERO: return Color.YELLOW;
            case JOHN_WICK: return Color.BLACK;
            case EMPANADA: return new Color(139, 69, 19);
            default: return Color.BLUE;
        }
    }

    private BufferedImage createSimplePlaceholder(String text, Color color) {
        BufferedImage img = new BufferedImage(100, 120, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();

        // Fondo
        g2d.setColor(color);
        g2d.fillRect(0, 0, 100, 120);

        // Texto
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 12));

        // Centrar texto
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        int x = (100 - textWidth) / 2;
        int y = 60;

        g2d.drawString(text, x, y);

        g2d.dispose();
        return img;
    }

    public BufferedImage[] getCharacterFrames(CharacterType type) {
        BufferedImage[] frames = characterAnimations.get(type);
        if (frames == null || frames.length < 2) {
            // Crear frames de emergencia
            frames = new BufferedImage[] {
                    createSimplePlaceholder(type.getName(), getCharacterColor(type)),
                    createSimplePlaceholder(type.getName(), getCharacterColor(type).brighter())
            };
            characterAnimations.put(type, frames);
        }
        return frames;
    }

    public BufferedImage getCharacterTitleImage(CharacterType type) {
        // Para el menú, usar el primer frame más grande
        BufferedImage[] frames = getCharacterFrames(type);
        if (frames[0] != null) {
            // Escalar a 200x200 para el menú
            BufferedImage scaled = new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = scaled.createGraphics();
            g2d.drawImage(frames[0], 0, 0, 200, 200, null);
            g2d.dispose();
            return scaled;
        }
        return createSimplePlaceholder(type.getName(), getCharacterColor(type));
    }
}