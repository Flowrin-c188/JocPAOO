package PaooGame.MenuWindow;

import javax.swing.*;
import java.awt.*;

public class RoundedButton extends JButton {
    private Color a;
    private Color b;
    public RoundedButton(String text) {
        super(text);
        setContentAreaFilled(false);
        setFocusPainted(false);
    }
    public void setColor(Color a,Color b)
    {
        this.a=a;
        this.b=b;
    }
    @Override
    protected void paintComponent(Graphics g) {

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        ButtonModel model = getModel();

        if(model.isPressed()) {
            g2.setColor(a);
        }
        else if(model.isRollover()) {
            g2.setColor(b);
        }
        else {
            g2.setColor(new Color(0, 0, 0, 0));
        }

        g2.fillRoundRect(0,0,getWidth(),getHeight(),
                getHeight(),getHeight());

        g2.dispose();

        super.paintComponent(g);
    }

    @Override
    protected void paintBorder(Graphics g) {

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(new Color(0,0,0,0));

        g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,
                getHeight(),getHeight());

        g2.dispose();
    }
}