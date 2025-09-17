package Clases;

import Enums.CharacterType;

import java.awt.Rectangle;
import java.awt.image.BufferedImage; // Importación añadida

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
    private boolean wasMovingLastFrame;
    private CharacterType currentCharacter;
    private BufferedImage[] currentFrames; // Usa BufferedImage

    public Player(int x, int groundY, CharacterType character) {
        this.GROUND_Y = groundY;
        rect = new Rectangle(x, groundY - 130, 130, 140);
        vx = vy = 0;
        onGround = true;
        facingRight = true;
        isMoving = false;
        animationFrame = 0;
        wasMovingLastFrame = false;
        this.currentCharacter = character;
    }

    public void update() {
        rect.x += vx;
        boolean currentlyMoving = Math.abs(vx) > 0.1f;

        if (currentlyMoving != wasMovingLastFrame) {
            animationFrame = 0;
        }
        isMoving = currentlyMoving;
        wasMovingLastFrame = currentlyMoving;

        if (isMoving) {
            animationFrame = (animationFrame + 1) % (ANIMATION_DELAY * 2);
        }

        if (!onGround) {
            vy += GRAVITY;
            rect.y += vy;

            if (rect.y >= GROUND_Y - rect.height) {
                rect.y = GROUND_Y - rect.height;
                onGround = true;
                vy = 0;
                jumped = doubleJumped = false;
            }
        }
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

    // Getters
    public Rectangle getRect() { return rect; }
    public boolean isFacingRight() { return facingRight; }
    public void setFacingRight(boolean facingRight) { this.facingRight = facingRight; }
    public boolean isMoving() { return isMoving; }
    public int getAnimationFrame() { return animationFrame; }
    public int getAnimationDelay() { return ANIMATION_DELAY; }
    public CharacterType getCurrentCharacter() { return currentCharacter; }

    public void setCharacterFrames(BufferedImage[] frames) {
        this.currentFrames = frames;
    }
}