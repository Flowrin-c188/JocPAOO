package PaooGame.Tiles;

import PaooGame.Graphics.Assets;

/*! \class public class WaterTile extends Tile
    \brief Abstractizeaza notiunea de dala de tip apa.
 */public class rock5Tile extends Tile
{
    /*! \fn public WaterTile(int id)
       \brief Constructorul de initializare al clasei

       \param id Id-ul dalei util in desenarea hartii.
    */
    public rock5Tile(int id)
    {
        super(Assets.rock5, id);
    }

    /*! \fn public boolean IsSolid()
       \brief Suprascrie metoda IsSolid() din clasa de baza in sensul ca va fi luat in calcul in caz de coliziune.
    */
    @Override
    public boolean IsSolid()
    {
        return true;
    }
}
