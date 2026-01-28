package Clases;

import Enums.CharacterType;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.List;

public class Player {
    private Rectangle rect;
    private int vx, vy;
    private boolean onGround;
    private boolean facingRight;
    private boolean jumped, doubleJumped;
    private final int PLAYER_SPEED = 8;
    private final int JUMP_VELOCITY = -20;
    private final int GRAVITY = 1;
    private final int GROUND_Y;

    // Variables para animación
    private boolean isMoving;
    private int animationFrame;
    private final int ANIMATION_DELAY = 10;
    private int animationCounter;
    private boolean wasMovingLastFrame;
    private CharacterType currentCharacter;
    private BufferedImage[] currentFrames;

    // Variables para invencibilidad
    private boolean isInvulnerable;
    private int invulnerabilityTimer;
    private final int INVULNERABILITY_TIME = 90; // ~1.5 segundos a 60 FPS

    public Player(int x, int groundY, CharacterType character) {
        this.GROUND_Y = groundY;
        rect = new Rectangle(x, groundY - 130, 100, 120);
        vx = vy = 0;
        onGround = true;
        facingRight = true;
        isMoving = false;
        animationFrame = 0;
        animationCounter = 0;
        wasMovingLastFrame = false;
        this.currentCharacter = character;
        this.isInvulnerable = false;
        this.invulnerabilityTimer = 0;
    }

    public void update(List<Rectangle> platforms) {
        // Guardar posición anterior para detección de colisiones
        int oldX = rect.x;
        int oldY = rect.y;

        // Movimiento horizontal
        rect.x += vx;

        // Aplicar gravedad
        vy += GRAVITY;
        rect.y += vy;

        boolean currentlyMoving = Math.abs(vx) > 0.1f;

        // Lógica de animación mejorada
        if (currentlyMoving != wasMovingLastFrame) {
            animationFrame = 0;
            animationCounter = 0;
        }
        isMoving = currentlyMoving;
        wasMovingLastFrame = currentlyMoving;

        if (isMoving) {
            animationCounter++;
            if (animationCounter >= ANIMATION_DELAY) {
                animationCounter = 0;
                animationFrame = (animationFrame + 1) % 2;
            }
        } else {
            animationFrame = 0;
        }

        // Actualizar invencibilidad
        if (isInvulnerable) {
            invulnerabilityTimer--;
            if (invulnerabilityTimer <= 0) {
                isInvulnerable = false;
            }
        }

        // Verificar colisión con el suelo
        onGround = false;
        if (rect.y >= GROUND_Y - rect.height) {
            rect.y = GROUND_Y - rect.height;
            onGround = true;
            vy = 0;
            jumped = doubleJumped = false;
        }

        // Verificar colisión con plataformas (árboles y castillos)
        for (Rectangle platform : platforms) {
            if (rect.intersects(platform)) {
                // Si está cayendo y la parte superior del jugador está por encima de la plataforma
                if (oldY + rect.height <= platform.y && vy > 0) {
                    rect.y = platform.y - rect.height;
                    onGround = true;
                    vy = 0;
                    jumped = doubleJumped = false;
                }
                // Si choca lateralmente, retroceder
                else if (oldX + rect.width <= platform.x || oldX >= platform.x + platform.width) {
                    rect.x = oldX;
                }
            }
        }

        // Limitar al jugador dentro del mundo
        if (rect.x < 0) rect.x = 0;
        if (rect.x > 5000 - rect.width) rect.x = 5000 - rect.width;
    }

    public void moveLeft() {
        vx = -PLAYER_SPEED;
        facingRight = false;
    }

    public void moveRight() {
        vx = PLAYER_SPEED;
        facingRight = true;
    }

    public void stop() {
        vx = 0;
    }

    public void jump() {
        if (onGround) {
            vy = JUMP_VELOCITY;
            onGround = false;
            jumped = true;
        } else if (jumped && !doubleJumped) {
            vy = JUMP_VELOCITY;
            doubleJumped = true;
        }
    }

    // Método para recibir daño
    public boolean takeDamage() {
        if (!isInvulnerable) {
            isInvulnerable = true;
            invulnerabilityTimer = INVULNERABILITY_TIME;
            return true; // Jugador recibió daño
        }
        return false; // Jugador no recibió daño (estaba invulnerable)
    }

    // Getters
    public Rectangle getRect() { return rect; }
    public boolean isFacingRight() { return facingRight; }
    public void setFacingRight(boolean facingRight) { this.facingRight = facingRight; }
    public boolean isMoving() { return isMoving; }
    public int getAnimationFrame() { return animationFrame; }
    public int getAnimationDelay() { return ANIMATION_DELAY; }
    public CharacterType getCurrentCharacter() { return currentCharacter; }

    // Getters para invencibilidad
    public boolean isInvulnerable() { return isInvulnerable; }
    public int getInvulnerabilityTimer() { return invulnerabilityTimer; }

    public BufferedImage getCurrentFrame() {
        if (currentFrames == null || currentFrames.length < 2) {
            return null;
        }

        // Si el jugador es invulnerable y el temporizador está en un estado par, no mostrar frame
        // Esto crea el efecto de parpadeo
        if (isInvulnerable && (invulnerabilityTimer / 10) % 2 == 0) {
            return null;
        }

        if (isMoving) {
            return currentFrames[animationFrame];
        } else {
            return currentFrames[0];
        }
    }

    public void setCharacterFrames(BufferedImage[] frames) {
        this.currentFrames = frames;
    }

    public boolean isOnGround() { return onGround; }
    public int getVx() { return vx; }
    public int getVy() { return vy; }
}