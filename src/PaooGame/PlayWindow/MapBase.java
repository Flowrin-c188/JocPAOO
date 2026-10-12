package PaooGame.PlayWindow;

import PaooGame.Graphics.ImageLoader;
import PaooGame.Graphics.SpriteSheet;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/*! \class MapBase
    \brief Clasa abstracta care contine logica comuna pentru toate hartile:
    incarcare din fisier, tile-uri, layer iarba, desenare de baza.
    Fiecare harta concreta (Map1, Map2) extinde aceasta clasa.
 */
public abstract class MapBase extends JPanel {

    public static final int TILE_SIZE = 50;
    public static final int MAP_COLS  = 55;
    public static final int MAP_ROWS  = 45;

    // ID-uri tile-uri
    public static final int iarba    = 0;
    public static final int iarba1   = 1;
    public static final int iarba2   = 2;
    public static final int iarba3   = 3;
    public static final int iarba4   = 4;
    public static final int pamant   = 5;
    public static final int pamant1  = 6;
    public static final int pamant2  = 7;
    public static final int pamant3  = 8;
    public static final int pamant4  = 9;
    public static final int desert   = 10;
    public static final int desert1  = 11;
    public static final int piatra   = 12;
    public static final int piatra1  = 13;
    public static final int piatra2  = 14;
    public static final int lemn     = 15;
    public static final int lemn1    = 16;
    public static final int lemn2    = 17;
    public static final int rock     = 18;
    public static final int rock1    = 19;
    public static final int rock2    = 20;
    public static final int rock3    = 21;
    public static final int rock4    = 22;
    public static final int rock5    = 23;

    protected int[][]         MAP;
    protected int[][]         grassLayer;
    protected BufferedImage[] tileImages;

    // ---------------------------------------------------------------
    // Constructor — fiecare subclasa apeleaza super(mapPath)
    // ---------------------------------------------------------------
    public MapBase(String mapPath) {
        MAP = loadMap(mapPath);
        loadTiles();
        initGrassLayer();
    }

    // ---------------------------------------------------------------
    // Incarcare harta din fisier text
    // ---------------------------------------------------------------
    protected int[][] loadMap(String path) {
        int[][] map = new int[MAP_ROWS][MAP_COLS];
        try {
            InputStream is = getClass().getResourceAsStream(path);
            if (is == null) { System.err.println("Harta nu a fost gasita: " + path); return map; }
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line; int row = 0;
            while ((line = br.readLine()) != null && row < MAP_ROWS) {
                String[] tokens = line.trim().split("\\s+");
                for (int col = 0; col < Math.min(tokens.length, MAP_COLS); col++)
                    map[row][col] = Integer.parseInt(tokens[col]);
                row++;
            }
            br.close();
        } catch (IOException e) { e.printStackTrace(); }
        return map;
    }

    // Incarcare tile-uri din SpriteHarta.png
    private void loadTiles() {
        BufferedImage sheet = ImageLoader.LoadImage("/textures/SpriteHarta.png");
        SpriteSheet ss = new SpriteSheet(sheet);

        tileImages = new BufferedImage[24];
        tileImages[iarba]   = ss.crop(0, 0);
        tileImages[iarba1]  = ss.crop(1, 0);
        tileImages[iarba2]  = ss.crop(2, 0);
        tileImages[iarba3]  = ss.crop(3, 0);
        tileImages[iarba4]  = ss.crop(0, 1);
        tileImages[pamant]  = ss.crop(4, 0);
        tileImages[pamant1] = ss.crop(5, 0);
        tileImages[pamant2] = ss.crop(1, 1);
        tileImages[pamant3] = ss.crop(2, 1);
        tileImages[pamant4] = ss.crop(3, 1);
        tileImages[desert]  = ss.crop(4, 1);
        tileImages[desert1] = ss.crop(5, 1);
        tileImages[piatra]  = ss.crop(0, 2);
        tileImages[piatra1] = ss.crop(1, 2);
        tileImages[piatra2] = ss.crop(2, 2);
        tileImages[lemn]    = ss.crop(3, 2);
        tileImages[lemn1]   = ss.crop(4, 2);
        tileImages[lemn2]   = ss.crop(5, 2);
        tileImages[rock]    = ss.crop(0, 3);
        tileImages[rock1]   = ss.crop(1, 3);
        tileImages[rock2]   = ss.crop(2, 3);
        tileImages[rock3]   = ss.crop(3, 3);
        tileImages[rock4]   = ss.crop(4, 3);
        tileImages[rock5]   = ss.crop(5, 3);
    }


