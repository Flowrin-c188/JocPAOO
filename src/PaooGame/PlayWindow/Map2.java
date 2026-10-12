package PaooGame.PlayWindow;

import PaooGame.Graphics.ImageLoader;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Map2 extends MapBase {

    private List<SnakeEnemy> enemies;

    // -------- USA NIVEL 3 --------
    private BufferedImage imgDoor;
    private int doorCol = 6;
    private int doorRow = 26;

    // ================================================================
    //  SISTEM CAPCANE


    private enum TrapState { INTACT, FALLING, DONE }

    private static class Trap {
        final int grassCol, grassRow;
        final int enemyCol, enemyRow;

        TrapState state;
        float rockPixelY;
        final float startPixelY;
        final float targetPixelY;

        Trap(int grassCol, int grassRow, int enemyCol, int enemyRow) {
            this.grassCol     = grassCol;
            this.grassRow     = grassRow;
            this.enemyCol     = enemyCol;
            this.enemyRow     = enemyRow;
            this.state        = TrapState.INTACT;
            this.startPixelY  = (grassRow - 1) * (float) MapBase.TILE_SIZE;
            this.targetPixelY = grassRow * (float) MapBase.TILE_SIZE;
            this.rockPixelY   = startPixelY;
        }
    }

    private List<Trap> traps;
    private BufferedImage imgFallingRock;
    private static final float ROCK_FALL_SPEED = 8f;

    // ================================================================
    //  SISTEM DIAMANTE
    // ================================================================

    private static final int DIAMOND_COUNT = 5;

    private int[]     diamondCols;
    private int[]     diamondRows;
    private boolean[] diamondCollected;
    private int       diamondsRemaining;

    /** Imaginea diamantului: linia 0, coloana 0 din sprite_unelte1.png */
    private BufferedImage imgDiamond;

    // animatie plutire
    private int sparkleTick  = 0;
    private int sparkleFrame = 0;
    private static final int SPARKLE_INTERVAL = 6;

    // ================================================================
    //  CONSTRUCTOR
    // ================================================================

    public Map2() {
        super("/maps/map_level2.txt");

        loadObjects();

        enemies = new ArrayList<>();
        spawnEnemies();

        buildTraps();
        spawnDiamonds();

        System.out.println("[Map2] " + enemies.size() + " inamici, "
                + traps.size() + " capcane, "
                + DIAMOND_COUNT + " diamante.");
    }

    // ================================================================
    //  INCARCARE IMAGINI
    // ================================================================

    private void loadObjects() {
        BufferedImage doorSheet = ImageLoader.LoadImage("/textures/Door.png");
        if (doorSheet != null) {
            imgDoor = doorSheet.getSubimage(0, 0, 32, 32);
        } else {
            System.err.println("[Map2] Door.png nu a fost gasit!");
        }

        BufferedImage toolSheet = ImageLoader.LoadImage("/textures/sprite_unelte1.png");
        if (toolSheet != null) {
            imgFallingRock = toolSheet.getSubimage(0 * 32, 2 * 32, 32, 32);
            // Diamant: linia 0, coloana 0
            imgDiamond = toolSheet.getSubimage(0 * 32, 0 * 32, 32, 32);
        } else {
            System.err.println("[Map2] sprite_unelte1.png nu a fost gasit!");
        }
    }

    // ================================================================
    //  INAMICI
    // ================================================================

    private void spawnEnemies() {
        addEnemyIfFree(5,  4);
        addEnemyIfFree(15, 5);
        addEnemyIfFree(13, 8);
        addEnemyIfFree(45, 23);
        addEnemyIfFree(41, 23);
        addEnemyIfFree(45, 25);
        addEnemyIfFree(41, 25);
        addEnemyIfFree(45, 27);
        addEnemyIfFree(41, 27);
        addEnemyIfFree(33, 23);
        addEnemyIfFree(33, 25);
        addEnemyIfFree(33, 27);
        addEnemyIfFree(31, 23);
        addEnemyIfFree(31, 25);
        addEnemyIfFree(31, 27);
        addEnemyIfFree(29, 23);
        addEnemyIfFree(29, 25);
        addEnemyIfFree(29, 27);
        addEnemyIfFree(27, 27);
        addEnemyIfFree(27, 25);
        addEnemyIfFree(27, 23);
        addEnemyIfFree(20, 22);
        addEnemyIfFree(20, 25);
        addEnemyIfFree(20, 27);
        addEnemyIfFree(16, 22);
        addEnemyIfFree(16, 25);
        addEnemyIfFree(16, 27);
    }

    private void addEnemyIfFree(int col, int row) {
        if (!isSolid(col, row)) {
            enemies.add(new SnakeEnemy(col, row, this));
        }
    }

    public List<SnakeEnemy> getEnemies() {
        return enemies;
    }

    // ================================================================
    //  CAPCANE
    // ================================================================

    private void buildTraps() {
        traps = new ArrayList<>();

        for (int r = 0; r < MAP_ROWS; r++)
            for (int c = 0; c < MAP_COLS; c++)
                grassLayer[r][c] = -1;

        for (SnakeEnemy e : enemies) {
            int eCol    = e.getCol();
            int eRow    = e.getRow();
            int grassRow = eRow - 1;

            if (grassRow < 1) {
                System.out.println("[Map2] Sarpe la row=" + eRow + " prea sus; capcana ignorata.");
                continue;
            }

            grassLayer[grassRow][eCol] = iarba4;

            Trap t = new Trap(eCol, grassRow, eCol, eRow);
            traps.add(t);

            System.out.println("[Map2] Capcana: iarba(" + eCol + "," + grassRow
                    + ") piatra deasupra(" + eCol + "," + (grassRow - 1)
                    + ") sarpe(" + eCol + "," + eRow + ")");
        }
    }

    // ================================================================
    //  DIAMANTE – spawn random pe tile-uri accesibile
    // ================================================================

    private void spawnDiamonds() {
        diamondCols      = new int[DIAMOND_COUNT];
        diamondRows      = new int[DIAMOND_COUNT];
        diamondCollected = new boolean[DIAMOND_COUNT];
        diamondsRemaining = DIAMOND_COUNT;

        // Candidati raspanditi pe toata harta (zone fara pereti)
        int[][] candidates = {
                {10, 7},  {22, 4},  {35, 7},  {42, 12}, {7, 15},
                {18, 13}, {28, 7},  {38, 4},  {44, 18}, {12, 20},
                {25, 15}, {32, 12}, {40, 22}, {15, 10}, {20, 18},
                {30, 20}, {5, 25},  {48, 5},  {10, 30}, {45, 35},
                {3, 10},  {50, 10}, {8, 38},  {40, 38}, {25, 35}
        };

        List<int[]> pool = new ArrayList<>();
        for (int[] c : candidates) {
            if (c[0] >= 0 && c[0] < MAP_COLS
                    && c[1] >= 0 && c[1] < MAP_ROWS
                    && !isSolid(c[0], c[1])) {
                pool.add(c);
            }
        }

        // Completam cu pozitii random daca pool-ul e prea mic
        if (pool.size() < DIAMOND_COUNT) {
            Random rng2 = new Random(999);
            int attempts = 0;
            while (pool.size() < DIAMOND_COUNT && attempts < 3000) {
                int rc = 1 + rng2.nextInt(MAP_COLS - 2);
                int rr = 1 + rng2.nextInt(MAP_ROWS - 2);
                if (!isSolid(rc, rr) && rc > 4 && rr > 4) {
                    boolean dup = false;
                    for (int[] p : pool) {
                        if (p[0] == rc && p[1] == rr) { dup = true; break; }
                    }
                    if (!dup) pool.add(new int[]{rc, rr});
                }
                attempts++;
            }
        }

        Random rng = new Random(42);
        for (int i = 0; i < DIAMOND_COUNT; i++) {
            int idx = rng.nextInt(pool.size());
            diamondCols[i] = pool.get(idx)[0];
            diamondRows[i] = pool.get(idx)[1];
            pool.remove(idx);
            System.out.println("[Map2] Diamant " + i + " la ("
                    + diamondCols[i] + "," + diamondRows[i] + ")");
        }
    }

    /**
     * Apelata din GamePanel dupa fiecare miscare.
     * @return true daca s-a cules un diamant nou
     */
    public boolean tryPickDiamond(int playerCol, int playerRow) {
        for (int i = 0; i < DIAMOND_COUNT; i++) {
            if (!diamondCollected[i]
                    && diamondCols[i] == playerCol
                    && diamondRows[i] == playerRow) {
                diamondCollected[i] = true;
                diamondsRemaining--;
                System.out.println("[Map2] Diamant " + i + " cules! Ramas: " + diamondsRemaining);
                return true;
            }
        }
        return false;
    }

    public int  getDiamondsRemaining()  { return diamondsRemaining; }
    public boolean allDiamondsCollected() { return diamondsRemaining == 0; }

    // ================================================================
    //  USA
    // ================================================================

    @Override
    public void stepOnTile(int col, int row) {
        super.stepOnTile(col, row);

        if (traps == null) return;

        for (Trap t : traps) {
            if (t.state == TrapState.INTACT
                    && t.grassCol == col
                    && t.grassRow == row) {
                t.state = TrapState.FALLING;
                System.out.println("[Map2] Capcana declansata la (" + col + "," + row + ")!");
            }
        }
    }

    public boolean tryOpenDoor(int playerCol, int playerRow) {
        return Math.abs(playerCol - doorCol) + Math.abs(playerRow - doorRow) <= 1;
    }

    // ================================================================
    //  TICK (apelat de animTimer din GamePanel)
    // ================================================================

    @Override
    public void tickSparkle() {
        for (SnakeEnemy e : enemies) {
            e.tick();
        }

        if (traps != null) {
            for (Trap t : traps) {
                if (t.state != TrapState.FALLING) continue;

                t.rockPixelY += ROCK_FALL_SPEED;

                if (t.rockPixelY >= t.targetPixelY) {
                    t.rockPixelY = t.targetPixelY;
                    t.state = TrapState.DONE;
                    killSnakeUnderRock(t);
                }
            }
        }

        // anima diamantele (plutire)
        sparkleTick++;
        if (sparkleTick >= SPARKLE_INTERVAL) {
            sparkleTick = 0;
            sparkleFrame = (sparkleFrame + 1) % 4;
        }
    }

    private void killSnakeUnderRock(Trap t) {
        boolean killed = false;

        for (int i = enemies.size() - 1; i >= 0; i--) {
            SnakeEnemy e = enemies.get(i);

            if (e.getCol() == t.grassCol
                    && Math.abs(e.getRow() - t.enemyRow) <= 2) {
                enemies.remove(i);
                killed = true;
                System.out.println("[Map2] Sarpe omorat de piatra la col="
                        + t.grassCol + " row=" + e.getRow());
            }
        }

        if (!killed)
            System.out.println("[Map2] Piatra a cazut, sarpele nu era in raza.");
    }

    // ================================================================
    //  DESENARE
    // ================================================================

    @Override
    public void drawMap(Graphics g, int camX, int camY, int viewW, int viewH) {

        drawBaseTiles(g, camX, camY, viewW, viewH);

        drawTraps(g, camX, camY, viewW, viewH);

        drawDiamonds(g, camX, camY, viewW, viewH);

        if (imgDoor != null) {
            int sx = doorCol * TILE_SIZE - camX;
            int sy = doorRow * TILE_SIZE - camY;
            if (inView(sx, sy, viewW, viewH))
                g.drawImage(imgDoor, sx, sy, TILE_SIZE, TILE_SIZE, null);
        }

        drawGrassLayer(g, camX, camY, viewW, viewH);

        for (SnakeEnemy e : enemies)
            e.draw(g, camX, camY);
    }

    private void drawDiamonds(Graphics g, int camX, int camY, int viewW, int viewH) {
        if (imgDiamond == null) return;

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        for (int i = 0; i < DIAMOND_COUNT; i++) {
            if (diamondCollected[i]) continue;

            int sx = diamondCols[i] * TILE_SIZE - camX;
            int sy = diamondRows[i] * TILE_SIZE - camY;

            if (!inView(sx, sy, viewW, viewH)) continue;

            // efect plutire sus-jos
            int bob = (int)(Math.sin(sparkleFrame * Math.PI / 2.0) * 4);

            int drawX = sx + (TILE_SIZE - 32) / 2;
            int drawY = sy + (TILE_SIZE - 32) / 2 + bob;

            g2.drawImage(imgDiamond, drawX, drawY, 32, 32, null);

            // contur cyan pulsant
            float alpha = 0.4f + 0.4f * (float)Math.abs(Math.sin(sparkleFrame * Math.PI / 2.0));
            g2.setColor(new Color(0, 200, 255, (int)(alpha * 180)));
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(drawX - 2, drawY - 2, 36, 36, 6, 6);
            g2.setStroke(new BasicStroke(1f));
        }
    }

    private void drawTraps(Graphics g, int camX, int camY, int viewW, int viewH) {
        if (traps == null || imgFallingRock == null) return;

        Graphics2D g2 = (Graphics2D) g;

        for (Trap t : traps) {
            if (t.state == TrapState.DONE) continue;

            int screenX = t.grassCol * TILE_SIZE - camX;
            int screenY = (t.state == TrapState.INTACT)
                    ? (t.grassRow - 1) * TILE_SIZE - camY
                    : (int) t.rockPixelY - camY;

            if (!inView(screenX, screenY, viewW, viewH)) continue;

            g2.drawImage(imgFallingRock, screenX, screenY, TILE_SIZE, TILE_SIZE, null);

            if (t.state == TrapState.FALLING) {
                g2.setColor(new Color(220, 50, 50, 160));
                g2.setStroke(new BasicStroke(3f));
                g2.drawRect(screenX + 2, screenY + 2, TILE_SIZE - 4, TILE_SIZE - 4);
                g2.setStroke(new BasicStroke(1f));
            }
        }
    }

    private boolean inView(int sx, int sy, int viewW, int viewH) {
        return sx + TILE_SIZE >= 0 && sx < viewW
                && sy + TILE_SIZE >= 0 && sy < viewH;
    }
}