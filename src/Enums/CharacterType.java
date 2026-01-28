package Enums;

public enum CharacterType {
    MARIO("Mario", "/assets/characters/mario_1.png", "/assets/characters/mario_2.png"),
    PEPPA("Peppa Pig", "/assets/characters/peppa_1.png", "/assets/characters/peppa_2.png"),
    HOMERO("Homero", "/assets/characters/homero_1.png", "/assets/characters/homero_2.png"),
    JOHN_WICK("John Wick", "/assets/characters/john_1.png", "/assets/characters/john_2.png"),
    EMPANADA("Empanada Gaucha", "/assets/characters/empanada_1.png", "/assets/characters/empanada_2.png");

    private final String name;
    private final String frame1Path;
    private final String frame2Path;

    CharacterType(String name, String frame1Path, String frame2Path) {
        this.name = name;
        this.frame1Path = frame1Path;
        this.frame2Path = frame2Path;
    }

    public String getName() {
        return name;
    }

    public String getFrame1Path() {
        return frame1Path;
    }

    public String getFrame2Path() {
        return frame2Path;
    }

    public static CharacterType getDefault() {
        return MARIO;
    }
}