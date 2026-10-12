package PaooGame.MenuWindow;
import PaooGame.MenuWindow.ColoredSliderUI;
import PaooGame.MenuWindow.ImagePanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

public class OptionWindow extends JDialog {

    private Sound music;

    public OptionWindow(JFrame windowAncestor, Sound music) {

        super(windowAncestor, "Options", ModalityType.APPLICATION_MODAL);

        this.music = music;

        setSize(400,300);
        setLocationRelativeTo(windowAncestor);
        setResizable(false);
        setUndecorated(true);
        setLayout(null);

        ImagePanel panel = new ImagePanel("/textures/options.png");
        panel.setBounds(0,0,400,300);
        panel.setLayout(null);

        setContentPane(panel);

        RoundedButton close = new RoundedButton("");
        close.setColor(
                new Color(255,0,0,100),
                new Color(147,0,0,100)
        );
        close.setBounds(140,250,120,30);
        close.addActionListener(e -> dispose());

        panel.add(close);

        RoundedButton mute = new RoundedButton("");

        mute.setBounds(299,196,30,30);
        mute.setColor(new Color(255, 0, 13, 100),new Color(255, 0, 0, 40));

        mute.setContentAreaFilled(false);
        mute.setOpaque(false);
        panel.add(mute);

        JSlider volume = new JSlider(0,100,30);

        mute.addActionListener(e -> {
            if(volume.getValue()==0)
            {
                volume.setValue(50);
                volume.repaint();
            }
            else {
                volume.setValue(0);
                volume.repaint();
            }
        });
        mute.addActionListener(e -> {
        });

        volume.setBounds(70,100,220,40);
        volume.setOpaque(false);
        volume.setFocusable(false);
        volume.setBorder(null);

        volume.setUI(new ColoredSliderUI(
                volume,
                new Color(0,0,0,80),
                new Color(212,175,55)
        ));

        panel.add(volume);

        volume.addChangeListener(e -> {

            int value = volume.getValue();

            music.setVolume(value);

        });

        setVisible(true);
        startAnimation();
    }

    private void startAnimation(){

        int finalWidth = 400;
        int finalHeight = 300;

        Point center = getLocation();
        int centerX = center.x + finalWidth / 2;
        int centerY = center.y + finalHeight / 2;

        setSize(0,0);
        setLocation(centerX, centerY);
    }

    public void keyReleased(KeyEvent e)
    {
        int code=e.getKeyCode();
    }
}