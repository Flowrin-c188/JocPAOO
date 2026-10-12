package PaooGame.PlayWindow;

public class MapFactory {

    public static MapBase createMap(int level, String playerName) {
        switch (level) {
            case 1:
                return new Map1();

            case 2:
                return new Map2();

            case 3:
                return new Map3(playerName);

            default:
                return new Map1();
        }
    }
}