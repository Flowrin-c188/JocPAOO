package PaooGame.MenuWindow;

import PaooGame.PlayWindow.GamePanel;
import PaooGame.PlayWindow.MapBase;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class MenuPanel extends JPanel {

    Image background;

    public MenuPanel() {

        background = new ImageIcon("res/textures/home.png").getImage();

        setLayout(null);

        RoundedButton play = new RoundedButton("");
        play.setBounds(280,247,220,30);
        play.setColor(new Color(24,210,20,50),new Color(24,190,20,30));
        play.setContentAreaFilled(false);
        play.setOpaque(false);
        add(play);
        Sound music = Sound.getInstance();
        music.playMusic("/music/home1.wav");
        play.addActionListener(e -> {

            music.stop();
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);

            frame.setContentPane(new NameWindow(frame));
            frame.revalidate();
            frame.repaint();

        });
//        JButton test=new JButton("Harta niv 3");
//        test.setBounds(280,200,220,30);
//        add(test);
//        test.addActionListener(e -> {
//            music.stop();
//
//            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
//
//            GamePanel gp = new GamePanel(frame, "niv3", 3);
//
//            frame.setContentPane(gp);
//            frame.revalidate();
//            frame.repaint();
//
//            SwingUtilities.invokeLater(gp::requestFocusInWindow);
//        });
        RoundedButton options = new RoundedButton("");
        options.setBounds(280,300,220,30);
        options.setColor(new Color(0, 45, 255,50),new Color(0,45,255,30));
        options.setBackground(Color.black);
        options.setContentAreaFilled(false);

        options.setOpaque(false);
        add(options);
        options.addActionListener(e->{
            new OptionWindow((JFrame) SwingUtilities.getWindowAncestor(this),music);
        });

        RoundedButton about = new RoundedButton("");
        about.setBounds(280,350,220,30);
        about.setColor(new Color(213, 202, 63, 50),new Color(213, 200, 60, 30));
        about.setOpaque(false);
        add(about);
        about.addActionListener(e -> {
            try {
                File site=new File("src/about.html");
                Desktop.getDesktop().browse(site.toURI());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        RoundedButton exit = new RoundedButton("");
        exit.setBounds(280,405,220,30);
        exit.setColor(new Color(255, 0, 0, 80),new Color(255,10,20,40));

        exit.setOpaque(false);
        add(exit);

        exit.addActionListener(e -> System.exit(0));

        play.addActionListener(e -> System.out.println("Start Game"));
        options.addActionListener(e -> System.out.println("Options"));
        about.addActionListener(e -> System.out.println("About"));

    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.drawImage(background,0,0,getWidth(),getHeight(),this);
    }
}