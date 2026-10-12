package PaooGame.PlayWindow;

import PaooGame.MenuWindow.Database;
import PaooGame.MenuWindow.GameOption;
import PaooGame.MenuWindow.MenuPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

public class GamePanel extends JPanel {

    public static final int VIEW_WIDTH  = 800;
    public static final int VIEW_HEIGHT = 600;

    private final JFrame parentFrame;
    private final String playerName;

    private MapBase currentMap;
    private Player player;

    private boolean hasKey = false;
    private boolean gameOver = false;
    private boolean showingGameOver = false;

    // Pozitia de start a nivelului curent, folosita doar pentru respawn / nivel nou
    private int startCol = 3;
    private int startRow = 3;

    private int currentLevel = 1;
    public static int LAST_LEVEL = 1;

    private JDialog dialog;

    // Mesaj HUD temporar
    private String hudMessage = "";
    private int hudMessageTimer = 0;
    private static final int MSG_DURATION = 20;

    public javax.swing.Timer animTimer;
    private int coordTimer = 0;
    private boolean showChestCoords = false;
    private static final int COORD_DELAY = 300;

    // ---------------------------------------------------------------
    // Constructor normal: reia jocul din baza de date
    // ---------------------------------------------------------------
    public GamePanel(JFrame frame, String name) {
        this.parentFrame = frame;
        this.playerName = name;

        Database.createTable();
        Database.createPlayerIfNotExists(playerName);

        currentLevel = Database.getPlayerLevel(playerName);
        GamePanel.LAST_LEVEL = currentLevel;

        loadMapForLevel(currentLevel);

        int[] pos = Database.getPlayerPosition(playerName);

        player = new Player(pos[0], pos[1], currentMap, playerName);

        setPreferredSize(new Dimension(VIEW_WIDTH, VIEW_HEIGHT));
        setLayout(null);
        setFocusable(true);

        setupAnimTimer();
        setupInput();
        setupPauseButton();

        currentMap.stepOnTile(player.getCol(), player.getRow());

        System.out.println("Joc reluat pentru " + playerName +
                " la level=" + currentLevel +
                ", col=" + player.getCol() +
                ", row=" + player.getRow());

        SwingUtilities.invokeLater(this::requestFocusInWindow);
    }

    // ---------------------------------------------------------------
    // Constructor cu level primit: porneste level-ul dat, dar foloseste pozitia salvata
    // ---------------------------------------------------------------
    public GamePanel(JFrame frame, String name, int level) {
        this.parentFrame = frame;
        this.playerName = name;

        Database.createTable();
        Database.createPlayerIfNotExists(playerName);

        this.currentLevel = level;
        GamePanel.LAST_LEVEL = level;

        Database.savePlayerLevel(playerName, currentLevel);

        loadMapForLevel(currentLevel);

        int[] pos = Database.getPlayerPosition(playerName);

        player = new Player(pos[0], pos[1], currentMap, playerName);

        setPreferredSize(new Dimension(VIEW_WIDTH, VIEW_HEIGHT));
        setLayout(null);
        setFocusable(true);

        setupAnimTimer();
        setupInput();
        setupPauseButton();

        currentMap.stepOnTile(player.getCol(), player.getRow());

        System.out.println("Joc pornit pentru " + playerName +
                " la level=" + currentLevel +
                ", col=" + player.getCol() +
                ", row=" + player.getRow());

        SwingUtilities.invokeLater(this::requestFocusInWindow);
    }

    // ---------------------------------------------------------------
    // Incarca harta in functie de nivel
    // ---------------------------------------------------------------
    private void loadMapForLevel(int level) {
        if (level == 3) {
            currentMap = MapFactory.createMap(3,playerName);
            startCol = 3;
            startRow = 3;
            currentLevel = 3;
        } else if (level == 2) {
            currentMap = MapFactory.createMap(2,playerName);
            startCol = 1;
            startRow = 1;
            currentLevel = 2;
        } else {
            currentMap = MapFactory.createMap(1,playerName);
            startCol = 3;
            startRow = 3;
            currentLevel = 1;
        }
    }