    protected void initGrassLayer() {
        grassLayer = new int[MAP_ROWS][MAP_COLS];
        for (int row = 0; row < MAP_ROWS; row++)
            for (int col = 0; col < MAP_COLS; col++) {
                int id = MAP[row][col];
                grassLayer[row][col] = (id == rock5 || id == rock) ? -1 : iarba4;
            }
    }


    // Verifica daca e Solid
    public boolean isSolid(int col, int row) {
        if (col < 0 || col >= MAP_COLS || row < 0 || row >= MAP_ROWS) return true;
        int id = MAP[row][col];
        return id == rock5 || id == rock;
    }


    // Distruge iarba daca calci pe ea

    public void stepOnTile(int col, int row) {
        if (col < 0 || col >= MAP_COLS || row < 0 || row >= MAP_ROWS) return;
        if (grassLayer[row][col] != -1) grassLayer[row][col] = -1;
    }

    // ---------------------------------------------------------------
    // Desenare tile-uri de baza (pasa 1)
    // ---------------------------------------------------------------
    protected void drawBaseTiles(Graphics g, int camX, int camY, int viewW, int viewH) {
        int startCol = camX / TILE_SIZE;
        int startRow = camY / TILE_SIZE;
        int endCol   = Math.min(MAP_COLS, (camX + viewW) / TILE_SIZE + 1);
        int endRow   = Math.min(MAP_ROWS, (camY + viewH) / TILE_SIZE + 1);

        for (int row = startRow; row < endRow; row++)
            for (int col = startCol; col < endCol; col++) {
                int id = MAP[row][col];
                BufferedImage img = (id >= 0 && id < tileImages.length) ? tileImages[id] : tileImages[iarba];
                g.drawImage(img, col * TILE_SIZE - camX, row * TILE_SIZE - camY, TILE_SIZE, TILE_SIZE, null);
            }
    }

    // ---------------------------------------------------------------
    // Desenare layer iarba (pasa 3)
    // ---------------------------------------------------------------
    protected void drawGrassLayer(Graphics g, int camX, int camY, int viewW, int viewH) {
        int startCol = camX / TILE_SIZE;
        int startRow = camY / TILE_SIZE;
        int endCol   = Math.min(MAP_COLS, (camX + viewW) / TILE_SIZE + 1);
        int endRow   = Math.min(MAP_ROWS, (camY + viewH) / TILE_SIZE + 1);

        for (int row = startRow; row < endRow; row++)
            for (int col = startCol; col < endCol; col++) {
                int grassId = grassLayer[row][col];
                if (grassId != -1)
                    g.drawImage(tileImages[grassId],
                            col * TILE_SIZE - camX, row * TILE_SIZE - camY,
                            TILE_SIZE, TILE_SIZE, null);
            }
    }

    // ---------------------------------------------------------------
    // Metoda abstracta — fiecare harta isi deseneaza obiectele proprii
    // (cufar, usa, sparkle etc.) intre tile-uri de baza si iarba
    // ---------------------------------------------------------------
    public abstract void drawMap(Graphics g, int camX, int camY, int viewW, int viewH);

    public abstract void tickSparkle();

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawMap(g, 0, 0, MAP_COLS * TILE_SIZE, MAP_ROWS * TILE_SIZE);
    }
}