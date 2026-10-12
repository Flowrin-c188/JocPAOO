package PaooGame.MenuWindow;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import PaooGame.PlayWindow.GamePanel;
import PaooGame.MenuWindow.Database;

public class NameWindow extends JPanel {
    Image name;

    public NameWindow(JFrame frame) {
        name = new ImageIcon("res/textures/name.png").getImage();
        setLayout(null);

        Database.createTable();

        JTextField nume = new JTextField("Introdu numele");
        nume.setBounds(260, 252, 264, 37);

        nume.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                if (nume.getText().length() >= 18) {
                    e.consume();
                }
            }
        });

        nume.setCaretColor(Color.CYAN);
        nume.setSelectionColor(Color.BLUE);
        nume.setForeground(Color.WHITE);
        nume.setFont(new Font("Arial", Font.BOLD, 20));
        nume.setOpaque(false);

        nume.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (nume.getText().equals("Introdu numele")) {
                    nume.setText("");
                }
            }

            public void focusLost(FocusEvent e) {
                if (nume.getText().isEmpty()) {
                    nume.setText("Introdu numele");
                }
            }
        });

        add(nume);

        RoundedButton home_name = new RoundedButton("");
        home_name.setColor(new Color(145, 3, 13, 131), new Color(213, 63, 63, 50));
        home_name.setBounds(325, 470, 140, 30);
        add(home_name);

        RoundedButton start = new RoundedButton("");
        start.setBounds(326, 385, 130, 29);
        start.setColor(new Color(24, 210, 20, 111), new Color(24, 210, 20, 77));
        add(start);

        home_name.addActionListener(e -> {
            frame.setContentPane(new MenuPanel());
            frame.revalidate();
            frame.repaint();
        });

        start.addActionListener(e -> {
            String playerName = nume.getText().trim();

            if (playerName.isEmpty() || playerName.equals("Introdu numele")) {
                JOptionPane.showMessageDialog(
                        this,
                        "Trebuie sa introduci un nume!",
                        "Nume lipsa",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            Database.createPlayerIfNotExists(playerName);

            int savedLevel = Database.getPlayerLevel(playerName);

            GamePanel game = new GamePanel(frame, playerName, savedLevel);

            frame.setContentPane(game);
            frame.revalidate();
            frame.repaint();

            SwingUtilities.invokeLater(game::requestFocusInWindow);
        });
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(name, 0, 0, getWidth(), getHeight(), this);
    }
}