    // ---------------------------------------------------------------
    // Timer principal
    // ---------------------------------------------------------------
    private void setupAnimTimer() {
        animTimer = new javax.swing.Timer(100, e -> {

            if (!gameOver) {

                // =========================================
                // UPDATE MAP / INAMICI
                // =========================================
                currentMap.tickSparkle();
                player.tickHealth();

                if (currentMap instanceof Map3) {
                    Map3 map3 = (Map3) currentMap;

                    map3.tickSpiders(
                            player.getCol(),
                            player.getRow()
                    );
                    map3.tickBoss(player);

                    map3.checkEnemyCollisions(player);

                    if (player.isDead()) {
                        triggerGameOver();
                        return;
                    }
                }

                // =========================================
                // COLIZIUNI MAP2
                // =========================================
                if (currentMap instanceof Map2) {
                    List<SnakeEnemy> enemies = ((Map2) currentMap).getEnemies();

                    for (SnakeEnemy snakeEnemy : enemies) {
                        if (snakeEnemy.checkCollision(player.getX(), player.getY())) {
                            player.takeDamage(1);

                            if (player.isDead()) {
                                triggerGameOver();
                            }

                            break;
                        }
                    }
                }
            }

            // =========================================
            // HUD MESSAGE TIMER
            // =========================================
            if (hudMessageTimer > 0) {

                hudMessageTimer--;

                if (hudMessageTimer == 0) {
                    hudMessage = "";
                }
            }
            if (currentMap instanceof Map3 && !showChestCoords) {
                coordTimer++;

                if (coordTimer >= COORD_DELAY) {
                    showChestCoords = true;
                    System.out.println("Coordonate chest afisate!");
                }
            }

            repaint();
        });

        animTimer.start();
    }

    // ---------------------------------------------------------------
    // Game Over
    // ---------------------------------------------------------------
    private void triggerGameOver() {
        gameOver = true;
        showingGameOver = true;
        player.stopMoving();
        showMessage("Game Over! Apasa ENTER pentru a continua.");
    }

    // ---------------------------------------------------------------
    // Respawn la inceputul nivelului curent
    // ---------------------------------------------------------------
    private void respawn() {
        gameOver = false;
        showingGameOver = false;
        hudMessage = "";

        loadMapForLevel(currentLevel);

        player.setMap(currentMap);
        player.setCol(startCol);
        player.setRow(startRow);
        player.stopMoving();
        player.resetHealth();

        Database.savePlayerPosition(playerName, startCol, startRow);

        currentMap.stepOnTile(player.getCol(), player.getRow());
        repaint();
    }

    // ---------------------------------------------------------------
    // Buton pauza
    // ---------------------------------------------------------------
    private void setupPauseButton() {
        JButton pause = new JButton("II");
        pause.setBounds(15, 15, 30, 30);
        pause.setBackground(new Color(135, 11, 11, 224));
        pause.setOpaque(true);
        pause.setFocusable(false);
        add(pause);

        pause.addActionListener(e -> {
            JDialog dialog = new JDialog(parentFrame, "Game Options", true);
            dialog.setSize(400, 300);
            dialog.setUndecorated(true);
            dialog.setBackground(new Color(12, 12, 12, 150));
            dialog.setShape(new java.awt.geom.RoundRectangle2D.Double(
                    0, 0, dialog.getWidth(), dialog.getHeight(), 30, 30
            ));
            dialog.setLocationRelativeTo(parentFrame);
            dialog.setContentPane(new GameOption());
            dialog.setVisible(true);
            requestFocusInWindow();
        });
    }

