package PaooGame.MenuWindow;

import javax.swing.*;
import javax.swing.plaf.basic.BasicSliderUI;
import java.awt.*;

public class ColoredSliderUI extends BasicSliderUI {

    private final Color trackColor;
    private final Color thumbColor;

    public ColoredSliderUI(JSlider slider, Color trackColor, Color thumbColor) {
        super(slider);
        this.trackColor = trackColor;
        this.thumbColor = thumbColor;

        // 👇 adăugat pentru update vizual când valoarea sliderului se schimbă
        slider.addChangeListener(e -> slider.repaint());
    }

    @Override
    public void paintTrack(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int trackHeight = 6;
        int y = trackRect.y + trackRect.height / 2 - trackHeight / 2;

        g2.setColor(trackColor);
        g2.fillRoundRect(trackRect.x, y, trackRect.width, trackHeight, 10, 10);

        g2.dispose();
    }

    @Override
    public void paintThumb(Graphics g)
    {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        int x = thumbRect.x;
        int y = thumbRect.y;
        int w = thumbRect.width;
        int h = thumbRect.height;

        Polygon diamond = new Polygon();

        diamond.addPoint(x + w/2, y);
        diamond.addPoint(x + w, y + h/2);
        diamond.addPoint(x + w/2, y + h);
        diamond.addPoint(x, y + h/2);

        g2.setColor(thumbColor);
        g2.fillPolygon(diamond);

        g2.dispose();
    }

    @Override
    protected Dimension getThumbSize() {
        return new Dimension(16, 16);
    }
}