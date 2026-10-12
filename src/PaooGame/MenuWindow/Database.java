package PaooGame.MenuWindow;

import java.sql.*;

public class Database {
    private static final String URL = "jdbc:sqlite:game.db";

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.out.println("Driverul SQLite JDBC nu a fost gasit.");
            e.printStackTrace();
        }
    }

    public static void createTable() {
        String sql =
                "CREATE TABLE IF NOT EXISTS players (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "name TEXT NOT NULL UNIQUE, " +
                        "level INTEGER NOT NULL DEFAULT 1, " +
                        "col INTEGER NOT NULL DEFAULT 3, " +
                        "row INTEGER NOT NULL DEFAULT 3, " +
                        "hp INTEGER NOT NULL DEFAULT 5, " +
                        "hammerPicked INTEGER NOT NULL DEFAULT 0, " +
                        "spidersKilled INTEGER NOT NULL DEFAULT 0, " +
                        "bossHp INTEGER NOT NULL DEFAULT 3" +
                        ");";

        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement()) {

            stmt.execute(sql);

            addColumnIfMissing(conn, "level", "INTEGER NOT NULL DEFAULT 1");
            addColumnIfMissing(conn, "col", "INTEGER NOT NULL DEFAULT 3");
            addColumnIfMissing(conn, "row", "INTEGER NOT NULL DEFAULT 3");
            addColumnIfMissing(conn, "hp", "INTEGER NOT NULL DEFAULT 5");
            addColumnIfMissing(conn, "hammerPicked", "INTEGER NOT NULL DEFAULT 0");
            addColumnIfMissing(conn, "spidersKilled", "INTEGER NOT NULL DEFAULT 0");
            addColumnIfMissing(conn, "bossHp", "INTEGER NOT NULL DEFAULT 3");

            System.out.println("Tabelul players este gata.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void addColumnIfMissing(Connection conn, String columnName, String columnDefinition) {
        ResultSet rs = null;

        try {
            DatabaseMetaData metaData = conn.getMetaData();
            rs = metaData.getColumns(null, null, "players", columnName);

            if (!rs.next()) {
                String sql = "ALTER TABLE players ADD COLUMN " + columnName + " " + columnDefinition;

                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(sql);
                    System.out.println("Coloana adaugata: " + columnName);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            try {
                if (rs != null) rs.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void createPlayerIfNotExists(String name) {
        String sql =
                "INSERT OR IGNORE INTO players(name, level, col, row, hp, hammerPicked, spidersKilled, bossHp) " +
                        "VALUES(?, 1, 3, 3, 5, 0, 0, 3)";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            pstmt.executeUpdate();

            System.out.println("Jucator incarcat in baza de date: " + name);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int getPlayerLevel(String name) {
        String sql = "SELECT level FROM players WHERE name = ?";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) return rs.getInt("level");

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 1;
    }

    public static void savePlayerLevel(String name, int level) {
        String sql = "UPDATE players SET level = ? WHERE name = ?";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, level);
            pstmt.setString(2, name);
            pstmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void savePlayerPosition(String name, int col, int row) {
        String sql = "UPDATE players SET col = ?, row = ? WHERE name = ?";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, col);
            pstmt.setInt(2, row);
            pstmt.setString(3, name);
            pstmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int[] getPlayerPosition(String name) {
        String sql = "SELECT col, row FROM players WHERE name = ?";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new int[]{rs.getInt("col"), rs.getInt("row")};
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return new int[]{3, 3};
    }

    public static void savePlayerHp(String name, int hp) {
        String sql = "UPDATE players SET hp = ? WHERE name = ?";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, hp);
            pstmt.setString(2, name);
            pstmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int getPlayerHp(String name) {
        String sql = "SELECT hp FROM players WHERE name = ?";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) return rs.getInt("hp");

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 5;
    }

    public static void saveHammerPicked(String name, boolean picked) {
        String sql = "UPDATE players SET hammerPicked = ? WHERE name = ?";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, picked ? 1 : 0);
            pstmt.setString(2, name);
            pstmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean getHammerPicked(String name) {
        String sql = "SELECT hammerPicked FROM players WHERE name = ?";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) return rs.getInt("hammerPicked") == 1;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public static void saveSpidersKilled(String name, int killed) {
        String sql = "UPDATE players SET spidersKilled = ? WHERE name = ?";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, killed);
            pstmt.setString(2, name);
            pstmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int getSpidersKilled(String name) {
        String sql = "SELECT spidersKilled FROM players WHERE name = ?";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) return rs.getInt("spidersKilled");

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    public static void saveBossHp(String name, int bossHp) {
        String sql = "UPDATE players SET bossHp = ? WHERE name = ?";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, bossHp);
            pstmt.setString(2, name);
            pstmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int getBossHp(String name) {
        String sql = "SELECT bossHp FROM players WHERE name = ?";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) return rs.getInt("bossHp");

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 3;
    }

    public static void resetLevel3Progress(String name) {
        String sql = "UPDATE players SET hammerPicked = 0, spidersKilled = 0, bossHp = 3 WHERE name = ?";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            pstmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void savePlayerData(String name, int level, int col, int row, int hp) {
        String sql = "UPDATE players SET level = ?, col = ?, row = ?, hp = ? WHERE name = ?";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, level);
            pstmt.setInt(2, col);
            pstmt.setInt(3, row);
            pstmt.setInt(4, hp);
            pstmt.setString(5, name);
            pstmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static PlayerData getPlayerData(String name) {
        String sql = "SELECT level, col, row, hp, hammerPicked, spidersKilled, bossHp FROM players WHERE name = ?";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new PlayerData(
                        rs.getInt("level"),
                        rs.getInt("col"),
                        rs.getInt("row"),
                        rs.getInt("hp"),
                        rs.getInt("hammerPicked") == 1,
                        rs.getInt("spidersKilled"),
                        rs.getInt("bossHp")
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return new PlayerData(1, 3, 3, 5, false, 0, 3);
    }

    public static class PlayerData {
        public int level;
        public int col;
        public int row;
        public int hp;
        public boolean hammerPicked;
        public int spidersKilled;
        public int bossHp;

        public PlayerData(
                int level,
                int col,
                int row,
                int hp,
                boolean hammerPicked,
                int spidersKilled,
                int bossHp
        ) {
            this.level = level;
            this.col = col;
            this.row = row;
            this.hp = hp;
            this.hammerPicked = hammerPicked;
            this.spidersKilled = spidersKilled;
            this.bossHp = bossHp;
        }
    }
}