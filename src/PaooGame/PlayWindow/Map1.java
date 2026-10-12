package PaooGame.PlayWindow;

import PaooGame.Graphics.ImageLoader;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

/*! \class Map1
    \brief Prima harta a jocului (map_level1.txt).
    Contine logica pentru: cufar, cheie, usa, layer iarba, sparkle.
 */
public class Map1 extends MapBase {

    private BufferedImage imgChest;
    private BufferedImage imgKey;
    private BufferedImage imgDoor;

    private int     chestCol      = 5;
    private int     chestRow      = 5;
    private boolean chestRevealed = false;
    private boolean keyPickedUp   = false;

    private final int doorCol = 48;
    private final int doorRow = 37;

    private int sparkleTimer = 0;
    private static final int SPARKLE_DURATION = 40;

    public Map1() {
        super("/maps/map_level1.txt");

        placeRandomChest();
        loadObjects();

        System.out.println("[Map1] Cufar la col=" + chestCol + ", row=" + chestRow);
        System.out.println("[Map1] Usa la col=" + doorCol + ", row=" + doorRow);
    }
    private void placeRandomChest() {
        Random rand = new Random();

        do {
            chestCol = rand.nextInt(MAP_COLS);
            chestRow = rand.nextInt(MAP_ROWS);
        } while (
                isSolid(chestCol, chestRow) ||
                        (chestCol == doorCol && chestRow == doorRow)
        );
    }

    // Incarcare sprite-uri obiecte (cufar, cheie, usa)
    private void loadObjects() {
        BufferedImage toolSheet = ImageLoader.LoadImage("/textures/sprite_unelte1.png");
        if (toolSheet != null) {
            imgChest = toolSheet.getSubimage(2 * 32, 0, 32, 32);
            imgKey   = toolSheet.getSubimage(2 * 32, 32, 32, 32);
        } else {
            System.err.println("[Map1] sprite_unelte1.png nu a fost gasit!");
        }

        BufferedImage doorSheet = ImageLoader.LoadImage("/textures/Door.png");
        if (doorSheet != null) {
            imgDoor = doorSheet.getSubimage(0, 0, 32, 32);
        } else {
            System.err.println("[Map1] Door.png nu a fost gasit!");
        }
    }

    // stepOnTile — suprascris pentru a dezvalui cufarul
    @Override
    public void stepOnTile(int col, int row) {
        super.stepOnTile(col, row);
        if (col == chestCol && row == chestRow) chestRevealed = true;
    }

    // Interactiune cufar (ENTER)
    public boolean tryOpenChest(int playerCol, int playerRow) {
        if (keyPickedUp) return false;
        int dc = Math.abs(playerCol - chestCol);
        int dr = Math.abs(playerRow - chestRow);
        if (chestRevealed && (dc + dr <= 1)) {
            keyPickedUp  = true;
            sparkleTimer = SPARKLE_DURATION;
            return true;
        }
        return false;
    }

    // Interactiune usa (ENTER)
    public boolean tryOpenDoor(int playerCol, int playerRow, boolean hasKey) {
        if (!hasKey) return false;
        int dc = Math.abs(playerCol - doorCol);
        int dr = Math.abs(playerRow - doorRow);
        return (dc + dr <= 1);
    }

    // Reset harta
    public void reset() {
        MAP = loadMap("/maps/map_level1.txt");
        initGrassLayer();

        placeRandomChest();

        chestRevealed = false;
        keyPickedUp   = false;
        sparkleTimer  = 0;
    }


    @Override
    public void tickSparkle() {
        if (sparkleTimer > 0) sparkleTimer--;
    }



    // Desenare completa harta 1
    @Override
    public void drawMap(Graphics g, int camX, int camY, int viewW, int viewH) {

        // Pasa 1: tile-uri de baza
        drawBaseTiles(g, camX, camY, viewW, viewH);

        // Pasa 2a: cufar
        if (chestRevealed && !keyPickedUp && imgChest != null) {
            int sx = chestCol * TILE_SIZE - camX;
            int sy = chestRow * TILE_SIZE - camY;
            if (sx + TILE_SIZE >= 0 && sx < viewW && sy + TILE_SIZE >= 0 && sy < viewH) {
                g.drawImage(imgChest, sx, sy, TILE_SIZE, TILE_SIZE, null);
                drawChestPrompt(g, sx, sy);
            }
        }

        // Pasa 2b: sparkle cheie
        if (keyPickedUp && sparkleTimer > 0 && imgKey != null) {
            int sx     = chestCol * TILE_SIZE - camX;
            int sy     = chestRow * TILE_SIZE - camY;
            float prog = 1f - (sparkleTimer / (float) SPARKLE_DURATION);
            int offsetY = (int)(prog * 30);
            int alpha   = sparkleTimer > 10 ? 255 : (int)(sparkleTimer * 25.5f);
            Graphics2D g2 = (Graphics2D) g;
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha / 255f));
            g2.drawImage(imgKey, sx, sy - offsetY, TILE_SIZE, TILE_SIZE, null);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        }

        // Pasa 2c: usa
        if (imgDoor != null) {
            int sx = doorCol * TILE_SIZE - camX;
            int sy = doorRow * TILE_SIZE - camY;
            if (sx + TILE_SIZE >= 0 && sx < viewW && sy + TILE_SIZE >= 0 && sy < viewH)
                g.drawImage(imgDoor, sx, sy, TILE_SIZE, TILE_SIZE, null);
        }

        // Pasa 3: layer iarba deasupra
        drawGrassLayer(g, camX, camY, viewW, viewH);
    }

    private void drawChestPrompt(Graphics g, int sx, int sy) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int bx = sx + TILE_SIZE / 2 - 24;
        int by = sy - 22;
        g2.setColor(new Color(0, 0, 0, 170));
        g2.fillRoundRect(bx, by, 48, 18, 8, 8);
        g2.setColor(new Color(255, 215, 0));
        g2.setFont(new Font("Arial", Font.BOLD, 10));
        g2.drawString("ENTER", bx + 7, by + 12);
    }
}