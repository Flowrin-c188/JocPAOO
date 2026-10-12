package PaooGame.PlayWindow;

import PaooGame.Graphics.ImageLoader;
import PaooGame.Graphics.SpriteSheet;
import PaooGame.MenuWindow.Database;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

public class Player {

    public static final int DIR_DOWN  = 0;
    public static final int DIR_UP    = 1;
    public static final int DIR_LEFT  = 2;
    public static final int DIR_RIGHT = 3;

    private static final int FRAME_COUNT = 4;
    private static final long MOVE_DELAY = 120;

    private BufferedImage[][] sprites;

    private int direction;
    private int animFrame;

    private long lastMoveTime = 0;
    private int lastMoveDirection = -1;

    private int col;
    private int row;

    private MapBase currentMap;
    private String playerName;

    private int health = 5;

    private boolean invulnerable = false;
    private int invulnerableTimer = 0;

    private int damageFlashTimer = 0;

    private static final int INVULNERABLE_DURATION = 60;
    private static final int DAMAGE_FLASH_DURATION = 10;

    public Player(int startCol, int startRow, MapBase map, String playerName) {
        this.col = startCol;
        this.row = startRow;
        this.direction = DIR_DOWN;
        this.animFrame = 0;
        this.currentMap = map;
        this.playerName = playerName;

        if (playerName != null && !playerName.isEmpty()) {
            this.health = Database.getPlayerHp(playerName);
        }

        loadSprites();
        currentMap.stepOnTile(col, row);
    }
    private void loadSprites() {
        BufferedImage sheet = ImageLoader.LoadImage("/textures/Personaj.png");
        SpriteSheet ss = new SpriteSheet(sheet);

        sprites = new BufferedImage[4][FRAME_COUNT];

        for (int f = 0; f < FRAME_COUNT; f++) {
            sprites[DIR_DOWN][f] = ss.crop(f, 0);
            sprites[DIR_LEFT][f] = ss.crop(f, 2);
            sprites[DIR_RIGHT][f] = flipHorizontal(sprites[DIR_LEFT][f]);
        }

        sprites[DIR_UP][0] = ss.crop(0, 1);
        sprites[DIR_UP][1] = ss.crop(1, 1);
        sprites[DIR_UP][2] = sprites[DIR_UP][0];
        sprites[DIR_UP][3] = sprites[DIR_UP][1];
    }

    private BufferedImage flipHorizontal(BufferedImage img) {
        AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
        tx.translate(-img.getWidth(), 0);

        AffineTransformOp op = new AffineTransformOp(
                tx,
                AffineTransformOp.TYPE_NEAREST_NEIGHBOR
        );

        return op.filter(img, null);
    }

    public void moveUp() {
        direction = DIR_UP;
        if (!canMove(DIR_UP)) return;

        if (!currentMap.isSolid(col, row - 1)) {
            row--;
            animFrame = (animFrame + 1) % 2;

            afterMove();
        }
    }

    public void moveDown() {
        direction = DIR_DOWN;
        if (!canMove(DIR_DOWN)) return;

        if (!currentMap.isSolid(col, row + 1)) {
            row++;
            animFrame = 0;

            afterMove();
        }
    }

    public void moveLeft() {
        direction = DIR_LEFT;
        if (!canMove(DIR_LEFT)) return;

        if (!currentMap.isSolid(col - 1, row)) {
            col--;
            nextAnimFrame();

            afterMove();
        }
    }

    public void moveRight() {
        direction = DIR_RIGHT;
        if (!canMove(DIR_RIGHT)) return;

        if (!currentMap.isSolid(col + 1, row)) {
            col++;
            nextAnimFrame();

            afterMove();
        }
    }

    private boolean canMove(int newDirection) {
        long now = System.currentTimeMillis();

        if (newDirection != lastMoveDirection) {
            lastMoveDirection = newDirection;
            lastMoveTime = now;
            return true;
        }

        if (now - lastMoveTime < MOVE_DELAY) {
            return false;
        }

        lastMoveTime = now;
        return true;
    }

    private void nextAnimFrame() {
        animFrame = (animFrame + 1) % FRAME_COUNT;
    }