    // ---------------------------------------------------------------
    // Meniu ESC
    // ---------------------------------------------------------------
    private void toggleOption() {
        if (dialog == null) {
            dialog = new JDialog(parentFrame, "Game Options", false);
            dialog.setSize(400, 300);
            dialog.setUndecorated(true);
            dialog.setBackground(new Color(12, 12, 12, 150));

            dialog.setShape(new java.awt.geom.RoundRectangle2D.Double(
                    0, 0, dialog.getWidth(), dialog.getHeight(), 30, 30
            ));

            dialog.setLocationRelativeTo(parentFrame);
            dialog.setContentPane(new GameOption());

            dialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                    .put(KeyStroke.getKeyStroke("ESCAPE"), "closeOptions");

            dialog.getRootPane().getActionMap()
                    .put("closeOptions", new AbstractAction() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            dialog.setVisible(false);
                            requestFocusInWindow();
                        }
                    });
        }

        dialog.setVisible(!dialog.isVisible());

        if (!dialog.isVisible()) {
            requestFocusInWindow();
        }
    }

    // ---------------------------------------------------------------
    // Input
    // ---------------------------------------------------------------
    private void setupInput() {
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {

                if (showingGameOver) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        respawn();
                    }
                    return;
                }

                switch (e.getKeyCode()) {

                    case KeyEvent.VK_W:
                    case KeyEvent.VK_UP:
                        player.moveUp();
                        break;

                    case KeyEvent.VK_S:
                    case KeyEvent.VK_DOWN:
                        player.moveDown();
                        break;

                    case KeyEvent.VK_A:
                    case KeyEvent.VK_LEFT:
                        player.moveLeft();
                        break;

                    case KeyEvent.VK_D:
                    case KeyEvent.VK_RIGHT:
                        player.moveRight();
                        break;

                    case KeyEvent.VK_3:
                        forceLevel3();
                        return;

                    case KeyEvent.VK_ENTER:
                        handleEnter();
                        return;

                    case KeyEvent.VK_SPACE:
                        handleAttack();
                        return;

                    case KeyEvent.VK_ESCAPE:
                        toggleOption();
                        return;

                    default:
                        return;
                }

                checkEnemyCollisionAfterMove();
                checkDiamondPickup();
                repaint();
            }

            @Override
            public void keyReleased(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_W:
                    case KeyEvent.VK_UP:
                    case KeyEvent.VK_S:
                    case KeyEvent.VK_DOWN:
                    case KeyEvent.VK_A:
                    case KeyEvent.VK_LEFT:
                    case KeyEvent.VK_D:
                    case KeyEvent.VK_RIGHT:
                        player.stopMoving();
                        break;
                }
            }
        });
    }

    private void checkEnemyCollisionAfterMove() {
        if (currentMap instanceof Map2) {
            for (SnakeEnemy snakeEnemy : ((Map2) currentMap).getEnemies()) {
                if (snakeEnemy.checkCollision(player.getX(), player.getY())) {
                    player.takeDamage(1);

                    if (player.isDead()) {
                        triggerGameOver();
                    }

                    return;
                }
            }
        }

        if (currentMap instanceof Map3) {
            Map3 map3 = (Map3) currentMap;

            map3.checkEnemyCollisions(player);

            if (player.isDead()) {
                triggerGameOver();
            }
        }
    }

    // ---------------------------------------------------------------
    // Colectare diamante (Map2)
    // ---------------------------------------------------------------
    private void checkDiamondPickup() {
        if (!(currentMap instanceof Map2)) return;
        Map2 map2 = (Map2) currentMap;
        if (map2.tryPickDiamond(player.getCol(), player.getRow())) {
            int rem = map2.getDiamondsRemaining();
            if (rem == 0) {
                showMessage("Ai cules toate diamantele! Usa este deschisa!");
            } else {
                showMessage("Diamant cules! Mai ai " + rem + " de gasit.");
            }
        }
    }

    // ---------------------------------------------------------------
    // Interactiune ENTER
    // ---------------------------------------------------------------
    private void handleEnter() {
        if (currentMap instanceof Map1) {
            Map1 map1 = (Map1) currentMap;

            if (!hasKey) {
                if (map1.tryOpenChest(player.getCol(), player.getRow())) {
                    hasKey = true;
                    showMessage("Ai gasit o cheie!");
                    return;
                }
            }

            if (map1.tryOpenDoor(player.getCol(), player.getRow(), hasKey)) {
                showMessage("Nivelul 2!");
                loadNextLevel();
                return;
            }

            showMessage("Nu poti face asta.");
            return;
        }

        if (currentMap instanceof Map2) {
            Map2 map2 = (Map2) currentMap;

            if (map2.tryOpenDoor(player.getCol(), player.getRow())) {
                if (!map2.allDiamondsCollected()) {
                    int rem = map2.getDiamondsRemaining();
                    showMessage("Trebuie sa culegi toate diamantele! Inca "
                            + rem + " ramas" + (rem == 1 ? "." : "e."));
                    return;
                }
                showMessage("Nivelul 3!");
                loadLevel3();
                return;
            }

            showMessage("Nu poti face asta.");
            return;
        }

        if (currentMap instanceof Map3) {
            Map3 map3 = (Map3) currentMap;

            if (!map3.isHammerPickedUp()) {
                if (map3.tryOpenChest(player.getCol(), player.getRow())) {
                    showMessage("Ai gasit un ciocan! Apasa SPACE langa paianjeni pentru a ataca.");
                    return;
                }
            }
            if (map3.tryOpenDoor(player.getCol(), player.getRow(), true)) {
                // codul tau de final joc
                return;
            }

            if (map3.getSpiderCount() > 0) {
                showMessage("Mai ai de omorat " + map3.getSpiderCount() + " paianjeni!");
                return;
            }
            if (map3.tryOpenDoor(player.getCol(), player.getRow(), true)) {
                JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);

                JOptionPane optionPane = new JOptionPane(
                        "Ai terminat jocul!",
                        JOptionPane.INFORMATION_MESSAGE,
                        JOptionPane.DEFAULT_OPTION
                );

                Database.savePlayerLevel(playerName, 1);
                Database.savePlayerPosition(playerName, 3, 3);

                JDialog dialog = optionPane.createDialog(frame, "Felicitari!");
                dialog.setModal(false);
                dialog.setVisible(true);

                new javax.swing.Timer(3000, e -> {
                    dialog.dispose();

                    frame.setContentPane(new MenuPanel());
                    frame.revalidate();
                    frame.repaint();

                    ((javax.swing.Timer) e.getSource()).stop();
                }).start();

                return;
            }

            showMessage("Nu poti face asta.");
            return;
        }

        showMessage("Nu poti face asta.");
    }
    private void handleAttack() {
        if (!(currentMap instanceof Map3)) {
            return;
        }

        Map3 map3 = (Map3) currentMap;

        if (!map3.isHammerPickedUp()) {
            showMessage("Nu ai nicio arma!");
            return;
        }

        if (map3.tryAttackSpider(player.getCol(), player.getRow())) {
            showMessage("Paianjen eliminat cu ciocanul!");
        } else if (map3.tryAttackBoss(player.getCol(), player.getRow())) {
            showMessage("Boss lovit!");
        } else {
            showMessage("Niciun inamic in raza de atac.");
        }

        repaint();
    }

    // ---------------------------------------------------------------
    // Trecere la nivelul 2
    // ---------------------------------------------------------------
    private void loadNextLevel() {
        currentLevel = 2;
        GamePanel.LAST_LEVEL = currentLevel;

        Database.savePlayerLevel(playerName, currentLevel);

        currentMap = MapFactory.createMap(2,playerName);
        startCol = 1;
        startRow = 1;

        player.setMap(currentMap);
        player.setCol(startCol);
        player.setRow(startRow);
        player.stopMoving();
        player.resetHealth();

        Database.savePlayerPosition(playerName, startCol, startRow);

        hasKey = false;

        currentMap.stepOnTile(player.getCol(), player.getRow());
        repaint();
    }

    // ---------------------------------------------------------------
    // Trecere la nivelul 3
    // ---------------------------------------------------------------
    private void loadLevel3() {
        currentLevel = 3;
        GamePanel.LAST_LEVEL = currentLevel;

        Database.savePlayerLevel(playerName, currentLevel);

        currentMap = MapFactory.createMap(3,playerName);
        startCol = 3;
        startRow = 3;

        player.setMap(currentMap);
        player.setCol(startCol);
        player.setRow(startRow);
        player.stopMoving();
        player.resetHealth();

        Database.savePlayerPosition(playerName, startCol, startRow);

        currentMap.stepOnTile(player.getCol(), player.getRow());
        repaint();
    }

    private void showMessage(String msg) {
        hudMessage = msg;
        hudMessageTimer = MSG_DURATION;
    }


    public void stopGame() {
        if (animTimer != null) {
            animTimer.stop();
        }

        player.stopAnimTimer();
    }

    // ---------------------------------------------------------------
    // Desenare
    // ---------------------------------------------------------------
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int camX = player.calcCameraX(VIEW_WIDTH);
        int camY = player.calcCameraY(VIEW_HEIGHT);

        currentMap.drawMap(g, camX, camY, VIEW_WIDTH, VIEW_HEIGHT);
        player.draw(g, camX, camY);
        drawHud(g);

        if (!hudMessage.isEmpty()) {
            drawCenteredMessage(g, hudMessage);
        }

        if (showingGameOver) {
            drawGameOver(g);
        }
    }

    // ---------------------------------------------------------------
    // Ecran Game Over
    // ---------------------------------------------------------------
    private void drawGameOver(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRect(0, 0, VIEW_WIDTH, VIEW_HEIGHT);

        g2.setFont(new Font("Arial", Font.BOLD, 60));
        g2.setColor(new Color(220, 30, 30));

        String title = "GAME OVER";
        FontMetrics fm = g2.getFontMetrics();

        g2.drawString(
                title,
                VIEW_WIDTH / 2 - fm.stringWidth(title) / 2,
                VIEW_HEIGHT / 2 - 30
        );

        g2.setFont(new Font("Arial", Font.PLAIN, 22));
        g2.setColor(Color.WHITE);

        String sub = "Apasa ENTER pentru a relua nivelul";
        fm = g2.getFontMetrics();

        g2.drawString(
                sub,
                VIEW_WIDTH / 2 - fm.stringWidth(sub) / 2,
                VIEW_HEIGHT / 2 + 30
        );
    }

    private void drawHud(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        if (currentLevel != 1) {
            g2.setColor(new Color(0, 0, 0, 160));
            g2.fillRoundRect(15, 55, 100, 30, 10, 10);

            g2.setColor(Color.RED);
            g2.setFont(new Font("Arial", Font.BOLD, 16));
            g2.drawString("HP: " + player.getHealth(), 35, 75);
        }
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRoundRect(520, 5, 265, 50, 10, 10);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 14));
        g2.drawString("Jucator: " + playerName, 590, 23);

        g2.setFont(new Font("Arial", Font.PLAIN, 11));
        g2.drawString(
                "WASD=miscare  ENTER=interactiune  SPACE=atac  ESC=meniu",
                525,
                45
        );

        if (currentMap instanceof Map2) {
            Map2 map2 = (Map2) currentMap;
            int rem   = map2.getDiamondsRemaining();
            int total = 5;
            int cules = total - rem;

            g2.setColor(new Color(0, 0, 0, 160));
            g2.fillRoundRect(55, 10, 175, 38, 10, 10);

            g2.setColor(new Color(0, 200, 255));
            g2.setFont(new Font("Arial", Font.BOLD, 14));
            g2.drawString("\u25C6 Diamante: " + cules + " / " + total, 68, 28);

            if (rem == 0) {
                g2.setColor(new Color(50, 255, 100));
                g2.setFont(new Font("Arial", Font.BOLD, 11));
                g2.drawString("Usa deschisa! Apasa ENTER.", 68, 42);
            } else {
                g2.setColor(new Color(255, 220, 50));
                g2.setFont(new Font("Arial", Font.PLAIN, 11));
                g2.drawString("Mai gaseste " + rem + " pentru a trece!", 68, 42);
            }
        }

        if (hasKey) {
            g2.setColor(new Color(0, 0, 0, 160));
            g2.fillRoundRect(55, 10, 140, 30, 10, 10);

            g2.setColor(new Color(255, 215, 0));
            g2.setFont(new Font("Arial", Font.BOLD, 13));
            g2.drawString("Cheie: 1", 75, 30);
        }

        if (currentMap instanceof Map3) {
            Map3 map3 = (Map3) currentMap;

            g2.setColor(new Color(0, 0, 0, 180));
            g2.fillRoundRect(15, 95, 180, 30, 10, 10);

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 13));

            if (showChestCoords) {
                g2.drawString(
                        "Chest: " + map3.getChestCol() + ", " + map3.getChestRow(),
                        30,
                        115
                );
            } else {
                g2.drawString("Chest: apare in 30 sec", 30, 115);
            }
        }
    }

    private void drawCenteredMessage(Graphics g, String message) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setFont(new Font("Arial", Font.BOLD, 26));

        FontMetrics fm = g2.getFontMetrics();

        int mw = fm.stringWidth(message);
        int mh = fm.getHeight();

        int bx = VIEW_WIDTH / 2 - mw / 2 - 24;
        int by = VIEW_HEIGHT / 2 - mh / 2 - 12;

        float alpha = Math.min(1f, hudMessageTimer / 30f);

        g2.setColor(new Color(0, 0, 0, (int) (180 * alpha)));
        g2.fillRoundRect(bx, by, mw + 48, mh + 24, 16, 16);

        g2.setColor(new Color(255, 215, 0, (int) (255 * alpha)));
        g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(bx, by, mw + 48, mh + 24, 16, 16);

        g2.setColor(new Color(255, 255, 255, (int) (255 * alpha)));
        g2.drawString(message, VIEW_WIDTH / 2 - mw / 2, VIEW_HEIGHT / 2 + mh / 4);
    }

    public void forceLevel3() {
        loadLevel3();
    }

    public int getCurrentLevel() {
        return currentLevel;
    }
}