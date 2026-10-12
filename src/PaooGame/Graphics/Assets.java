package PaooGame.Graphics;

import java.awt.image.BufferedImage;

/*! \class public class Assets
    \brief Clasa incarca fiecare element grafic necesar jocului.

    Game assets include tot ce este folosit intr-un joc: imagini, sunete, harti etc.
 */
public class Assets
{
    public static BufferedImage menuBackground;
    public static BufferedImage optionBackground;


        /// Referinte catre elementele grafice (dale) utilizate in joc.
    public static BufferedImage playerLeft;
    public static BufferedImage playerRight;
    public static BufferedImage iarba;
    public static BufferedImage iarba1;
    public static BufferedImage iarba2;
    public static BufferedImage iarba3;
    public static BufferedImage iarba4;
    public static BufferedImage pamant;
    public static BufferedImage pamant1;
    public static BufferedImage pamant2;
    public static BufferedImage pamant3;
    public static BufferedImage pamant4;
    public static BufferedImage desert;
    public static BufferedImage desert1;
    public static BufferedImage piatra;
    public static BufferedImage piatra1;
    public static BufferedImage piatra2;
    public static BufferedImage lemn;
    public static BufferedImage lemn1;
    public static BufferedImage lemn2;
    public static BufferedImage rock;
    public static BufferedImage rock1;
    public static BufferedImage rock2;
    public static BufferedImage rock3;
    public static BufferedImage rock4;
    public static BufferedImage rock5;



    /*! \fn public static void Init()
        \brief Functia initializaza referintele catre elementele grafice utilizate.

        Aceasta functie poate fi rescrisa astfel incat elementele grafice incarcate/utilizate
        sa fie parametrizate. Din acest motiv referintele nu sunt finale.
     */
    public static void Init()
    {
        menuBackground = ImageLoader.LoadImage("/textures/img11.jpeg");
        lemn = ImageLoader.LoadImage("/textures/img1.jpeg");
            /// Se creaza temporar un obiect SpriteSheet initializat prin intermediul clasei ImageLoader
        SpriteSheet sheet = new SpriteSheet(ImageLoader.LoadImage("/textures/img1.jpeg"));

            /// Se obtin subimaginile corespunzatoare elementelor necesare.
        iarba = sheet.crop(0, 0);
        iarba1 = sheet.crop(1,0);
        iarba2 = sheet.crop(2,0);
        iarba3 = sheet.crop(3,0);
        iarba4 = sheet.crop(0, 1);
        pamant = sheet.crop(4, 0);
        pamant1 = sheet.crop(5, 0);
        pamant2 = sheet.crop(1, 1);
        pamant3 = sheet.crop(2, 1);
        pamant4 = sheet.crop(3, 1);
        desert = sheet.crop( 4, 1);
        desert1 = sheet.crop( 5, 1);
        piatra = sheet.crop(0, 2);
        piatra1 = sheet.crop(1, 2);
        piatra2 = sheet.crop(2, 2);
        lemn = sheet.crop(3, 2);
        lemn1 = sheet.crop(4, 2);
        lemn2 = sheet.crop(5, 2);
        rock = sheet.crop(0, 3);
        rock1 = sheet.crop(1, 3);
        rock2 = sheet.crop(2, 3);
        rock3 = sheet.crop(3, 3);
        rock4 = sheet.crop(4, 3);
        rock5 = sheet.crop(5, 3);

        playerLeft = sheet.crop(0, 2);
        playerRight = sheet.crop(1, 2);

    }
}
