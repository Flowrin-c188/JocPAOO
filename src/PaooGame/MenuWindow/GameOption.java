package PaooGame.MenuWindow;

import PaooGame.PlayWindow.GamePanel;

import javax.swing.*;
import java.awt.*;

public class GameOption extends JPanel {

    private JPanel menuPanel;
    private GamePanel level;

    public GameOption() {
        setLayout(null);
        setOpaque(false);

        // fereastra mica din centru
        menuPanel = new JPanel();
        menuPanel.setLayout(null);
        menuPanel.setBackground(new Color(40, 40, 40,150));
        menuPanel.setBounds(200, 150, 400, 300);

        JLabel title = new JLabel("PAUZA");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 28));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setBounds(100, 20, 200, 40);
        menuPanel.add(title);

        JButton resume = new JButton("Resume");
        resume.setBounds(140, 100, 120, 40);
        menuPanel.add(resume);

        JButton restart = new JButton("Restart");
        restart.setBounds(140, 160, 120, 40);
        menuPanel.add(restart);

        JButton exit = new JButton("Exit");
        exit.setBounds(140, 220, 120, 40);
        menuPanel.add(exit);

        add(menuPanel);

        resume.addActionListener(e -> {
            java.awt.Window window = javax.swing.SwingUtilities.getWindowAncestor(this);
            if (window != null) {
                window.dispose(); // închide fereastra
            }
        });
        restart.addActionListener(e -> {
            JDialog dialog = (JDialog) SwingUtilities.getWindowAncestor(this);
            JFrame frame = (JFrame) dialog.getOwner();

            dialog.dispose();

            // luam GamePanel-ul curent
            GamePanel oldGame = (GamePanel) frame.getContentPane();

            // luam nivelul curent din el
            int level = oldGame.getCurrentLevel();

            // recream jocul cu acelasi nivel
            frame.setContentPane(new GamePanel(frame, "", level));

            frame.revalidate();
            frame.repaint();
        });
        exit.addActionListener(e -> {
            JDialog dialog = (JDialog) SwingUtilities.getWindowAncestor(this);
            JFrame frame = (JFrame) dialog.getOwner();

            dialog.dispose();

            frame.setContentPane(new MenuPanel());
            frame.revalidate();
            frame.repaint();
        });



    }

    @Override
    public void doLayout() {
        super.doLayout();

        int w = getWidth();
        int h = getHeight();

        menuPanel.setBounds(w / 2 - 200, h / 2 - 150, 400, 300);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
    }
}