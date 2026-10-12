package PaooGame.PlayWindow;

import PaooGame.Graphics.ImageLoader;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;

public class Skelet {

    private static final int DIR_DOWN  = 0;
    private static final int DIR_LEFT  = 1;
    private static final int DIR_RIGHT = 2;
    private static final int DIR_UP    = 3;

    private static final int ANIM_DELAY = 6;
    private static final int SPEED = 5;

    private static final int SPRITE_SIZE = 64;
    private static final int BOSS_SCALE = 2;

    private BufferedImage[][] sprites;

    private float x;
    private float y;

    private float targetX;
    private float targetY;

    private int direction;
    private int animFrame;
    private int animTick;

    private final MapBase map;
    private int health = 3;

    public Skelet(int startCol, int startRow, MapBase map) {
        this.x = startCol * MapBase.TILE_SIZE;
        this.y = startRow * MapBase.TILE_SIZE;

        this.targetX = this.x;
        this.targetY = this.y;

        this.direction = DIR_DOWN;
        this.animFrame = 0;
        this.animTick = 0;

        this.map = map;

        loadSprites();
    }

    private void loadSprites() {
        BufferedImage sheet = ImageLoader.LoadImage("/textures/skeleton_boss_t.png");

        if (sheet == null) {
            System.err.println("[Skelet] skeleton_boss_t.png nu a fost gasit!");
            return;
        }

        sprites = new BufferedImage[4][];

        sprites[DIR_UP] = new BufferedImage[2];
        sprites[DIR_UP][0] = sheet.getSubimage(0 * SPRITE_SIZE, 1 * SPRITE_SIZE, SPRITE_SIZE, SPRITE_SIZE);
        sprites[DIR_UP][1] = sheet.getSubimage(1 * SPRITE_SIZE, 1 * SPRITE_SIZE, SPRITE_SIZE, SPRITE_SIZE);

        sprites[DIR_DOWN] = new BufferedImage[2];
        sprites[DIR_DOWN][0] = sheet.getSubimage(0 * SPRITE_SIZE, 0 * SPRITE_SIZE, SPRITE_SIZE, SPRITE_SIZE);
        sprites[DIR_DOWN][1] = sheet.getSubimage(1 * SPRITE_SIZE, 0 * SPRITE_SIZE, SPRITE_SIZE, SPRITE_SIZE);

        sprites[DIR_LEFT] = new BufferedImage[4];
        sprites[DIR_LEFT][0] = sheet.getSubimage(0 * SPRITE_SIZE, 2 * SPRITE_SIZE, SPRITE_SIZE, SPRITE_SIZE);
        sprites[DIR_LEFT][1] = sheet.getSubimage(1 * SPRITE_SIZE, 2 * SPRITE_SIZE, SPRITE_SIZE, SPRITE_SIZE);
        sprites[DIR_LEFT][2] = sheet.getSubimage(2 * SPRITE_SIZE, 1 * SPRITE_SIZE, SPRITE_SIZE, SPRITE_SIZE);
        sprites[DIR_LEFT][3] = sheet.getSubimage(3 * SPRITE_SIZE, 1 * SPRITE_SIZE, SPRITE_SIZE, SPRITE_SIZE);

        sprites[DIR_RIGHT] = new BufferedImage[sprites[DIR_LEFT].length];

        for (int i = 0; i < sprites[DIR_LEFT].length; i++) {
            BufferedImage leftImg = sprites[DIR_LEFT][i];
            BufferedImage rightImg = new BufferedImage(
                    SPRITE_SIZE,
                    SPRITE_SIZE,
                    BufferedImage.TYPE_INT_ARGB
            );

            Graphics2D g2d = rightImg.createGraphics();
            g2d.drawImage(leftImg, SPRITE_SIZE, 0, -SPRITE_SIZE, SPRITE_SIZE, null);
            g2d.dispose();

            sprites[DIR_RIGHT][i] = rightImg;
        }
    }
    public void takeDamage(int damage) {
        health -= damage;
        System.out.println("[Skelet] HP ramas: " + health);
    }

    public boolean isDead() {
        return health <= 0;
    }

    public int getHealth() {
        return health;
    }
    private void setDirection(int newDirection) {
        if (direction != newDirection) {
            direction = newDirection;
            animFrame = 0;
            animTick = 0;
        }
    }
    public void setHealth(int health) {
        this.health = health;
    }
    public void tick() {
        updateAnimation();
        moveTowardsTarget();

        if (x == targetX && y == targetY) {
            decideNextMoveVertical();
        }
    }

