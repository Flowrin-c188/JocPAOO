package PaooGame.PlayWindow;

import PaooGame.Graphics.ImageLoader;

import java.awt.*;
import java.awt.image.BufferedImage;

public class SpiderEnemy {

    private static final int DIR_DOWN  = 0;
    private static final int DIR_UP    = 1;
    private static final int DIR_LEFT  = 2;
    private static final int DIR_RIGHT = 3;

    private static final int FRAME_COUNT = 4;
    private static final int ANIM_DELAY  = 5;
    private static final int SPEED       = 10;

    private BufferedImage[][] sprites;

    private float x;
    private float y;

    private float targetX;
    private float targetY; // NOU

    private int direction;
    private int animFrame;
    private int animTick;

    private final MapBase map;

    public SpiderEnemy(int startCol, int startRow, MapBase map) {
        this.x = startCol * MapBase.TILE_SIZE;
        this.y = startRow * MapBase.TILE_SIZE;

        this.targetX = this.x;
        this.targetY = this.y; // NOU

        this.direction = DIR_DOWN;
        this.animFrame = 0;
        this.animTick = 0;
        this.map = map;

        loadSprites();
    }

    private void loadSprites() {
        BufferedImage sheet = ImageLoader.LoadImage("/textures/spider_32_1.png");

        if (sheet == null) {
            System.err.println("[SpiderEnemy] spider_32_1.png nu a fost gasit!");
            return;
        }

        int ts = 32;
        sprites = new BufferedImage[4][FRAME_COUNT];

        int[] rowMap = new int[4];
        rowMap[DIR_DOWN] = 0;
        rowMap[DIR_UP] = 1;
        rowMap[DIR_LEFT] = 2;
        rowMap[DIR_RIGHT] = 3;

        for (int dir = 0; dir < 4; dir++) {
            for (int f = 0; f < FRAME_COUNT; f++) {
                sprites[dir][f] = sheet.getSubimage(
                        f * ts,
                        rowMap[dir] * ts,
                        ts,
                        ts
                );
            }
        }
    }

    public void tick() {

        updateAnimation();

        float dx = targetX - x;
        float dy = targetY - y;

        // miscare pe X
        if (Math.abs(dx) > SPEED) {

            x += (dx > 0 ? 1 : -1) * SPEED;

        } else {

            x = targetX;
        }

        // miscare pe Y
        if (Math.abs(dy) > SPEED) {

            y += (dy > 0 ? 1 : -1) * SPEED;

        } else {

            y = targetY;
        }

        // ===== SPARGE IARBA CONTINUU =====
        int nextCol = (int)((targetX + (float) MapBase.TILE_SIZE / 2) / MapBase.TILE_SIZE);
        int nextRow = (int)((targetY + (float) MapBase.TILE_SIZE / 2) / MapBase.TILE_SIZE);

        map.stepOnTile(nextCol, nextRow);

        // ===== TILE NOU =====
        if (x == targetX && y == targetY) {

            decideNextMove();
        }
    }

    public void tickFollowPlayer(int playerCol, int playerRow) {

        updateAnimation();

        float dx = targetX - x;
        float dy = targetY - y;

        // miscare smooth pe X
        if (Math.abs(dx) > SPEED) {
            x += (dx > 0 ? 1 : -1) * SPEED;
        } else {
            x = targetX;
        }

        // miscare smooth pe Y
        if (Math.abs(dy) > SPEED) {
            y += (dy > 0 ? 1 : -1) * SPEED;
        } else {
            y = targetY;
        }

        // sparge iarba pe tile-ul spre care merge
        int grassCol = (int)((targetX + (float) MapBase.TILE_SIZE / 2) / MapBase.TILE_SIZE);
        int grassRow = (int)((targetY + (float) MapBase.TILE_SIZE / 2) / MapBase.TILE_SIZE);

        map.stepOnTile(grassCol, grassRow);

        // daca NU a ajuns pe tile inca
        if (x != targetX || y != targetY) {
            return;
        }

        // =========================================
        // AI FOLLOW PLAYER
        // =========================================

        int col = getCol();
        int row = getRow();

        int[][] directions = {

                { 0, -1 }, // UP
                { 0, 1 },  // DOWN
                { -1, 0 }, // LEFT
                { 1, 0 }   // RIGHT
        };

        int bestCol = col;
        int bestRow = row;

        double bestDist = Double.MAX_VALUE;

        for (int[] ints : directions) {

            int testCol = col + ints[0];
            int testRow = row + ints[1];

            // zid
            if (map.isSolid(testCol, testRow)) {
                continue;
            }

            // distanta pana la player
            double dist = Math.sqrt(
                    Math.pow(playerCol - testCol, 2) +
                            Math.pow(playerRow - testRow, 2)
            );

            // cel mai bun tile
            if (dist < bestDist) {

                bestDist = dist;

                bestCol = testCol;
                bestRow = testRow;

                // directie animatie
                if (ints[0] == -1) {
                    direction = DIR_RIGHT;
                } else if (ints[0] == 1) {
                    direction = DIR_LEFT;
                } else if (ints[1] == -1) {
                    direction = DIR_UP;
                } else {
                    direction = DIR_DOWN;
                }
            }
        }

        // aplicare miscare
        targetX = bestCol * MapBase.TILE_SIZE;
        targetY = bestRow * MapBase.TILE_SIZE;
    }

    private void updateAnimation() {
        animTick++;

        if (animTick >= ANIM_DELAY) {
            animTick = 0;
            animFrame = (animFrame + 1) % FRAME_COUNT;
        }
    }

    private void decideNextMove() {

        int col = getCol();
        int row = getRow();

        // IMPORTANT:
        // indexurile trebuie sa corespunda cu:
        //
        // DIR_DOWN  = 0
        // DIR_UP    = 1
        // DIR_LEFT  = 2
        // DIR_RIGHT = 3
        //
        int[][] dirs = {

                { 0, 1 },   // DOWN
                { 0, -1 },  // UP
                { 1, 0 },  // LEFT
                { -1, 0 }    // RIGHT
        };

        // incearca directia curenta prima
        for (int i = 0; i < 4; i++) {

            int dir = (direction + i) % 4;

            int nextCol = col + dirs[dir][0];
            int nextRow = row + dirs[dir][1];

            if (!map.isSolid(nextCol, nextRow)) {

                direction = dir;

                targetX = nextCol * MapBase.TILE_SIZE;
                targetY = nextRow * MapBase.TILE_SIZE;

                return;
            }
        }
    }

    public boolean checkCollision(float playerX, float playerY) {
        float ts = MapBase.TILE_SIZE;
        float margin = ts * 0.6f;

        return Math.abs(x - playerX) < margin &&
                Math.abs(y - playerY) < margin;
    }

    public void draw(Graphics g, int camX, int camY) {
        if (sprites == null) return;

        int screenX = (int)(x - camX);
        int screenY = (int)(y - camY);

        if (screenX + MapBase.TILE_SIZE < 0 || screenX > 800) return;
        if (screenY + MapBase.TILE_SIZE < 0 || screenY > 600) return;

        g.drawImage(
                sprites[direction][animFrame],
                screenX,
                screenY,
                MapBase.TILE_SIZE,
                MapBase.TILE_SIZE,
                null
        );
    }

    public int getCol() {
        return (int)(x / MapBase.TILE_SIZE);
    }

    public int getRow() {
        return (int)(y / MapBase.TILE_SIZE);
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }
}