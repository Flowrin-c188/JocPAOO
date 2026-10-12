package PaooGame.PlayWindow;

import PaooGame.Graphics.ImageLoader;
import PaooGame.MenuWindow.Database;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Map3 extends MapBase {

    private String playerName;

    private int doorCol = 53;
    private int doorRow = 42;

    private int chestCol;
    private int chestRow;

    private boolean chestRevealed = false;
    private boolean hammerPickedUp = false;

    private BufferedImage imgDoor;
    private BufferedImage imgChest;
    private BufferedImage imgHammer;

    private int sparkleTimer = 0;
    private static final int SPARKLE_DURATION = 40;

    private List<SpiderEnemy> spiders;
    private static final int SPIDER_COUNT = 12;
    private static final int SPIDER_DETECT_RANGE = 6;
    private int spidersKilled = 0;

    private Skelet skelet;
    private int bossTouchTimer = 0;
    private static final int BOSS_DAMAGE_DELAY = 8;
    private long lastBossHitTime = 0;
    private static final long BOSS_HIT_DELAY = 1000;

    private Random random = new Random();


    public Map3(String playerName) {
        super("/maps/map_level3");

        this.playerName = playerName;

        loadObjects();

        spiders = new ArrayList<>();

        hammerPickedUp = Database.getHammerPicked(this.playerName);
        spidersKilled = Database.getSpidersKilled(this.playerName);

        spawnRandomChest();
        spawnSpiders();
        spawnSkelet();

        System.out.println("[Map3] Harta 3 incarcata cu "
                + spiders.size() + " paianjeni.");
    }

    private void loadObjects() {
        BufferedImage doorSheet = ImageLoader.LoadImage("/textures/Door.png");

        if (doorSheet != null) {
            imgDoor = doorSheet.getSubimage(0, 0, 32, 32);
        } else {
            System.err.println("[Map3] Door.png nu a fost gasit!");
        }

        BufferedImage toolSheet = ImageLoader.LoadImage("/textures/sprite_unelte1.png");

        if (toolSheet != null) {
            imgChest = toolSheet.getSubimage(2 * 32, 0, 32, 32);
            imgHammer = toolSheet.getSubimage(3 * 32, 0, 32, 32);
        } else {
            System.err.println("[Map3] sprite_unelte1.png nu a fost gasit!");
        }
    }

    private void spawnRandomChest() {
        do {
            chestCol = random.nextInt(MAP_COLS);
            chestRow = random.nextInt(MAP_ROWS);
        } while (
                isSolid(chestCol, chestRow)
                        || (chestCol == doorCol && chestRow == doorRow)
        );

        System.out.println("[Map3] Chest spawnat la col="
                + chestCol + ", row=" + chestRow);
    }

    @Override
    public void stepOnTile(int col, int row) {
        super.stepOnTile(col, row);

        if (col == chestCol && row == chestRow) {
            chestRevealed = true;
        }
    }

    public boolean tryOpenChest(int playerCol, int playerRow) {
        if (hammerPickedUp) return false;

        int dc = Math.abs(playerCol - chestCol);
        int dr = Math.abs(playerRow - chestRow);

        if (chestRevealed && dc + dr <= 1) {
            hammerPickedUp = true;
            Database.saveHammerPicked(playerName, true);
            sparkleTimer = SPARKLE_DURATION;
            return true;
        }

        return false;
    }

    public boolean isHammerPickedUp() {
        return hammerPickedUp;
    }

    @Override
    public void tickSparkle() {
        if (sparkleTimer > 0) {
            sparkleTimer--;
        }
    }

    public int getChestCol() {
        return chestCol;
    }

    public int getChestRow() {
        return chestRow;
    }

    // SPIDERS
    private void spawnSpiders() {
        int spidersToSpawn = SPIDER_COUNT - spidersKilled;

        for (int i = 0; i < spidersToSpawn; i++) {
            spawnRandomSpider();
        }
    }

    private void spawnRandomSpider() {
        int col;
        int row;

        do {
            col = random.nextInt(MAP_COLS);
            row = random.nextInt(MAP_ROWS);
        } while (!isFreeForEnemy(col, row));

        spiders.add(new SpiderEnemy(col, row, this));

        System.out.println("[Map3] Paianjen spawnat la col="
                + col + ", row=" + row);
    }

    public void tickSpiders(int playerCol, int playerRow) {
        for (SpiderEnemy spider : spiders) {
            int dc = Math.abs(spider.getCol() - playerCol);
            int dr = Math.abs(spider.getRow() - playerRow);

            if (dc + dr <= SPIDER_DETECT_RANGE) {
                spider.tickFollowPlayer(playerCol, playerRow);
            } else {
                spider.tick();
            }
        }
    }

    public boolean tryAttackSpider(int playerCol, int playerRow) {
        if (!hammerPickedUp) return false;

        for (int i = spiders.size() - 1; i >= 0; i--) {
            SpiderEnemy spider = spiders.get(i);

            int dc = Math.abs(spider.getCol() - playerCol);
            int dr = Math.abs(spider.getRow() - playerRow);

            if (dc <= 1 && dr <= 1) {
                spiders.remove(i);
                spidersKilled++;
                Database.saveSpidersKilled(playerName, spidersKilled);

                System.out.println("[Map3] Paianjen eliminat cu ciocanul!");
                return true;
            }
        }

        return false;
    }

    public List<SpiderEnemy> getSpiders() {
        return spiders;
    }

    public int getSpiderCount() {
        return spiders.size();
    }

    // BOSS
    private void spawnSkelet() {
        int col = 47;
        int row = 38;

        if (!isSolid(col, row)) {
            skelet = new Skelet(col, row, this);
            System.out.println("[Map3] Skelet spawnat la col=" + col + ", row=" + row);
        } else {
            System.out.println("[Map3] Nu pot spawna Skelet aici.");
        }

        if (skelet != null) {
            skelet.setHealth(Database.getBossHp(playerName));
        }
    }

    public void tickBoss(Player player) {
        if (skelet == null) return;

        boolean playerInBossRoom =
                player.getCol() >= 39 && player.getCol() <= 53 &&
                        player.getRow() >= 31 && player.getRow() <= 43;

        if (playerInBossRoom) {
            skelet.tickFollowPlayer(player.getCol(), player.getRow());
        } else {
            skelet.tick();
        }
    }

    public boolean tryAttackBoss(int playerCol, int playerRow) {
        if (!hammerPickedUp) return false;
        if (skelet == null) return false;

        long now = System.currentTimeMillis();

        if (now - lastBossHitTime < BOSS_HIT_DELAY) {
            return false;
        }

        boolean nearBoss =
                playerCol >= skelet.getCol() - 1 &&
                        playerCol <= skelet.getCol() + 2 &&
                        playerRow >= skelet.getRow() - 1 &&
                        playerRow <= skelet.getRow() + 2;

        if (nearBoss) {
            lastBossHitTime = now;

            skelet.takeDamage(1);
            Database.saveBossHp(playerName, skelet.getHealth());

            if (skelet.isDead()) {
                skelet = null;
                System.out.println("[Map3] Boss omorat!");
            }

            return true;
        }

        return false;
    }

    public void checkEnemyCollisions(Player player) {
        checkSpiderCollisions(player);
        checkBossCollision(player);
    }

    private void checkSpiderCollisions(Player player) {
        for (SpiderEnemy spider : spiders) {
            if (spider.checkCollision(player.getX(), player.getY())) {
                player.takeDamage(1);
                return;
            }
        }
    }

    private void checkBossCollision(Player player) {
        if (skelet != null && skelet.canAttackPlayer(player)) {
            bossTouchTimer++;

            if (bossTouchTimer >= BOSS_DAMAGE_DELAY) {
                player.takeDamage(2);
                bossTouchTimer = 0;
            }
        } else {
            bossTouchTimer = 0;
        }
    }


    public boolean tryOpenDoor(int playerCol, int playerRow, boolean hasKey) {
        if (!hasKey) return false;

        if (!spiders.isEmpty()) {
            return false;
        }

        int dc = Math.abs(playerCol - doorCol);
        int dr = Math.abs(playerRow - doorRow);

        return dc + dr <= 1;
    }


    private boolean isFreeForEnemy(int col, int row) {
        if (isSolid(col, row)) return false;
        if (col == chestCol && row == chestRow) return false;
        if (col == doorCol && row == doorRow) return false;

        for (SpiderEnemy spider : spiders) {
            if (spider.getCol() == col && spider.getRow() == row) {
                return false;
            }
        }

        if (skelet != null &&
                skelet.getCol() == col &&
                skelet.getRow() == row) {
            return false;
        }

        return true;
    }

    private boolean inView(int sx, int sy, int viewW, int viewH) {
        return sx + TILE_SIZE >= 0 &&
                sx < viewW &&
                sy + TILE_SIZE >= 0 &&
                sy < viewH;
    }

    @Override
    public void drawMap(Graphics g, int camX, int camY, int viewW, int viewH) {
        drawBaseTiles(g, camX, camY, viewW, viewH);

        drawChest(g, camX, camY, viewW, viewH);
        drawHammerSparkle(g, camX, camY, viewW, viewH);
        drawSpiders(g, camX, camY);
        drawBoss(g, camX, camY);

        drawGrassLayer(g, camX, camY, viewW, viewH);

        drawDoor(g, camX, camY, viewW, viewH);
    }

    private void drawChest(Graphics g, int camX, int camY, int viewW, int viewH) {
        if (!chestRevealed || hammerPickedUp || imgChest == null) return;

        int sx = chestCol * TILE_SIZE - camX;
        int sy = chestRow * TILE_SIZE - camY;

        if (inView(sx, sy, viewW, viewH)) {
            g.drawImage(imgChest, sx, sy, TILE_SIZE, TILE_SIZE, null);
            drawChestPrompt(g, sx, sy);
        }
    }

    private void drawHammerSparkle(Graphics g, int camX, int camY, int viewW, int viewH) {
        if (!hammerPickedUp || sparkleTimer <= 0 || imgHammer == null) return;

        int sx = chestCol * TILE_SIZE - camX;
        int sy = chestRow * TILE_SIZE - camY;

        if (inView(sx, sy, viewW, viewH)) {
            float prog = 1f - (sparkleTimer / (float) SPARKLE_DURATION);
            int offsetY = (int) (prog * 30);
            int alpha = sparkleTimer > 10 ? 255 : (int) (sparkleTimer * 25.5f);

            Graphics2D g2 = (Graphics2D) g;
            g2.setComposite(AlphaComposite.getInstance(
                    AlphaComposite.SRC_OVER,
                    alpha / 255f
            ));

            g2.drawImage(imgHammer, sx, sy - offsetY, TILE_SIZE, TILE_SIZE, null);

            g2.setComposite(AlphaComposite.getInstance(
                    AlphaComposite.SRC_OVER,
                    1f
            ));
        }
    }

    private void drawSpiders(Graphics g, int camX, int camY) {
        for (SpiderEnemy spider : spiders) {
            spider.draw(g, camX, camY);
        }
    }

    private void drawBoss(Graphics g, int camX, int camY) {
        if (skelet != null) {
            skelet.draw(g, camX, camY);
        }
    }

    private void drawDoor(Graphics g, int camX, int camY, int viewW, int viewH) {
        if (imgDoor == null) return;

        int sx = doorCol * TILE_SIZE - camX;
        int sy = doorRow * TILE_SIZE - camY;

        if (inView(sx, sy, viewW, viewH)) {
            g.drawImage(imgDoor, sx, sy, TILE_SIZE, TILE_SIZE, null);
        }
    }

    private void drawChestPrompt(Graphics g, int sx, int sy) {
        Graphics2D g2 = (Graphics2D) g;

        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        int bx = sx + TILE_SIZE / 2 - 24;
        int by = sy - 22;

        g2.setColor(new Color(0, 0, 0, 170));
        g2.fillRoundRect(bx, by, 48, 18, 8, 8);

        g2.setColor(new Color(255, 215, 0));
        g2.setFont(new Font("Arial", Font.BOLD, 10));
        g2.drawString("ENTER", bx + 7, by + 12);
    }
}