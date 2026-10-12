package PaooGame.Tiles;

import java.awt.*;
import java.awt.image.BufferedImage;

/*! \class public class Tile
    \brief Retine toate dalele intr-un vector si ofera posibilitatea regasirii dupa un id.
 */
public class Tile
{
    private static final int NO_TILES   = 32;
    public static Tile[] tiles          = new Tile[NO_TILES];       /*!< Vector de referinte de tipuri de dale.*/

        /// De remarcat ca urmatoarele dale sunt statice si publice. Acest lucru imi permite sa le am incarcate
        /// o singura data in memorie
    public static Tile grassTile        = new GrassTile(0);
    public static Tile iarba1Tile       = new iarba1Tile(1);/*!< Dala de tip iarba*/
    public static Tile iarba2Tile       = new iarba2Tile(2);
    public static Tile iarba3Tile       = new iarba3Tile(3);
    public static Tile iarba4Tile       = new iarba3Tile(4);
    public static Tile pamantTile           = new pamantTile(5);
    public static Tile pamant1Tile          = new pamant1Tile(6);
    public static Tile pamant2Tile          = new pamant2Tile(7);
    public static Tile pamant3Tile          = new pamant3Tile(8);
    public static Tile pamant4Tile          = new pamant4Tile(9);
    public static Tile desertTile           = new desertTile(10);
    public static Tile desert1Tile           = new desert1Tile(11);
    public static Tile piatraTile           = new piatraTile(12);
    public static Tile piatra1Tile           = new piatra1Tile(13);
    public static Tile piatra2Tile           = new piatra2Tile(14);
    public static Tile lemnTile           = new lemnTile(15);
    public static Tile lemn1Tile           = new lemn1Tile(16);
    public static Tile lemn2Tile           = new lemn2Tile(17);
    public static Tile rockTile           = new rockTile(18);
    public static Tile rock1Tile           = new rock1Tile(19);
    public static Tile rock2Tile           = new rock2Tile(20);
    public static Tile rock3Tile           = new rock3Tile(21);
    public static Tile rock4Tile           = new rock4Tile(22);
    public static Tile rock5Tile           = new rock5Tile(23);


    public static final int TILE_WIDTH  = 48;                       /*!< Latimea unei dale.*/
    public static final int TILE_HEIGHT = 48;                       /*!< Inaltimea unei dale.*/

    protected BufferedImage img;                                    /*!< Imaginea aferenta tipului de dala.*/
    protected final int id;                                         /*!< Id-ul unic aferent tipului de dala.*/

    /*! \fn public Tile(BufferedImage texture, int id)
        \brief Constructorul aferent clasei.

        \param image Imaginea corespunzatoare dalei.
        \param id Id-ul dalei.
     */
    public Tile(BufferedImage image, int idd)
    {
        img = image;
        id = idd;

        tiles[id] = this;
    }

    /*! \fn public void Update()
        \brief Actualizeaza proprietatile dalei.
     */
    public void Update()
    {

    }

    /*! \fn public void Draw(Graphics g, int x, int y)
        \brief Deseneaza in fereastra dala.

        \param g Contextul grafic in care sa se realizeze desenarea
        \param x Coordonata x in cadrul ferestrei unde sa fie desenata dala
        \param y Coordonata y in cadrul ferestrei unde sa fie desenata dala
     */
    public void Draw(Graphics g, int x, int y)
    {
            /// Desenare dala
        g.drawImage(img, x, y, TILE_WIDTH, TILE_HEIGHT, null);
    }

    /*! \fn public boolean IsSolid()
        \brief Returneaza proprietatea de dala solida (supusa coliziunilor) sau nu.
     */
    public boolean IsSolid()
    {
        return false;
    }

    /*! \fn public int GetId()
        \brief Returneaza id-ul dalei.
     */
    public int GetId()
    {
        return id;
    }
}
