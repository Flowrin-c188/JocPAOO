package PaooGame.PlayWindow;

import PaooGame.Graphics.ImageLoader;

import java.awt.*;
import java.awt.image.BufferedImage;

/*! \class Enemy
    \brief Sarpe cu miscare smooth pe pixeli (sus-jos).
    Se misca cu SPEED pixeli/tick spre targetRow.
    La obstacol, inverseaza directia.
 */
public class SnakeEnemy {

    private static final int DIR_DOWN = 0;
    private static final int DIR_UP   = 1;

    private static final int FRAME_COUNT = 4;
    private static final int ANIM_DELAY  = 5;
    private static final int SPEED       = 3;  // pixeli per tick (mai lent decat jucatorul)

    private BufferedImage[][] sprites;

    // Pozitie reala in pixeli
    private float x;
    private float y;
    private float targetY;

    private int direction;
    private int animFrame;
    private int animTick;

    private final MapBase map;

    public SnakeEnemy(int startCol, int startRow, MapBase map) {
        this.x         = startCol * MapBase.TILE_SIZE;
        this.y         = startRow * MapBase.TILE_SIZE;
        this.targetY   = this.y;
        this.direction = DIR_DOWN;
        this.animFrame = 0;
        this.animTick  = 0;
        this.map       = map;

        loadSprites();
    }

    // ---------------------------------------------------------------
    // Incarcare sprite-uri
    // ---------------------------------------------------------------
    private void loadSprites() {
        BufferedImage sheet = ImageLoader.LoadImage("/textures/sarpe_32.png");
        if (sheet == null) {
            System.err.println("[Enemy] sarpe_32.png nu a fost gasit!");
            return;
        }
        int ts = 32;
        sprites = new BufferedImage[4][FRAME_COUNT];
        for (int dir = 0; dir < 4; dir++)
            for (int f = 0; f < FRAME_COUNT; f++)
                sprites[dir][f] = sheet.getSubimage(f * ts, dir * ts, ts, ts);
    }

    // ---------------------------------------------------------------
    // Tick — miscare smooth + animatie
    // ---------------------------------------------------------------
    public void tick() {
        // Animatie
        animTick++;
        if (animTick >= ANIM_DELAY) {
            animTick  = 0;
            animFrame = (animFrame + 1) % FRAME_COUNT;
        }

        // Miscare smooth spre target
        float dy = targetY - y;
        if (Math.abs(dy) > SPEED) {
            y += (dy > 0 ? 1 : -1) * SPEED;
        } else {
            // Am ajuns la target — porneste imediat spre urmatorul tile
            y = targetY;
            decideNextMove();
        }
    }

    // ---------------------------------------------------------------
    // Decide urmatorul tile spre care se misca
    // ---------------------------------------------------------------
    private void decideNextMove() {
        int col     = getCol();
        int row     = getRow();
        int nextRow = row + (direction == DIR_DOWN ? 1 : -1);

        if (map.isSolid(col, nextRow)) {
            direction = (direction == DIR_DOWN) ? DIR_UP : DIR_DOWN;
            nextRow   = row + (direction == DIR_DOWN ? 1 : -1);
        }

        if (!map.isSolid(col, nextRow)) {
            targetY = nextRow * MapBase.TILE_SIZE;
        }
    }

    // ---------------------------------------------------------------
    // Coliziune cu jucatorul (bazata pe pozitie in pixeli)
    // ---------------------------------------------------------------
    public boolean checkCollision(float playerX, float playerY) {
        float ts   = MapBase.TILE_SIZE;
        float margin = ts * 0.6f; // zona de coliziune (60% din tile)
        return Math.abs(x - playerX) < margin && Math.abs(y - playerY) < margin;
    }

    // ---------------------------------------------------------------
    // Desenare
    // ---------------------------------------------------------------
    public void draw(Graphics g, int camX, int camY) {
        if (sprites == null) return;
        int screenX = (int)(x - camX);
        int screenY = (int)(y - camY);
        if (screenX + MapBase.TILE_SIZE < 0 || screenX > 800) return;
        if (screenY + MapBase.TILE_SIZE < 0 || screenY > 600) return;
        g.drawImage(sprites[direction][animFrame],
                screenX, screenY,
                MapBase.TILE_SIZE, MapBase.TILE_SIZE, null);
    }

    public int   getCol() { return (int)(x / MapBase.TILE_SIZE); }
    public int   getRow() { return (int)(y / MapBase.TILE_SIZE); }
}