    public void tickFollowPlayer(int playerCol, int playerRow) {
        updateAnimation();
        moveTowardsTarget();

        if (x != targetX || y != targetY) {
            return;
        }

        Point next = findNextStepToPlayer(playerCol, playerRow);

        if (next == null) {
            return;
        }

        int oldCol = getCol();
        int oldRow = getRow();

        if (next.x > oldCol) {
            setDirection(DIR_RIGHT);
        } else if (next.x < oldCol) {
            setDirection(DIR_LEFT);
        } else if (next.y > oldRow) {
            setDirection(DIR_DOWN);
        } else if (next.y < oldRow) {
            setDirection(DIR_UP);
        }

        targetX = next.x * MapBase.TILE_SIZE;
        targetY = next.y * MapBase.TILE_SIZE;
    }
    private boolean bossCanTouchPlayerFrom(int bossCol, int bossRow, int playerCol, int playerRow) {
        int bossLeft = bossCol;
        int bossRight = bossCol + BOSS_SCALE - 1;
        int bossTop = bossRow;
        int bossBottom = bossRow + BOSS_SCALE - 1;

        return playerCol >= bossLeft - 1 &&
                playerCol <= bossRight + 1 &&
                playerRow >= bossTop - 1 &&
                playerRow <= bossBottom + 1;
    }
    private Point findNextStepToPlayer(int playerCol, int playerRow) {
        int startCol = getCol();
        int startRow = getRow();

        Queue<Point> queue = new LinkedList<>();
        boolean[][] visited = new boolean[MapBase.MAP_ROWS][MapBase.MAP_COLS];
        Point[][] parent = new Point[MapBase.MAP_ROWS][MapBase.MAP_COLS];

        Point start = new Point(startCol, startRow);
        queue.add(start);
        visited[startRow][startCol] = true;

        int[][] dirs = {
                {0, -1},
                {0, 1},
                {-1, 0},
                {1, 0}
        };

        Point target = null;

        while (!queue.isEmpty()) {
            Point current = queue.poll();

            int distToPlayer =
                    Math.abs(current.x - playerCol) +
                            Math.abs(current.y - playerRow);

            // boss-ul se opreste langa player, nu peste player
            if (bossCanTouchPlayerFrom(current.x, current.y, playerCol, playerRow)) {
                target = current;
                break;
            }

            for (int[] d : dirs) {
                int nc = current.x + d[0];
                int nr = current.y + d[1];

                if (nc < 0 || nc >= MapBase.MAP_COLS ||
                        nr < 0 || nr >= MapBase.MAP_ROWS) {
                    continue;
                }

                if (visited[nr][nc]) {
                    continue;
                }

                if (!canBossMoveTo(nc, nr)) {
                    continue;
                }

                visited[nr][nc] = true;
                parent[nr][nc] = current;
                queue.add(new Point(nc, nr));
            }
        }

        if (target == null) {
            return null;
        }

        Point step = target;

        while (parent[step.y][step.x] != null &&
                !(parent[step.y][step.x].x == startCol &&
                        parent[step.y][step.x].y == startRow)) {
            step = parent[step.y][step.x];
        }

        return step;
    }

    private void updateAnimation() {
        if (sprites == null || sprites[direction] == null) return;

        if (animFrame >= sprites[direction].length) {
            animFrame = 0;
        }

        animTick++;

        if (animTick >= ANIM_DELAY) {
            animTick = 0;
            animFrame = (animFrame + 1) % sprites[direction].length;
        }
    }

    private void moveTowardsTarget() {
        float dx = targetX - x;
        float dy = targetY - y;

        // alegem animatia in functie de directia de  mers
        if (Math.abs(dx) > Math.abs(dy)) {
            if (dx > 0) {
                setDirection(DIR_RIGHT);
            } else if (dx < 0) {
                setDirection(DIR_LEFT);
            }
        } else {
            if (dy > 0) {
                setDirection(DIR_DOWN);
            } else if (dy < 0) {
                setDirection(DIR_UP);
            }
        }

        if (Math.abs(dx) > SPEED) {
            x += (dx > 0 ? 1 : -1) * SPEED;
        } else {
            x = targetX;
        }

        if (Math.abs(dy) > SPEED) {
            y += (dy > 0 ? 1 : -1) * SPEED;
        } else {
            y = targetY;
        }
    }

    private void decideNextMoveVertical() {
        int col = getCol();
        int row = getRow();

        int nextRow = row + (direction == DIR_DOWN ? 1 : -1);

        if (!canBossMoveTo(col, nextRow)) {
            if (direction == DIR_DOWN) {
                setDirection(DIR_UP);
            } else {
                setDirection(DIR_DOWN);
            }

            nextRow = row + (direction == DIR_DOWN ? 1 : -1);
        }

        if (canBossMoveTo(col, nextRow)) {
            targetX = col * MapBase.TILE_SIZE;
            targetY = nextRow * MapBase.TILE_SIZE;
        }
    }

    private boolean canBossMoveTo(int col, int row) {
        for (int r = row; r < row + BOSS_SCALE; r++) {
            for (int c = col; c < col + BOSS_SCALE; c++) {
                if (map.isSolid(c, r)) {
                    return false;
                }
            }
        }

        return true;
    }
    public boolean canAttackPlayer(Player player) {
        int bossCol = getCol();
        int bossRow = getRow();

        int bossLeft = bossCol;
        int bossRight = bossCol + BOSS_SCALE - 1;
        int bossTop = bossRow;
        int bossBottom = bossRow + BOSS_SCALE - 1;

        int playerCol = player.getCol();
        int playerRow = player.getRow();

        return playerCol >= bossLeft - 1 &&
                playerCol <= bossRight + 1 &&
                playerRow >= bossTop - 1 &&
                playerRow <= bossBottom + 1;
    }

    public void draw(Graphics g, int camX, int camY) {
        if (sprites == null || sprites[direction] == null) return;

        if (animFrame >= sprites[direction].length) {
            animFrame = 0;
        }

        int screenX = (int) (x - camX);
        int screenY = (int) (y - camY);

        int bossSize = MapBase.TILE_SIZE * BOSS_SCALE;

        if (screenX + bossSize < 0 || screenX > 800) return;
        if (screenY + bossSize < 0 || screenY > 600) return;

        g.drawImage(
                sprites[direction][animFrame],
                screenX,
                screenY,
                bossSize,
                bossSize,
                null
        );
    }

    public int getCol() {
        return (int) (x / MapBase.TILE_SIZE);
    }

    public int getRow() {
        return (int) (y / MapBase.TILE_SIZE);
    }

}