    private void afterMove() {
        currentMap.stepOnTile(col, row);
        saveCurrentPosition();
    }

    public void stopMoving() {
        animFrame = 0;
    }

    public void stopAnimTimer() {
        // momentan nu folosesti timer separat pentru player
    }

    // =========================
    // HEALTH / DAMAGE
    // =========================
    public void takeDamage(int damage) {
        if (invulnerable) {
            return;
        }

        health -= damage;

        if (playerName != null && !playerName.isEmpty()) {
            Database.savePlayerHp(playerName, health);
        }

        damageFlashTimer = DAMAGE_FLASH_DURATION;
        invulnerable = true;
        invulnerableTimer = INVULNERABLE_DURATION;

        System.out.println("[Player] Damage primit! HP = " + health);

        if (health <= 0) {
            System.out.println("[Player] GAME OVER");
        }
    }

    public void tickHealth() {
        if (invulnerableTimer > 0) {
            invulnerableTimer--;

            if (invulnerableTimer <= 0) {
                invulnerable = false;
            }
        }

        if (damageFlashTimer > 0) {
            damageFlashTimer--;
        }
    }

    public void resetHealth() {
        health = 5;
        invulnerable = false;
        invulnerableTimer = 0;
        damageFlashTimer = 0;

        if (playerName != null && !playerName.isEmpty()) {
            Database.savePlayerHp(playerName, health);
        }
    }

    public boolean isDead() {
        return health <= 0;
    }

    public boolean isInvulnerable() {
        return invulnerable;
    }

    public int getHealth() {
        return health;
    }

    // CAMERA
    public int calcCameraX(int viewWidth) {
        int mapPxW = MapBase.MAP_COLS * MapBase.TILE_SIZE;
        int cx = col * MapBase.TILE_SIZE + MapBase.TILE_SIZE / 2 - viewWidth / 2;

        return Math.max(0, Math.min(mapPxW - viewWidth, cx));
    }

    public int calcCameraY(int viewHeight) {
        int mapPxH = MapBase.MAP_ROWS * MapBase.TILE_SIZE;
        int cy = row * MapBase.TILE_SIZE + MapBase.TILE_SIZE / 2 - viewHeight / 2;

        return Math.max(0, Math.min(mapPxH - viewHeight, cy));
    }

    public void draw(Graphics g, int camX, int camY) {
        int screenX = col * MapBase.TILE_SIZE - camX;
        int screenY = row * MapBase.TILE_SIZE - camY;

        int frameToDraw = animFrame;

        if (direction == DIR_UP) {
            frameToDraw = animFrame % 2;
        }

        Graphics2D g2 = (Graphics2D) g;

        applyInvulnerabilityEffect(g2);

        g.drawImage(
                sprites[direction][frameToDraw],
                screenX,
                screenY,
                MapBase.TILE_SIZE,
                MapBase.TILE_SIZE,
                null
        );

        resetTransparency(g2);
    }

    private void applyInvulnerabilityEffect(Graphics2D g2) {
        if (!invulnerable) return;

        float alpha = 0.5f;

        if ((invulnerableTimer / 5) % 2 == 0) {
            alpha = 0.15f;
        }

        g2.setComposite(
                AlphaComposite.getInstance(
                        AlphaComposite.SRC_OVER,
                        alpha
                )
        );
    }

    private void resetTransparency(Graphics2D g2) {
        g2.setComposite(
                AlphaComposite.getInstance(
                        AlphaComposite.SRC_OVER,
                        1f
                )
        );
    }

    private void saveCurrentPosition() {
        if (playerName != null && !playerName.isEmpty()) {
            Database.savePlayerPosition(playerName, col, row);
        }
    }


    // GETTERS / SETTERS

    public int getCol() {
        return col;
    }

    public int getRow() {
        return row;
    }

    public float getX() {
        return col * MapBase.TILE_SIZE;
    }

    public float getY() {
        return row * MapBase.TILE_SIZE;
    }

    public void setCol(int col) {
        this.col = col;
        saveCurrentPosition();
    }

    public void setRow(int row) {
        this.row = row;
        saveCurrentPosition();
    }

    public void setMap(MapBase map) {
        this.currentMap = map;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }
}